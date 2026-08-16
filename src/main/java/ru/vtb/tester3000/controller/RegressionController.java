package ru.vtb.tester3000.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.vtb.tester3000.dto.RegressionRunRequest;
import ru.vtb.tester3000.dto.RegressionRunResponse;
import ru.vtb.tester3000.service.RegressionReportService;
import ru.vtb.tester3000.service.RegressionService;

import java.util.UUID;

@RestController
@RequestMapping(path = "/api/v1/regression")
@Tag(name = "Регресс", description = "Сквозное тестирование авторизаций")
public class RegressionController {

    private final RegressionService regressionService;
    private final RegressionReportService reportService;

    public RegressionController(RegressionService regressionService, RegressionReportService reportService) {
        this.regressionService = regressionService;
        this.reportService = reportService;
    }

    @PostMapping(
            path = "/run",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @Operation(
            summary = "Запустить регресс",
            description = "Кейсы из regression_cases по tests и/или regressionTag. "
                    + "Параллельность и интервалы — из конфига (parallel-tests, test-start-interval-ms). "
                    + "Внутри кейса: слоты auth/clr (+ add_*), пауза message-delay-ms; "
                    + "ошибка auth останавливает оставшиеся сообщения кейса. "
                    + "Сразу возвращает runId."
    )
    public RegressionRunResponse run(@Valid @RequestBody RegressionRunRequest request) {
        return regressionService.run(request);
    }

    @GetMapping(path = "/report/{runId}", produces = MediaType.TEXT_HTML_VALUE)
    @Operation(
            summary = "HTML-отчёт по прогону",
            description = "HTML-отчёт: primary auth/clr и шаги process по run_id."
    )
    @ApiResponse(
            responseCode = "200",
            description = "HTML-отчёт",
            content = @Content(mediaType = MediaType.TEXT_HTML_VALUE)
    )
    @ApiResponse(responseCode = "404", description = "Прогон не найден")
    public ResponseEntity<String> report(
            @Parameter(description = "Идентификатор прогона", required = true)
            @PathVariable UUID runId
    ) {
        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_HTML)
                .body(reportService.buildHtmlReport(runId));
    }
}
