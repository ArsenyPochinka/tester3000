package ru.vtb.tester3000.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.examples.Example;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.IntegerSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Configuration
public class OpenApiConfig {

    @Bean
    OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Регресс")
                        .description("Сквозное тестирование авторизаций")
                        .version("1.0.0"));
    }

    @Bean
    OpenApiCustomizer regressionExampleCustomizer(TestCodesSnapshot testCodesSnapshot) {
        return openApi -> {
            List<String> defaultTests = new ArrayList<>(testCodesSnapshot.getCodes());

            Schema<?> cardAuth = new Schema<>().type("object")
                    .addProperty("presence", new Schema<>().type("boolean").example(true));
            Schema<?> cardEmv = new Schema<>().type("object")
                    .addProperty("mbr", new Schema<>().type("integer").example(90));
            Schema<?> card = new Schema<>().type("object")
                    .addProperty("auth", cardAuth)
                    .addProperty("plasticId", new Schema<>().type("string")
                            .example("15c812a5-edc4-4dd2-a842-6fd53be44369"))
                    .addProperty("cardId", new Schema<>().type("string")
                            .example("b8c89a63-3a79-4e7e-a606-8273cc8e5e4a"))
                    .addProperty("expDate", new Schema<>().type("string")
                            .example("2030-01-01T00:00:00.000"))
                    .addProperty("emv", cardEmv);

            ArraySchema testsSchema = new ArraySchema();
            testsSchema.setItems(new StringSchema());
            testsSchema.setExample(defaultTests);
            testsSchema.setDefault(defaultTests);
            testsSchema.setDescription("Коды тестов из БД (снимок на старте приложения)");

            Schema<?> request = new Schema<>().type("object")
                    .addProperty("card", card)
                    .addProperty("tests", testsSchema);

            Schema<?> response = new Schema<>().type("object")
                    .addProperty("runId", new StringSchema().format("uuid"))
                    .addProperty("code", new IntegerSchema().example(0)
                            .description("0 — запуск принят"));

            Map<String, Object> exampleValue = new LinkedHashMap<>();
            Map<String, Object> cardValue = new LinkedHashMap<>();
            cardValue.put("auth", Map.of("presence", true));
            cardValue.put("plasticId", "15c812a5-edc4-4dd2-a842-6fd53be44369");
            cardValue.put("cardId", "b8c89a63-3a79-4e7e-a606-8273cc8e5e4a");
            cardValue.put("expDate", "2030-01-01T00:00:00.000");
            cardValue.put("emv", Map.of("mbr", 90));
            exampleValue.put("card", cardValue);
            exampleValue.put("tests", defaultTests);

            openApi.getComponents().addSchemas("RegressionRunRequest", request);
            openApi.getComponents().addSchemas("RegressionRunResponse", response);
            openApi.getComponents().addExamples("RegressionRunRequestExample",
                    new Example().summary("Карта и перечень тестов").value(exampleValue));

            if (openApi.getPaths() != null) {
                openApi.getPaths().forEach((path, item) -> {
                    if (item.getPost() != null && item.getPost().getRequestBody() != null
                            && item.getPost().getRequestBody().getContent() != null) {
                        item.getPost().getRequestBody().getContent().forEach((media, mediaType) -> {
                            mediaType.setExample(exampleValue);
                        });
                    }
                });
            }
        };
    }
}
