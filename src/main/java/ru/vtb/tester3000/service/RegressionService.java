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
import java.util.List;
import java.util.UUID;

@Service
public class RegressionService {

    private final TestMessageCatalog catalog;
    private final MessageMapper messageMapper;
    private final LinkageKeyGenerator linkageKeyGenerator;
    private final RegressionTestMessageRepository messageRepository;
    private final RegressionDispatchService dispatchService;
    private final RegressionRunLog runLog;
    private final ObjectMapper objectMapper;
    private final Clock clock;

    public RegressionService(
            TestMessageCatalog catalog,
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
        List<TestMessageCatalog.TestCase> tests = catalog.listTests(request.getTests());

        UUID runId = UUID.randomUUID();
        Instant createdAt = Instant.now(clock);
        runLog.info(runId, "Старт прогона. Источник: test_messages_from_25. Тестов: " + tests.size()
                + " [" + String.join(", ", tests.stream().map(TestMessageCatalog.TestCase::testCode).toList()) + "]");

        for (TestMessageCatalog.TestCase test : tests) {
            LinkageKeyGenerator.LinkageKey linkage = linkageKeyGenerator.generate();
            JsonNode authMapped = messageMapper.mapAuthorization(test.auth(), card, linkage);
            String authReqId = authMapped.path("rId").asText();

            JsonNode clearingMapped = null;
            String clearingReqId = null;
            if (test.clearing().isPresent()) {
                clearingMapped = messageMapper.mapClearing(test.clearing().get(), card, linkage);
                clearingReqId = clearingMapped.path("rId").asText();
            }

            runLog.info(runId, authReqId, "Сформирован тест «" + test.testCode() + "» ("
                    + test.testDescription() + "), linkage.key=" + linkage.key()
                    + (clearingReqId == null ? ", без клиринга" : ", clearing.reqId=" + clearingReqId));
            runLog.info(runId, authReqId, "Подготовленный auth JSON: " + writeJson(authMapped));
            if (clearingMapped != null) {
                runLog.info(runId, clearingReqId, "Подготовленный clearing JSON: " + writeJson(clearingMapped));
            }

            RegressionTestMessageEntity entity = new RegressionTestMessageEntity();
            entity.setId(UUID.randomUUID());
            entity.setRunId(runId);
            entity.setTestName(test.testCode());
            entity.setAuthMessage(writeJson(authMapped));
            entity.setClearingMessage(clearingMapped == null ? null : writeJson(clearingMapped));
            entity.setCreatedAt(createdAt);
            messageRepository.save(entity);
            runLog.info(runId, authReqId, "Сохранено в regression_test_message id=" + entity.getId());

            dispatchService.dispatch(runId, entity.getId(), test.testCode(), authMapped, clearingMapped);
        }

        runLog.info(runId, "Синхронная часть завершена, запуск принят. Тестов: " + tests.size());

        RegressionRunResponse response = new RegressionRunResponse();
        response.setRunId(runId);
        response.setCode(0);
        return response;
    }

    private String writeJson(JsonNode node) {
        try {
            return objectMapper.writeValueAsString(node);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize message", e);
        }
    }
}
