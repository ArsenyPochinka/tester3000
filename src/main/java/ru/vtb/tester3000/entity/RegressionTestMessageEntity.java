package ru.vtb.tester3000.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "regression_test_message")
public class RegressionTestMessageEntity {

    @Id
    private UUID id;

    @Column(name = "run_id", nullable = false)
    private UUID runId;

    @Column(name = "test_name", nullable = false, length = 255)
    private String testName;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "auth_message", columnDefinition = "jsonb")
    private String authMessage;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "clearing_message", columnDefinition = "jsonb")
    private String clearingMessage;

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

    public String getTestName() {
        return testName;
    }

    public void setTestName(String testName) {
        this.testName = testName;
    }

    public String getAuthMessage() {
        return authMessage;
    }

    public void setAuthMessage(String authMessage) {
        this.authMessage = authMessage;
    }

    public String getClearingMessage() {
        return clearingMessage;
    }

    public void setClearingMessage(String clearingMessage) {
        this.clearingMessage = clearingMessage;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
