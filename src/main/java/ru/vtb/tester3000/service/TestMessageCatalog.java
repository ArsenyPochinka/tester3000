package ru.vtb.tester3000.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import ru.vtb.tester3000.entity.TestMessageFrom25Entity;
import ru.vtb.tester3000.repository.TestMessageFrom25Repository;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class TestMessageCatalog {

    public record TestCase(String testCode, String testDescription, JsonNode auth, Optional<JsonNode> clearing) {
    }

    private final ObjectMapper objectMapper;
    private final TestMessageFrom25Repository repository;

    public TestMessageCatalog(ObjectMapper objectMapper, TestMessageFrom25Repository repository) {
        this.objectMapper = objectMapper;
        this.repository = repository;
    }

    /**
     * Возвращает тесты из БД в порядке переданных кодов.
     */
    public List<TestCase> listTests(List<String> testCodes) {
        if (testCodes == null || testCodes.isEmpty()) {
            throw new IllegalArgumentException("Не указан перечень тестов");
        }

        List<String> orderedUnique = new ArrayList<>();
        for (String code : testCodes) {
            if (code == null || code.isBlank()) {
                continue;
            }
            String trimmed = code.trim();
            if (!orderedUnique.contains(trimmed)) {
                orderedUnique.add(trimmed);
            }
        }
        if (orderedUnique.isEmpty()) {
            throw new IllegalArgumentException("Не указан перечень тестов");
        }

        Map<String, TestMessageFrom25Entity> byCode = repository.findByTestCodeIn(orderedUnique).stream()
                .collect(Collectors.toMap(
                        TestMessageFrom25Entity::getTestCode,
                        e -> e,
                        (a, b) -> a,
                        LinkedHashMap::new
                ));

        Set<String> missing = orderedUnique.stream()
                .filter(code -> !byCode.containsKey(code))
                .collect(Collectors.toCollection(java.util.LinkedHashSet::new));
        if (!missing.isEmpty()) {
            throw new IllegalArgumentException("Неизвестные коды тестов: " + String.join(", ", missing));
        }

        List<TestCase> result = new ArrayList<>(orderedUnique.size());
        for (String code : orderedUnique) {
            TestMessageFrom25Entity row = byCode.get(code);
            JsonNode auth = readJson(row.getAuth(), code + ".auth");
            Optional<JsonNode> clearing = Optional.empty();
            if (row.getClr() != null && !row.getClr().isBlank()) {
                clearing = Optional.of(readJson(row.getClr(), code + ".clr"));
            }
            result.add(new TestCase(row.getTestCode(), row.getTestDescription(), auth, clearing));
        }
        return result;
    }

    private JsonNode readJson(String raw, String label) {
        try {
            return objectMapper.readTree(raw);
        } catch (Exception e) {
            throw new IllegalStateException("Некорректный JSON в test_messages_from_25 (" + label + ")", e);
        }
    }
}
