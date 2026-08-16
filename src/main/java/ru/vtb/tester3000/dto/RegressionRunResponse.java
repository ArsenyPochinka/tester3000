package ru.vtb.tester3000.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(name = "RegressionRunResponse", description = "Результат приёма запуска регресса")
public class RegressionRunResponse {

    @Schema(description = "Идентификатор прогона")
    private UUID runId;

    @Schema(description = "Код выполнения запуска: 0 — запуск принят", example = "0")
    private int code;

    public UUID getRunId() {
        return runId;
    }

    public void setRunId(UUID runId) {
        this.runId = runId;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }
}
