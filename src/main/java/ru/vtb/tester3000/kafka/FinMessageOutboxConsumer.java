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
import java.util.Iterator;
import java.util.Optional;
import java.util.UUID;

/**
 * Слушает outbox-топики 25 / 104 / 39.
 * Связка с отправленными m210/m095 по rId:
 * <ul>
 *   <li>25 — {@code Object.Id.Id} = rId</li>
 *   <li>104 — {@code RelatedInstructions.Instruction.Id.Id} = rId,
 *       где RelatedInstructions[].Type = Parent</li>
 *   <li>39 — {@code Instruction.Id.Id} = {@code Object.Id.Id} события 104,
 *       далее rId через RelatedInstructions[].Type = Parent этого 104</li>
 * </ul>
 * События без связки вычитываются и игнорируются без логов.
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
            streamingStatus = extractStreamingStatus(root);
            if (streamingStatus == null || streamingStatus.isBlank()) {
                return;
            }
            if (step == ProcessEntity.Step.FIN_TRANSACTION_39) {
                link = resolveLinkViaFinInstruction104(root);
                reqId = link.map(ProcessEntity::getReqId).orElse(null);
            } else {
                reqId = extractDirectReqId(root, step);
                if (reqId == null || reqId.isBlank()) {
                    return;
                }
                link = processRepository.findFirstByReqIdAndStepInOrderByCreatedAtDesc(reqId, LINK_STEPS);
            }
            if (reqId == null || reqId.isBlank() || link.isEmpty()) {
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

    /**
     * 39: Instruction.Id.Id → Object.Id.Id записи 104 → rId из Parent RelatedInstructions (уже в process.req_id).
     */
    private Optional<ProcessEntity> resolveLinkViaFinInstruction104(JsonNode root) {
        String instructionObjectId = firstNonBlank(
                textAt(root, "/Object/Instruction/Id/Id"),
                textAt(root, "/Instruction/Id/Id")
        );
        if (instructionObjectId == null) {
            return Optional.empty();
        }
        // 39 может прийти раньше, чем consumer сохранит 104 — короткие ретраи
        for (int attempt = 0; attempt < 15; attempt++) {
            Optional<ProcessEntity> found =
                    processRepository.findLatestFinInstruction104ByObjectId(instructionObjectId);
            if (found.isPresent()) {
                return found;
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return Optional.empty();
            }
        }
        return Optional.empty();
    }

    private ProcessEntity.Status mapStatus(String streamingStatus) {
        try {
            return ProcessEntity.Status.valueOf(streamingStatus);
        } catch (IllegalArgumentException ex) {
            return ProcessEntity.Status.Error;
        }
    }

    private String extractDirectReqId(JsonNode root, ProcessEntity.Step step) {
        return switch (step) {
            case FIN_MESSAGE_25 -> textAt(root, "/Object/Id/Id");
            case FIN_INSTRUCTION_104 -> extractParentRelatedInstructionId(root);
            default -> null;
        };
    }

    private String extractParentRelatedInstructionId(JsonNode root) {
        JsonNode related = firstPresent(
                root.at("/Object/RelatedInstructions"),
                root.at("/RelatedInstructions")
        );
        if (related == null) {
            return null;
        }
        if (related.isObject()) {
            String fromParentKey = textAt(related, "/Parent/Instruction/Id/Id");
            if (fromParentKey != null) {
                return fromParentKey;
            }
            if (isParentRelation(related)) {
                return textAt(related, "/Instruction/Id/Id");
            }
            return null;
        }
        if (related.isArray()) {
            for (JsonNode item : related) {
                if (item != null && item.isObject() && isParentRelation(item)) {
                    String id = textAt(item, "/Instruction/Id/Id");
                    if (id != null) {
                        return id;
                    }
                }
            }
        }
        return null;
    }

    private boolean isParentRelation(JsonNode node) {
        return "Parent".equals(node.path("Type").asText(null))
                || "Parent".equals(node.path("Relation").asText(null))
                || "Parent".equals(node.path("Kind").asText(null))
                || "Parent".equals(node.path("Role").asText(null));
    }

    private String extractStreamingStatus(JsonNode root) {
        String status = textAt(root, "/Object/Status");
        if (status != null) {
            return status;
        }
        if (root.hasNonNull("storage_event")) {
            return root.get("storage_event").asText();
        }
        return null;
    }

    private static JsonNode firstPresent(JsonNode a, JsonNode b) {
        if (a != null && !a.isMissingNode() && !a.isNull()) {
            return a;
        }
        if (b != null && !b.isMissingNode() && !b.isNull()) {
            return b;
        }
        return null;
    }

    private static String firstNonBlank(String a, String b) {
        if (a != null && !a.isBlank()) {
            return a;
        }
        if (b != null && !b.isBlank()) {
            return b;
        }
        return null;
    }

    private static String textAt(JsonNode root, String pointer) {
        if (root == null) {
            return null;
        }
        JsonNode node = root.at(pointer);
        if (node.isMissingNode() || node.isNull() || !node.isTextual() || node.asText().isBlank()) {
            return null;
        }
        return node.asText();
    }
}
