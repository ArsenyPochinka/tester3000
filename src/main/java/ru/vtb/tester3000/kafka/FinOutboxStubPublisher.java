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
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

/**
 * Заглушка: публикует по 2 сообщения со случайными статусами
 * в топики 25_FIN_MESSAGE / 104_FIN_INSTRUCTION / 39_FIN_TRANSACTION.
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
     * По 2 сообщения со случайным статусом в каждый из трёх outbox-топиков.
     */
    public List<PublishResult> publishRandomPairToAllTopics(String reqId, JsonNode sourceMessage) {
        TesterProperties.Kafka kafka = properties.getKafka();
        return List.of(
                publishRandom(kafka.getFinOutboxTopic(), "BusinessCardIncomingMessage", "2.5.0",
                        FIN_MESSAGE_STATUSES, reqId, sourceMessage),
                publishRandom(kafka.getFinOutboxTopic(), "BusinessCardIncomingMessage", "2.5.0",
                        FIN_MESSAGE_STATUSES, reqId, sourceMessage),
                publishRandom(kafka.getFinInstructionTopic(), "BusinessCardFinancialInstruction", "2.3.0",
                        FIN_INSTRUCTION_STATUSES, reqId, sourceMessage),
                publishRandom(kafka.getFinInstructionTopic(), "BusinessCardFinancialInstruction", "2.3.0",
                        FIN_INSTRUCTION_STATUSES, reqId, sourceMessage),
                publishRandom(kafka.getFinTransactionTopic(), "BusinessCardFinancialTransaction", "2.3.0",
                        FIN_TRANSACTION_STATUSES, reqId, sourceMessage),
                publishRandom(kafka.getFinTransactionTopic(), "BusinessCardFinancialTransaction", "2.3.0",
                        FIN_TRANSACTION_STATUSES, reqId, sourceMessage)
        );
    }

    private PublishResult publishRandom(
            String topic,
            String objectName,
            String version,
            List<String> statuses,
            String reqId,
            JsonNode sourceMessage
    ) {
        String status = statuses.get(ThreadLocalRandom.current().nextInt(statuses.size()));
        return publish(topic, objectName, version, status, reqId, sourceMessage);
    }

    private PublishResult publish(
            String topic,
            String objectName,
            String version,
            String objectStatus,
            String reqId,
            JsonNode sourceMessage
    ) {
        try {
            ObjectNode message = buildMessage(objectName, version, reqId, objectStatus, sourceMessage);
            String payload = objectMapper.writeValueAsString(message);
            var result = kafkaTemplate.send(topic, reqId, payload).get(30, TimeUnit.SECONDS);
            var meta = result.getRecordMetadata();
            return new PublishResult(
                    true,
                    topic,
                    objectStatus,
                    payload,
                    "topic=" + meta.topic() + ", partition=" + meta.partition() + ", offset=" + meta.offset()
            );
        } catch (Exception ex) {
            return new PublishResult(false, topic, objectStatus, null, ex.getMessage());
        }
    }

    private ObjectNode buildMessage(
            String objectName,
            String version,
            String reqId,
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
        objectId.put("Id", reqId);
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
}
