package ru.vtb.tester3000.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.ArrayList;
import java.util.List;

@Schema(name = "RegressionRunRequest", description = "Параметры запуска регресса")
public class RegressionRunRequest {

    @NotNull
    @Valid
    private CardParam card;

    @NotEmpty
    @Schema(description = "Коды тестов из test_messages_from_25. По умолчанию — все коды на момент старта приложения.")
    private List<String> tests = new ArrayList<>();

    public CardParam getCard() {
        return card;
    }

    public void setCard(CardParam card) {
        this.card = card;
    }

    public List<String> getTests() {
        return tests;
    }

    public void setTests(List<String> tests) {
        this.tests = tests;
    }
}
