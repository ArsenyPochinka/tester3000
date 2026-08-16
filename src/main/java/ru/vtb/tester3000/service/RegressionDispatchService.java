package ru.vtb.tester3000.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import ru.vtb.tester3000.client.ProductAuthorizationClient;
import ru.vtb.tester3000.config.TesterProperties;
import ru.vtb.tester3000.entity.ProcessEntity;
import ru.vtb.tester3000.kafka.ClearingKafkaPublisher;
import ru.vtb.tester3000.kafka.FinOutboxStubPublisher;
import ru.vtb.tester3000.logging.RegressionRunLog;
import ru.vtb.tester3000.repository.ProcessRepository;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class RegressionDispatchService {

    private final ProductAuthorizationClient productAuthorizationClient;
    private final ClearingKafkaPublisher clearingKafkaPublisher;
    private final ObjectProvider<FinOutboxStubPublisher> finOutboxStubPublisher;
    private final ProcessRepository processRepository;
    private final TesterProperties properties;
    private final ObjectMapper objectMapper;
    private final RegressionRunLog runLog;
    private final Clock clock;

    public RegressionDispatchService(
            ProductAuthorizationClient productAuthorizationClient,
            ClearingKafkaPublisher clearingKafkaPublisher,
            ObjectProvider<FinOutboxStubPublisher> finOutboxStubPublisher,
            ProcessRepository processRepository,
            TesterProperties properties,
            ObjectMapper objectMapper,
            RegressionRunLog runLog,
            Clock clock
    ) {
        this.productAuthorizationClient = productAuthorizationClient;
        this.clearingKafkaPublisher = clearingKafkaPublisher;
        this.finOutboxStubPublisher = finOutboxStubPublisher;
        this.processRepository = processRepository;
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.runLog = runLog;
        this.clock = clock;
    }

    @Async("regressionExecutor")
    public void dispatch(UUID runId, UUID testMessageId, String testName, JsonNode authMapped, JsonNode clearingMapped) {
        String authReqId = authMapped.path("rId").asText();
        try {
            runLog.info(runId, authReqId, "Фоновая обработка теста «" + testName
                    + "» (testMessageId=" + testMessageId + ")");
            boolean authOk = sendAuth(runId, testMessageId, testName, authMapped);
            if (authOk) {
                publishOutboxStub(runId, authReqId, authMapped, "авторизация");
            }
            if (authOk && clearingMapped != null) {
                String clearingReqId = clearingMapped.path("rId").asText();
                runLog.info(runId, clearingReqId, "Ожидание " + properties.getClearingDelayMs()
                        + " мс перед отправкой клиринга");
                sleepClearingDelay();
                sendClearing(runId, testMessageId, testName, clearingMapped);
                publishOutboxStub(runId, clearingReqId, clearingMapped, "клиринг");
            } else if (!authOk) {
                runLog.info(runId, authReqId, "Клиринг пропущен: авторизация завершилась с ошибкой");
            }
            runLog.info(runId, authReqId, "Фоновая обработка теста «" + testName + "» завершена");
        } catch (Exception ex) {
            runLog.error(runId, authReqId, "Сбой фоновой обработки теста «" + testName + "»", ex);
        }
    }

    private boolean sendAuth(UUID runId, UUID testMessageId, String testName, JsonNode authMapped) {
        String reqId = authMapped.path("rId").asText();
        String requestJson = writeJson(authMapped);
        String url = properties.getM210().requestUrl();
        runLog.sent(runId, reqId, "m210 POST " + url, requestJson);

        ProductAuthorizationClient.SendResult sendResult = productAuthorizationClient.send(authMapped);
        runLog.received(runId, reqId, "m210 HTTP " + sendResult.statusCode(),
                nullToEmpty(sendResult.body()));

        ProcessEntity process = newProcess(
                runId, testMessageId, testName, ProcessEntity.Type.AUTH,
                ProcessEntity.Step.AUTH_SEND, reqId
        );
        if (sendResult.success()) {
            process.setStatus(ProcessEntity.Status.SUCCESS);
            process.setDescription("Отправка авторизационного сообщения в m210 (продуктовая авторизация)");
            process.setResult(requestJson);
            runLog.info(runId, reqId, "AUTH_SEND успешен");
        } else {
            process.setStatus(ProcessEntity.Status.ERROR);
            process.setDescription("Ошибка отправки авторизационного сообщения в m210");
            process.setResult("HTTP " + sendResult.statusCode() + ": " + nullToEmpty(sendResult.body()));
            runLog.info(runId, reqId, "AUTH_SEND ошибка");
        }
        processRepository.save(process);
        return sendResult.success();
    }

    private void sendClearing(UUID runId, UUID testMessageId, String testName, JsonNode clearingMapped) {
        String reqId = clearingMapped.path("rId").asText();
        String requestJson = writeJson(clearingMapped);
        String topic = properties.getKafka().getClearingTopic();
        runLog.sent(runId, reqId, "Kafka topic " + topic, requestJson);

        ClearingKafkaPublisher.PublishResult publishResult = clearingKafkaPublisher.publish(clearingMapped);
        ProcessEntity process = newProcess(
                runId, testMessageId, testName, ProcessEntity.Type.CLR,
                ProcessEntity.Step.CLEARING_SEND, reqId
        );
        if (publishResult.success()) {
            process.setStatus(ProcessEntity.Status.SUCCESS);
            process.setDescription("Публикация клирингового сообщения в Kafka (m095, топик " + topic + ")");
            process.setResult(requestJson);
            runLog.received(runId, reqId, "Kafka ack " + topic, publishResult.detail());
            runLog.info(runId, reqId, "CLEARING_SEND успешен");
        } else {
            process.setStatus(ProcessEntity.Status.ERROR);
            process.setDescription("Ошибка публикации клирингового сообщения в Kafka (m095)");
            process.setResult(publishResult.detail());
            runLog.info(runId, reqId, "CLEARING_SEND ошибка: " + publishResult.detail());
        }
        processRepository.save(process);
    }

    private void publishOutboxStub(UUID runId, String reqId, JsonNode sourceMessage, String label) {
        FinOutboxStubPublisher stub = finOutboxStubPublisher.getIfAvailable();
        if (stub == null) {
            runLog.info(runId, reqId, "Заглушка outbox выключена — события для «" + label + "» не публикуются");
            return;
        }
        runLog.info(runId, reqId, "Заглушка outbox: по 2 сообщения со случайным статусом в 3 топика («"
                + label + "»)");
        List<FinOutboxStubPublisher.PublishResult> results = stub.publishRandomPairToAllTopics(reqId, sourceMessage);
        for (FinOutboxStubPublisher.PublishResult result : results) {
            if (result.success()) {
                runLog.sent(runId, reqId, "stub → " + result.topic() + " status=" + result.status(),
                        result.payload());
            } else {
                runLog.info(runId, reqId, "Ошибка stub status=" + result.status()
                        + " topic=" + result.topic() + ": " + result.detail());
            }
        }
    }

    private ProcessEntity newProcess(
            UUID runId,
            UUID testMessageId,
            String testName,
            ProcessEntity.Type type,
            ProcessEntity.Step step,
            String reqId
    ) {
        ProcessEntity process = new ProcessEntity();
        process.setId(UUID.randomUUID());
        process.setRunId(runId);
        process.setTestMessageId(testMessageId);
        process.setTestName(testName);
        process.setType(type);
        process.setReqId(reqId);
        process.setStep(step);
        process.setCreatedAt(Instant.now(clock));
        return process;
    }

    private void sleepClearingDelay() {
        try {
            Thread.sleep(properties.getClearingDelayMs());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while waiting before clearing publish", e);
        }
    }

    private String writeJson(JsonNode node) {
        try {
            return objectMapper.writeValueAsString(node);
        } catch (Exception e) {
            return String.valueOf(node);
        }
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}
