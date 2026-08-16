package ru.vtb.tester3000.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
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
import java.util.concurrent.Executor;

@Service
public class RegressionDispatchService {

    public enum MessageKind {
        AUTH,
        CLR
    }

    public record OutboundMessage(MessageKind kind, String label, String reqId, JsonNode payload) {
    }

    public record CaseJob(UUID runId, UUID testMessageId, String testName, List<OutboundMessage> messages) {
    }

    private final ProductAuthorizationClient productAuthorizationClient;
    private final ClearingKafkaPublisher clearingKafkaPublisher;
    private final ObjectProvider<FinOutboxStubPublisher> finOutboxStubPublisher;
    private final ProcessRepository processRepository;
    private final TesterProperties properties;
    private final ObjectMapper objectMapper;
    private final RegressionRunLog runLog;
    private final Clock clock;
    private final Executor regressionExecutor;

    public RegressionDispatchService(
            ProductAuthorizationClient productAuthorizationClient,
            ClearingKafkaPublisher clearingKafkaPublisher,
            ObjectProvider<FinOutboxStubPublisher> finOutboxStubPublisher,
            ProcessRepository processRepository,
            TesterProperties properties,
            ObjectMapper objectMapper,
            RegressionRunLog runLog,
            Clock clock,
            @Qualifier("regressionExecutor") Executor regressionExecutor
    ) {
        this.productAuthorizationClient = productAuthorizationClient;
        this.clearingKafkaPublisher = clearingKafkaPublisher;
        this.finOutboxStubPublisher = finOutboxStubPublisher;
        this.processRepository = processRepository;
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.runLog = runLog;
        this.clock = clock;
        this.regressionExecutor = regressionExecutor;
    }

    /**
     * Первые {@code parallel-tests} кейсов стартуют сразу, остальные — с интервалом
     * {@code test-start-interval-ms}. Одновременно выполняется не больше {@code parallel-tests}.
     */
    @Async("regressionScheduler")
    public void schedule(List<CaseJob> jobs) {
        if (jobs.isEmpty()) {
            return;
        }
        int parallel = Math.max(1, properties.getParallelTests());
        long intervalMs = Math.max(0, properties.getTestStartIntervalMs());
        UUID runId = jobs.getFirst().runId();
        runLog.info(runId, "Планирование кейсов: всего=" + jobs.size()
                + ", parallel=" + parallel + ", startIntervalMs=" + intervalMs);

        for (int i = 0; i < jobs.size(); i++) {
            if (i >= parallel && intervalMs > 0) {
                runLog.info(runId, "Интервал " + intervalMs + " мс перед стартом кейса «"
                        + jobs.get(i).testName() + "»");
                sleep(intervalMs);
            }
            CaseJob job = jobs.get(i);
            regressionExecutor.execute(() -> runCase(job));
        }
    }

    private void runCase(CaseJob job) {
        UUID runId = job.runId();
        String firstReqId = job.messages().isEmpty() ? "-" : job.messages().getFirst().reqId();
        try {
            runLog.info(runId, firstReqId, "Фоновая обработка кейса «" + job.testName()
                    + "» (testMessageId=" + job.testMessageId() + "), сообщений=" + job.messages().size());
            for (int i = 0; i < job.messages().size(); i++) {
                if (i > 0) {
                    runLog.info(runId, job.messages().get(i).reqId(), "Ожидание "
                            + properties.getMessageDelayMs() + " мс перед следующим сообщением");
                    sleep(properties.getMessageDelayMs());
                }
                OutboundMessage message = job.messages().get(i);
                boolean ok = message.kind() == MessageKind.AUTH
                        ? sendAuth(runId, job.testMessageId(), job.testName(), message)
                        : sendClearing(runId, job.testMessageId(), job.testName(), message);
                if (ok) {
                    publishOutboxStub(runId, message.reqId(), message.payload(), message.label());
                    continue;
                }
                if (message.kind() == MessageKind.AUTH) {
                    int skipped = job.messages().size() - i - 1;
                    runLog.info(runId, message.reqId(), "AUTH ошибка (" + message.label()
                            + ") - остальные сообщения кейса пропущены (" + skipped + ")");
                    break;
                }
            }
            runLog.info(runId, firstReqId, "Фоновая обработка кейса «" + job.testName() + "» завершена");
        } catch (Exception ex) {
            runLog.error(runId, firstReqId, "Сбой фоновой обработки кейса «" + job.testName() + "»", ex);
        }
    }

    private boolean sendAuth(UUID runId, UUID testMessageId, String testName, OutboundMessage message) {
        String reqId = message.reqId();
        String requestJson = writeJson(message.payload());
        String url = properties.getM210().requestUrl();
        runLog.sent(runId, reqId, "m210 POST " + url + " (" + message.label() + ")", requestJson);

        ProductAuthorizationClient.SendResult sendResult = productAuthorizationClient.send(message.payload());
        runLog.received(runId, reqId, "m210 HTTP " + sendResult.statusCode() + " (" + message.label() + ")",
                nullToEmpty(sendResult.body()));

        return saveSendProcess(
                runId, testMessageId, testName, message,
                ProcessEntity.Type.AUTH, ProcessEntity.Step.AUTH_SEND,
                sendResult.success(),
                "Отправка auth («" + message.label() + "») в m210",
                "Ошибка отправки auth («" + message.label() + "») в m210",
                requestJson,
                "HTTP " + sendResult.statusCode() + ": " + nullToEmpty(sendResult.body()),
                "AUTH_SEND"
        );
    }

    private boolean sendClearing(UUID runId, UUID testMessageId, String testName, OutboundMessage message) {
        String reqId = message.reqId();
        String requestJson = writeJson(message.payload());
        String topic = properties.getKafka().getClearingTopic();
        runLog.sent(runId, reqId, "Kafka topic " + topic + " (" + message.label() + ")", requestJson);

        ClearingKafkaPublisher.PublishResult publishResult = clearingKafkaPublisher.publish(message.payload());
        if (publishResult.success()) {
            runLog.received(runId, reqId, "Kafka ack " + topic + " (" + message.label() + ")", publishResult.detail());
        }

        return saveSendProcess(
                runId, testMessageId, testName, message,
                ProcessEntity.Type.CLR, ProcessEntity.Step.CLEARING_SEND,
                publishResult.success(),
                "Публикация clr («" + message.label() + "») в Kafka (" + topic + ")",
                "Ошибка публикации clr («" + message.label() + "») в Kafka",
                requestJson,
                publishResult.detail(),
                "CLEARING_SEND"
        );
    }

    private boolean saveSendProcess(
            UUID runId,
            UUID testMessageId,
            String testName,
            OutboundMessage message,
            ProcessEntity.Type type,
            ProcessEntity.Step step,
            boolean success,
            String successDescription,
            String errorDescription,
            String successResult,
            String errorResult,
            String stepLogPrefix
    ) {
        ProcessEntity process = new ProcessEntity();
        process.setId(UUID.randomUUID());
        process.setRunId(runId);
        process.setTestMessageId(testMessageId);
        process.setTestName(testName);
        process.setType(type);
        process.setReqId(message.reqId());
        process.setStep(step);
        process.setCreatedAt(Instant.now(clock));
        if (success) {
            process.setStatus(ProcessEntity.Status.SUCCESS);
            process.setDescription(successDescription);
            process.setResult(successResult);
            runLog.info(runId, message.reqId(), stepLogPrefix + " успешен (" + message.label() + ")");
        } else {
            process.setStatus(ProcessEntity.Status.ERROR);
            process.setDescription(errorDescription);
            process.setResult(errorResult);
            runLog.info(runId, message.reqId(), stepLogPrefix + " ошибка (" + message.label() + ")"
                    + (errorResult == null || errorResult.isBlank() ? "" : ": " + errorResult));
        }
        processRepository.save(process);
        return success;
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

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while waiting", e);
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
