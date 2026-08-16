package ru.vtb.tester3000.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.ArrayList;
import java.util.List;

@Schema(name = "RegressionRunRequest", description = "Параметры запуска регресса")
public class RegressionRunRequest {

    @NotNull
    @Valid
    private CardParam card;

    @Schema(description = "Коды кейсов из regression_cases. Можно комбинировать с regressionTag (тогда пересечение).")
    private List<String> tests = new ArrayList<>();

    @Schema(description = "Тег регресса из regression_cases. Можно указать вместо tests или вместе с ними.")
    private String regressionTag;

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

    public String getRegressionTag() {
        return regressionTag;
    }

    public void setRegressionTag(String regressionTag) {
        this.regressionTag = regressionTag;
    }
}
