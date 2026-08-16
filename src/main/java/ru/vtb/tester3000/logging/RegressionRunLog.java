package ru.vtb.tester3000.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Логирование прогона в stdout приложения со сквозными runId / reqId.
 */
@Component
public class RegressionRunLog {

    private static final Logger log = LoggerFactory.getLogger(RegressionRunLog.class);

    public void info(UUID runId, String message) {
        log.info("[runId={}] {}", runId, message);
    }

    public void info(UUID runId, String reqId, String message) {
        log.info("[runId={}] [reqId={}] {}", runId, nullToDash(reqId), message);
    }

    public void sent(UUID runId, String reqId, String channel, String payload) {
        log.info("[runId={}] [reqId={}] ОТПРАВЛЕНО → {}: {}",
                runId, nullToDash(reqId), channel, payload);
    }

    public void received(UUID runId, String reqId, String channel, String payload) {
        log.info("[runId={}] [reqId={}] ПОЛУЧЕНО ← {}: {}",
                runId, nullToDash(reqId), channel, payload);
    }

    public void error(UUID runId, String message, Throwable error) {
        if (error == null) {
            log.error("[runId={}] {}", runId, message);
        } else {
            log.error("[runId={}] {}", runId, message, error);
        }
    }

    public void error(UUID runId, String reqId, String message, Throwable error) {
        if (error == null) {
            log.error("[runId={}] [reqId={}] {}", runId, nullToDash(reqId), message);
        } else {
            log.error("[runId={}] [reqId={}] {}", runId, nullToDash(reqId), message, error);
        }
    }

    private static String nullToDash(String value) {
        return value == null || value.isBlank() ? "-" : value;
    }
}
