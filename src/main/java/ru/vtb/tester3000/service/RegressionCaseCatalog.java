package ru.vtb.tester3000.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import ru.vtb.tester3000.entity.RegressionCaseEntity;
import ru.vtb.tester3000.repository.RegressionCaseRepository;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class RegressionCaseCatalog {

    public record MessageSlot(JsonNode auth, JsonNode clearing) {
        public boolean isEmpty() {
            return auth == null && clearing == null;
        }
    }

    public record TestCase(
            String testCode,
            String testDescription,
            String regressionTag,
            MessageSlot primary,
            MessageSlot add1,
            MessageSlot add2
    ) {
    }

    private final ObjectMapper objectMapper;
    private final RegressionCaseRepository repository;

    public RegressionCaseCatalog(ObjectMapper objectMapper, RegressionCaseRepository repository) {
        this.objectMapper = objectMapper;
        this.repository = repository;
    }

    /**
     * Выбор кейсов по {@code regressionTag} и/или списку {@code testCodes}.
     * Если заданы оба — пересечение. Если ничего — ошибка.
     */
    public List<TestCase> listCases(List<String> testCodes, String regressionTag) {
        boolean hasTag = regressionTag != null && !regressionTag.isBlank();
        List<String> codes = normalizeCodes(testCodes);
        boolean hasCodes = !codes.isEmpty();

        if (!hasTag && !hasCodes) {
            throw new IllegalArgumentException("Укажите tests и/или regressionTag");
        }

        List<RegressionCaseEntity> rows;
        if (hasTag && hasCodes) {
            rows = repository.findByRegressionTagAndTestCodeInOrderByTestCodeAsc(regressionTag.trim(), codes);
            requireAllCodes(rows, codes, "Коды не найдены для regressionTag=" + regressionTag.trim() + ": ");
            rows = orderByCodes(rows, codes);
        } else if (hasTag) {
            rows = repository.findByRegressionTagOrderByTestCodeAsc(regressionTag.trim());
            if (rows.isEmpty()) {
                throw new IllegalArgumentException("Нет кейсов с regressionTag=" + regressionTag.trim());
            }
        } else {
            rows = repository.findByTestCodeIn(codes);
            requireAllCodes(rows, codes, "Неизвестные коды тестов: ");
            rows = orderByCodes(rows, codes);
        }

        return rows.stream().map(this::toCase).toList();
    }

    private TestCase toCase(RegressionCaseEntity row) {
        String code = row.getTestCode();
        MessageSlot primary = new MessageSlot(
                parseOptionalJson(row.getAuth(), code + ".auth"),
                parseOptionalJson(row.getClr(), code + ".clr")
        );
        MessageSlot add1 = new MessageSlot(
                parseOptionalJson(row.getAuthAdd1(), code + ".auth_add_1"),
                parseOptionalJson(row.getClrAdd1(), code + ".clr_add_1")
        );
        MessageSlot add2 = new MessageSlot(
                parseOptionalJson(row.getAuthAdd2(), code + ".auth_add_2"),
                parseOptionalJson(row.getClrAdd2(), code + ".clr_add_2")
        );
        if (primary.isEmpty() && add1.isEmpty() && add2.isEmpty()) {
            throw new IllegalStateException("Кейс " + code + " не содержит ни одного auth/clr сообщения");
        }
        return new TestCase(code, row.getTestDescription(), row.getRegressionTag(), primary, add1, add2);
    }

    private JsonNode parseOptionalJson(String raw, String label) {
        if (raw == null || raw.isBlank() || "null".equalsIgnoreCase(raw.trim())) {
            return null;
        }
        try {
            return objectMapper.readTree(raw);
        } catch (Exception e) {
            throw new IllegalStateException("Некорректный JSON в regression_cases (" + label + ")", e);
        }
    }

    private static void requireAllCodes(List<RegressionCaseEntity> rows, List<String> codes, String errorPrefix) {
        Set<String> found = rows.stream().map(RegressionCaseEntity::getTestCode).collect(Collectors.toSet());
        List<String> missing = codes.stream().filter(c -> !found.contains(c)).toList();
        if (!missing.isEmpty()) {
            throw new IllegalArgumentException(errorPrefix + String.join(", ", missing));
        }
    }

    private static List<RegressionCaseEntity> orderByCodes(List<RegressionCaseEntity> rows, List<String> codes) {
        Map<String, RegressionCaseEntity> byCode = rows.stream()
                .collect(Collectors.toMap(RegressionCaseEntity::getTestCode, e -> e, (a, b) -> a, LinkedHashMap::new));
        return codes.stream().map(byCode::get).toList();
    }

    private static List<String> normalizeCodes(List<String> testCodes) {
        if (testCodes == null || testCodes.isEmpty()) {
            return List.of();
        }
        Set<String> seen = new LinkedHashSet<>();
        for (String code : testCodes) {
            if (code != null && !code.isBlank()) {
                seen.add(code.trim());
            }
        }
        return new ArrayList<>(seen);
    }
}
