package ru.vtb.tester3000.kafka;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.vtb.tester3000.entity.ProcessEntity;
import ru.vtb.tester3000.logging.RegressionRunLog;
import ru.vtb.tester3000.repository.ProcessRepository;

import java.time.Clock;
import java.time.Instant;
import java.util.EnumSet;
import java.util.Optional;
import java.util.UUID;

/**
 * Слушает outbox-топики 25 / 104 / 39.
 * События без связки с запусками данного приложения вычитываются и игнорируются без логов.
 */
@Component
public class FinMessageOutboxConsumer {

    private static final EnumSet<ProcessEntity.Step> LINK_STEPS = EnumSet.of(
            ProcessEntity.Step.AUTH_SEND,
            ProcessEntity.Step.CLEARING_SEND
    );

    private final ProcessRepository processRepository;
    private final ObjectMapper objectMapper;
    private final Clock clock;
    private final RegressionRunLog runLog;

    public FinMessageOutboxConsumer(
            ProcessRepository processRepository,
            ObjectMapper objectMapper,
            Clock clock,
            RegressionRunLog runLog
    ) {
        this.processRepository = processRepository;
        this.objectMapper = objectMapper;
        this.clock = clock;
        this.runLog = runLog;
    }

    @KafkaListener(
            topics = "${tester3000.kafka.fin-outbox-topic:tsss.ccop_incoming_fin_message.outbox}",
            groupId = "${tester3000.kafka.fin-outbox-group-id:tester3000-fin-outbox}"
    )
    @Transactional
    public void onFinMessage(@Payload String payload) {
        handle(payload, ProcessEntity.Step.FIN_MESSAGE_25, "25_FIN_MESSAGE");
    }

    @KafkaListener(
            topics = "${tester3000.kafka.fin-instruction-topic:tsss.ccop_fin_instruction.outbox}",
            groupId = "${tester3000.kafka.fin-instruction-group-id:tester3000-fin-instruction}"
    )
    @Transactional
    public void onFinInstruction(@Payload String payload) {
        handle(payload, ProcessEntity.Step.FIN_INSTRUCTION_104, "104_FIN_INSTRUCTION");
    }

    @KafkaListener(
            topics = "${tester3000.kafka.fin-transaction-topic:tsss.ccop_fin_transaction.outbox}",
            groupId = "${tester3000.kafka.fin-transaction-group-id:tester3000-fin-transaction}"
    )
    @Transactional
    public void onFinTransaction(@Payload String payload) {
        handle(payload, ProcessEntity.Step.FIN_TRANSACTION_39, "39_FIN_TRANSACTION");
    }

    private void handle(String payload, ProcessEntity.Step step, String stepLabel) {
        final String reqId;
        final String streamingStatus;
        final Optional<ProcessEntity> link;
        try {
            JsonNode root = objectMapper.readTree(payload);
            reqId = extractReqId(root);
            streamingStatus = extractStreamingStatus(root);
            if (reqId == null || reqId.isBlank() || streamingStatus == null || streamingStatus.isBlank()) {
                return;
            }
            link = processRepository.findFirstByReqIdAndStepInOrderByCreatedAtDesc(reqId, LINK_STEPS);
            if (link.isEmpty()) {
                return;
            }
        } catch (Exception ignored) {
            return;
        }

        ProcessEntity base = link.get();
        try {
            ProcessEntity.Status status = mapStatus(streamingStatus);
            ProcessEntity row = new ProcessEntity();
            row.setId(UUID.randomUUID());
            row.setRunId(base.getRunId());
            row.setTestMessageId(base.getTestMessageId());
            row.setTestName(base.getTestName());
            row.setType(base.getType());
            row.setReqId(reqId);
            row.setStep(step);
            row.setStatus(status);
            row.setDescription("Получено событие " + stepLabel + ": " + streamingStatus);
            row.setResult(payload);
            row.setCreatedAt(Instant.now(clock));
            processRepository.save(row);

            runLog.received(base.getRunId(), reqId, stepLabel + " status=" + streamingStatus
                    + " test=" + base.getTestName() + " type=" + base.getType(), payload);
        } catch (Exception ex) {
            runLog.error(base.getRunId(), reqId, "Ошибка обработки " + stepLabel, ex);
        }
    }

    private ProcessEntity.Status mapStatus(String streamingStatus) {
        try {
            return ProcessEntity.Status.valueOf(streamingStatus);
        } catch (IllegalArgumentException ex) {
            return ProcessEntity.Status.Error;
        }
    }

    private String extractReqId(JsonNode root) {
        JsonNode objectId = root.at("/Object/Id/Id");
        if (!objectId.isMissingNode() && !objectId.isNull() && !objectId.asText().isBlank()) {
            return objectId.asText();
        }
        JsonNode id = root.path("Id");
        if (id.isObject() && id.path("Id").isTextual()) {
            return id.path("Id").asText();
        }
        return null;
    }

    private String extractStreamingStatus(JsonNode root) {
        JsonNode status = root.at("/Object/Status");
        if (!status.isMissingNode() && status.isTextual()) {
            return status.asText();
        }
        if (root.hasNonNull("storage_event")) {
            return root.get("storage_event").asText();
        }
        return null;
    }
}
