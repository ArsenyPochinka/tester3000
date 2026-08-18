package ru.vtb.tester3000.kafka;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import ru.vtb.tester3000.config.TesterProperties;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

/**
 * Заглушка: публикует по 2 сообщения со случайными статусами
 * в топики 25 / 104 / 39 с корректной цепочкой связки
 * (39.Instruction.Id.Id = 104.Object.Id.Id → Parent RelatedInstructions → rId).
 */
@Component
@ConditionalOnProperty(prefix = "tester3000.kafka", name = "fin-outbox-stub-enabled", havingValue = "true", matchIfMissing = true)
public class FinOutboxStubPublisher {

    private static final List<String> FIN_MESSAGE_STATUSES = List.of(
            "New", "Received", "Processing", "Approved", "Rejected", "Executed", "SystemError", "Error"
    );
    private static final List<String> FIN_INSTRUCTION_STATUSES = List.of(
            "Canceled", "Completed", "Error", "Executed", "New", "Pending", "Processing", "Rejected"
    );
    private static final List<String> FIN_TRANSACTION_STATUSES = List.of(
            "Accepted", "Completed", "Error", "Executed", "New", "Pending", "Processing", "Rejected"
    );

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final TesterProperties properties;
    private final ObjectMapper objectMapper;

    public FinOutboxStubPublisher(
            KafkaTemplate<String, String> stringKafkaTemplate,
            TesterProperties properties,
            ObjectMapper objectMapper
    ) {
        this.kafkaTemplate = stringKafkaTemplate;
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    public record PublishResult(boolean success, String topic, String status, String payload, String detail) {
    }

    /**
     * По 2 набора: 25 (Object.Id.Id=rId), 104 (Object.Id.Id=instrId, Parent→rId),
     * 39 (Instruction.Id.Id=instrId). 104 публикуется до 39.
     */
    public List<PublishResult> publishRandomPairToAllTopics(String reqId, JsonNode sourceMessage) {
        TesterProperties.Kafka kafka = properties.getKafka();
        List<PublishResult> results = new ArrayList<>(6);
        for (int i = 0; i < 2; i++) {
            String messageStatus = randomStatus(FIN_MESSAGE_STATUSES);
            results.add(publish(
                    kafka.getFinOutboxTopic(),
                    buildFinMessage(reqId, messageStatus, sourceMessage),
                    reqId,
                    messageStatus
            ));

            String instructionObjectId = UUID.randomUUID().toString();
            String instructionStatus = randomStatus(FIN_INSTRUCTION_STATUSES);
            results.add(publish(
                    kafka.getFinInstructionTopic(),
                    buildFinInstruction(instructionObjectId, reqId, instructionStatus, sourceMessage),
                    reqId,
                    instructionStatus
            ));

            String transactionStatus = randomStatus(FIN_TRANSACTION_STATUSES);
            results.add(publish(
                    kafka.getFinTransactionTopic(),
                    buildFinTransaction(instructionObjectId, transactionStatus, sourceMessage),
                    reqId,
                    transactionStatus
            ));
        }
        return results;
    }

    private PublishResult publish(String topic, ObjectNode message, String kafkaKey, String objectStatus) {
        try {
            // status already baked into message; re-read for result record
            String status = message.at("/Object/Status").asText(objectStatus);
            String payload = objectMapper.writeValueAsString(message);
            var result = kafkaTemplate.send(topic, kafkaKey, payload).get(30, TimeUnit.SECONDS);
            var meta = result.getRecordMetadata();
            return new PublishResult(
                    true,
                    topic,
                    status,
                    payload,
                    "topic=" + meta.topic() + ", partition=" + meta.partition() + ", offset=" + meta.offset()
            );
        } catch (Exception ex) {
            return new PublishResult(false, topic, objectStatus, null, ex.getMessage());
        }
    }

    private ObjectNode buildFinMessage(String reqId, String objectStatus, JsonNode sourceMessage) {
        ObjectNode root = baseEnvelope("BusinessCardIncomingMessage", "2.5.0", objectStatus, sourceMessage);
        ObjectNode object = (ObjectNode) root.get("Object");
        ObjectNode objectId = (ObjectNode) object.get("Id");
        objectId.put("Id", reqId);
        return root;
    }

    private ObjectNode buildFinInstruction(
            String instructionObjectId,
            String parentReqId,
            String objectStatus,
            JsonNode sourceMessage
    ) {
        ObjectNode root = baseEnvelope("BusinessCardFinancialInstruction", "2.3.0", objectStatus, sourceMessage);
        ObjectNode object = (ObjectNode) root.get("Object");
        ObjectNode objectId = (ObjectNode) object.get("Id");
        objectId.put("Id", instructionObjectId);

        ObjectNode related = object.putArray("RelatedInstructions").addObject();
        related.put("Type", "Parent");
        ObjectNode instruction = related.putObject("Instruction");
        ObjectNode instructionId = instruction.putObject("Id");
        instructionId.put("Id", parentReqId);
        instructionId.put("System", "CCOP");
        return root;
    }

    private ObjectNode buildFinTransaction(
            String instructionObjectId,
            String objectStatus,
            JsonNode sourceMessage
    ) {
        ObjectNode root = baseEnvelope("BusinessCardFinancialTransaction", "2.3.0", objectStatus, sourceMessage);
        ObjectNode object = (ObjectNode) root.get("Object");
        ObjectNode objectId = (ObjectNode) object.get("Id");
        objectId.put("Id", UUID.randomUUID().toString());

        ObjectNode instruction = object.putObject("Instruction");
        ObjectNode instructionId = instruction.putObject("Id");
        instructionId.put("Id", instructionObjectId);
        instructionId.put("System", "CCOP");
        return root;
    }

    private ObjectNode baseEnvelope(
            String objectName,
            String version,
            String objectStatus,
            JsonNode sourceMessage
    ) {
        String now = OffsetDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        String kind = sourceMessage.at("/cbiRequest/tranRequest/kind").asText("Goods");
        String lifePhase = sourceMessage.at("/cbiRequest/tranRequest/lifePhase").asText("Auth");
        double amt = sourceMessage.at("/cbiRequest/tranRequest/moneys/clear/amt").asDouble(0);
        int ccy = sourceMessage.at("/cbiRequest/tranRequest/moneys/clear/ccy").asInt(643);

        ObjectNode root = objectMapper.createObjectNode();
        root.put("Id", UUID.randomUUID().toString());
        root.put("Type", "Message");
        root.put("PublishTime", now);
        root.put("ChangeType", "Create");

        ObjectNode publisher = root.putObject("Publisher");
        publisher.put("System", "CCOP");
        publisher.put("Service", "tester3000-stub");

        ObjectNode meta = root.putObject("Meta");
        meta.put("Object", objectName);
        meta.put("ObjectType", "BusinessObject");
        ObjectNode schema = meta.putObject("Schema");
        schema.put("Id", objectName + "." + version);
        schema.put("Version", version);

        ObjectNode object = root.putObject("Object");
        object.put("Status", objectStatus);
        ObjectNode objectId = object.putObject("Id");
        objectId.put("Id", UUID.randomUUID().toString());
        objectId.put("System", "CCOP");
        objectId.put("CreateTime", now);
        objectId.put("UpdateTime", now);

        ObjectNode body = object.putObject("Body");
        body.put("TokenKind", "Card");
        body.put("TransactionKind", kind);
        body.put("TransactionLifePhase", lifePhase);
        body.put("TransactionLocalDateTime",
                sourceMessage.at("/cbiRequest/tranRequest/localTime").asText(now));
        body.put("IsReversalTransaction",
                sourceMessage.at("/cbiRequest/tranRequest/isReversal").asBoolean(false));
        body.put("IsAdviceTransaction",
                sourceMessage.at("/cbiRequest/tranRequest/isAdvice").asBoolean(false));
        body.put("IsPartialTransaction",
                sourceMessage.at("/cbiRequest/tranRequest/isPartial").asBoolean(false));
        body.put("CardEntryMode",
                sourceMessage.at("/cbiRequest/tranRequest/parties/cust/token/0/card/entryMode").asText("Icc"));

        ObjectNode amount = body.putObject("TransactionAmount");
        amount.put("Amount", amt);
        amount.put("Currency", String.valueOf(ccy));

        ObjectNode terminal = body.putObject("Terminal");
        terminal.put("Type", sourceMessage.at("/cbiRequest/tranRequest/parties/term/type").asText("Pos"));
        terminal.put("MerchantCategoryCode",
                sourceMessage.at("/cbiRequest/tranRequest/parties/term/owner/mcc").asInt(0));
        terminal.put("MerchantName",
                sourceMessage.at("/cbiRequest/tranRequest/parties/term/owner/title").asText("STUB"));
        terminal.put("City",
                sourceMessage.at("/cbiRequest/tranRequest/parties/term/owner/city").asText("MOSCOW"));

        return root;
    }

    private static String randomStatus(List<String> statuses) {
        return statuses.get(ThreadLocalRandom.current().nextInt(statuses.size()));
    }
}
