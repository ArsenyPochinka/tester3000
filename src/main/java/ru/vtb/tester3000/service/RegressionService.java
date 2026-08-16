package ru.vtb.tester3000.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import ru.vtb.tester3000.dto.CardParam;
import ru.vtb.tester3000.dto.RegressionRunRequest;
import ru.vtb.tester3000.dto.RegressionRunResponse;
import ru.vtb.tester3000.entity.RegressionTestMessageEntity;
import ru.vtb.tester3000.logging.RegressionRunLog;
import ru.vtb.tester3000.mapper.LinkageKeyGenerator;
import ru.vtb.tester3000.mapper.MessageMapper;
import ru.vtb.tester3000.repository.RegressionTestMessageRepository;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class RegressionService {

    private static final List<SlotSpec> SLOT_SPECS = List.of(
            new SlotSpec("auth", "clr"),
            new SlotSpec("auth_add_1", "clr_add_1"),
            new SlotSpec("auth_add_2", "clr_add_2")
    );

    private final RegressionCaseCatalog catalog;
    private final MessageMapper messageMapper;
    private final LinkageKeyGenerator linkageKeyGenerator;
    private final RegressionTestMessageRepository messageRepository;
    private final RegressionDispatchService dispatchService;
    private final RegressionRunLog runLog;
    private final ObjectMapper objectMapper;
    private final Clock clock;

    public RegressionService(
            RegressionCaseCatalog catalog,
            MessageMapper messageMapper,
            LinkageKeyGenerator linkageKeyGenerator,
            RegressionTestMessageRepository messageRepository,
            RegressionDispatchService dispatchService,
            RegressionRunLog runLog,
            ObjectMapper objectMapper,
            Clock clock
    ) {
        this.catalog = catalog;
        this.messageMapper = messageMapper;
        this.linkageKeyGenerator = linkageKeyGenerator;
        this.messageRepository = messageRepository;
        this.dispatchService = dispatchService;
        this.runLog = runLog;
        this.objectMapper = objectMapper;
        this.clock = clock;
    }

    public RegressionRunResponse run(RegressionRunRequest request) {
        CardParam card = request.getCard();
        List<RegressionCaseCatalog.TestCase> cases = catalog.listCases(request.getTests(), request.getRegressionTag());

        UUID runId = UUID.randomUUID();
        Instant createdAt = Instant.now(clock);
        String codes = String.join(", ", cases.stream().map(RegressionCaseCatalog.TestCase::testCode).toList());
        String tagPart = blankToNull(request.getRegressionTag()) == null
                ? ""
                : ", tag=" + request.getRegressionTag().trim();
        runLog.info(runId, "Старт прогона. Источник: regression_cases. Кейсов: " + cases.size()
                + " [" + codes + "]" + tagPart);

        List<RegressionDispatchService.CaseJob> jobs = new ArrayList<>(cases.size());
        for (RegressionCaseCatalog.TestCase test : cases) {
            List<MappedSlot> slots = List.of(
                    mapSlot(test.primary(), card),
                    mapSlot(test.add1(), card),
                    mapSlot(test.add2(), card)
            );
            List<RegressionDispatchService.OutboundMessage> outbound = buildOutbound(slots);
            String firstReqId = outbound.isEmpty() ? "-" : outbound.getFirst().reqId();

            runLog.info(runId, firstReqId, "Сформирован кейс «" + test.testCode() + "» ("
                    + test.testDescription() + "), сообщений: " + outbound.size()
                    + (test.regressionTag() == null ? "" : ", tag=" + test.regressionTag()));
            for (RegressionDispatchService.OutboundMessage msg : outbound) {
                runLog.info(runId, msg.reqId(), "Подготовленный " + msg.label() + " JSON: " + writeJson(msg.payload()));
            }

            MappedSlot primary = slots.getFirst();
            RegressionTestMessageEntity entity = new RegressionTestMessageEntity();
            entity.setId(UUID.randomUUID());
            entity.setRunId(runId);
            entity.setTestName(test.testCode());
            entity.setAuthMessage(primary.auth() == null ? null : writeJson(primary.auth()));
            entity.setClearingMessage(primary.clearing() == null ? null : writeJson(primary.clearing()));
            entity.setCreatedAt(createdAt);
            messageRepository.save(entity);
            runLog.info(runId, firstReqId, "Сохранено в regression_test_message id=" + entity.getId());

            jobs.add(new RegressionDispatchService.CaseJob(
                    runId, entity.getId(), test.testCode(), List.copyOf(outbound)));
        }

        dispatchService.schedule(jobs);
        runLog.info(runId, "Синхронная часть завершена, кейсы поставлены в очередь: " + jobs.size());

        RegressionRunResponse response = new RegressionRunResponse();
        response.setRunId(runId);
        response.setCode(0);
        return response;
    }

    private List<RegressionDispatchService.OutboundMessage> buildOutbound(List<MappedSlot> slots) {
        List<RegressionDispatchService.OutboundMessage> outbound = new ArrayList<>();
        for (int i = 0; i < SLOT_SPECS.size(); i++) {
            SlotSpec spec = SLOT_SPECS.get(i);
            MappedSlot slot = slots.get(i);
            if (slot.auth() != null) {
                outbound.add(new RegressionDispatchService.OutboundMessage(
                        RegressionDispatchService.MessageKind.AUTH,
                        spec.authLabel(),
                        slot.auth().path("rId").asText(),
                        slot.auth()
                ));
            }
            if (slot.clearing() != null) {
                outbound.add(new RegressionDispatchService.OutboundMessage(
                        RegressionDispatchService.MessageKind.CLR,
                        spec.clrLabel(),
                        slot.clearing().path("rId").asText(),
                        slot.clearing()
                ));
            }
        }
        return outbound;
    }

    private MappedSlot mapSlot(RegressionCaseCatalog.MessageSlot slot, CardParam card) {
        if (slot.isEmpty()) {
            return new MappedSlot(null, null);
        }
        LinkageKeyGenerator.LinkageKey linkage = linkageKeyGenerator.generate();
        JsonNode auth = slot.auth() == null ? null : messageMapper.mapAuthorization(slot.auth(), card, linkage);
        JsonNode clearing = slot.clearing() == null ? null : messageMapper.mapClearing(slot.clearing(), card, linkage);
        return new MappedSlot(auth, clearing);
    }

    private record SlotSpec(String authLabel, String clrLabel) {
    }

    private record MappedSlot(JsonNode auth, JsonNode clearing) {
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }

    private String writeJson(JsonNode node) {
        try {
            return objectMapper.writeValueAsString(node);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize message", e);
        }
    }
}
