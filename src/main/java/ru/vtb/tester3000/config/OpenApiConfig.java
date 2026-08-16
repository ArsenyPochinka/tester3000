package ru.vtb.tester3000.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.examples.Example;
import io.swagger.v3.oas.models.info.Info;
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

    private static final String RUN_PATH = "/api/v1/regression/run";

    @Bean
    OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Регресс")
                        .description("Сквозное тестирование авторизаций")
                        .version("1.0.0"));
    }

    @Bean
    OpenApiCustomizer regressionExampleCustomizer(RegressionCasesSnapshot snapshot) {
        return openApi -> {
            List<String> codes = new ArrayList<>(snapshot.getCodes());
            List<String> tags = new ArrayList<>(snapshot.getRegressionTags());

            Schema<?> requestSchema = openApi.getComponents() == null
                    ? null
                    : openApi.getComponents().getSchemas().get("RegressionRunRequest");
            if (requestSchema != null && requestSchema.getProperties() != null) {
                enrichTestsProperty(requestSchema, codes);
                enrichTagProperty(requestSchema, tags);
            }

            Map<String, Object> byTag = new LinkedHashMap<>();
            byTag.put("card", sampleCard());
            if (!tags.isEmpty()) {
                byTag.put("regressionTag", tags.getFirst());
            }

            Map<String, Object> byCodes = new LinkedHashMap<>();
            byCodes.put("card", sampleCard());
            byCodes.put("tests", codes);

            if (openApi.getPaths() != null && openApi.getPaths().get(RUN_PATH) != null
                    && openApi.getPaths().get(RUN_PATH).getPost() != null
                    && openApi.getPaths().get(RUN_PATH).getPost().getRequestBody() != null
                    && openApi.getPaths().get(RUN_PATH).getPost().getRequestBody().getContent() != null) {
                openApi.getPaths().get(RUN_PATH).getPost().getRequestBody().getContent()
                        .forEach((media, mediaType) -> {
                            mediaType.addExamples("byTag", new Example()
                                    .summary("По regressionTag")
                                    .value(byTag));
                            mediaType.addExamples("byCodes", new Example()
                                    .summary("По списку test_code")
                                    .value(byCodes));
                            mediaType.setExample(tags.isEmpty() ? byCodes : byTag);
                        });
            }
        };
    }

    @SuppressWarnings("rawtypes")
    private static void enrichTestsProperty(Schema<?> requestSchema, List<String> codes) {
        Object raw = requestSchema.getProperties().get("tests");
        if (!(raw instanceof Schema tests)) {
            return;
        }
        tests.setExample(codes);
        tests.setDescription("Коды кейсов из regression_cases (снимок на старте приложения)");
    }

    @SuppressWarnings("rawtypes")
    private static void enrichTagProperty(Schema<?> requestSchema, List<String> tags) {
        Object raw = requestSchema.getProperties().get("regressionTag");
        if (!(raw instanceof Schema tag)) {
            return;
        }
        tag.setDescription("Тег регресса. Доступные на старте: " + tags);
        if (!tags.isEmpty()) {
            tag.setExample(tags.getFirst());
            if (tag instanceof StringSchema stringSchema) {
                stringSchema.setEnum(new ArrayList<>(tags));
            } else {
                tag.setEnum(new ArrayList<>(tags));
            }
        }
    }

    private static Map<String, Object> sampleCard() {
        Map<String, Object> card = new LinkedHashMap<>();
        card.put("auth", Map.of("presence", true));
        card.put("plasticId", "15c812a5-edc4-4dd2-a842-6fd53be44369");
        card.put("cardId", "b8c89a63-3a79-4e7e-a606-8273cc8e5e4a");
        card.put("expDate", "2030-01-01T00:00:00.000");
        card.put("emv", Map.of("mbr", 90));
        return card;
    }
}
