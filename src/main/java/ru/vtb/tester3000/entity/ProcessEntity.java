package ru.vtb.tester3000.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "process")
public class ProcessEntity {

    public enum Step {
        AUTH_SEND("AUTH_SEND"),
        CLEARING_SEND("CLEARING_SEND"),
        FIN_MESSAGE_25("25_FIN_MESSAGE"),
        FIN_INSTRUCTION_104("104_FIN_INSTRUCTION"),
        FIN_TRANSACTION_39("39_FIN_TRANSACTION");

        private final String dbValue;

        Step(String dbValue) {
            this.dbValue = dbValue;
        }

        public String getDbValue() {
            return dbValue;
        }

        public static Step fromDbValue(String value) {
            for (Step step : values()) {
                if (step.dbValue.equals(value)) {
                    return step;
                }
            }
            throw new IllegalArgumentException("Unknown step: " + value);
        }
    }

    /**
     * Авторизационное (AUTH) или клиринговое (CLR) событие.
     */
    public enum Type {
        AUTH,
        CLR
    }

    /**
     * Статусы шагов отправки (SUCCESS/ERROR) и статусы стриминга из outbox.
     */
    public enum Status {
        SUCCESS,
        ERROR,
        New,
        Received,
        Processing,
        Approved,
        Rejected,
        Executed,
        SystemError,
        Error,
        Canceled,
        Completed,
        Pending,
        Accepted
    }

    @Id
    private UUID id;

    @Column(name = "run_id", nullable = false)
    private UUID runId;

    @Column(name = "test_message_id", nullable = false)
    private UUID testMessageId;

    @Column(name = "test_name", nullable = false, length = 255)
    private String testName;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 32)
    private Type type;

    @Column(name = "req_id", nullable = false, length = 255)
    private String reqId;

    @Convert(converter = ProcessStepConverter.class)
    @Column(name = "step", nullable = false, length = 64)
    private Step step;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 64)
    private Status status;

    @Column(name = "description", nullable = false, columnDefinition = "text")
    private String description;

    @Column(name = "result", columnDefinition = "text")
    private String result;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getRunId() {
        return runId;
    }

    public void setRunId(UUID runId) {
        this.runId = runId;
    }

    public UUID getTestMessageId() {
        return testMessageId;
    }

    public void setTestMessageId(UUID testMessageId) {
        this.testMessageId = testMessageId;
    }

    public String getTestName() {
        return testName;
    }

    public void setTestName(String testName) {
        this.testName = testName;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public String getReqId() {
        return reqId;
    }

    public void setReqId(String reqId) {
        this.reqId = reqId;
    }

    public Step getStep() {
        return step;
    }

    public void setStep(Step step) {
        this.step = step;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
