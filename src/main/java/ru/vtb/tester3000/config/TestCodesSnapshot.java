package ru.vtb.tester3000.config;

import org.springframework.stereotype.Component;
import ru.vtb.tester3000.entity.TestMessageFrom25Entity;
import ru.vtb.tester3000.repository.TestMessageFrom25Repository;

import java.util.Collections;
import java.util.List;

/**
 * Снимок кодов тестов из БД на момент старта приложения (для Swagger default).
 */
@Component
public class TestCodesSnapshot {

    private final List<String> codes;

    public TestCodesSnapshot(TestMessageFrom25Repository repository) {
        this.codes = Collections.unmodifiableList(
                repository.findAllByOrderByTestCodeAsc().stream()
                        .map(TestMessageFrom25Entity::getTestCode)
                        .toList()
        );
    }

    public List<String> getCodes() {
        return codes;
    }
}
