package ru.vtb.tester3000.config;

import org.springframework.stereotype.Component;
import ru.vtb.tester3000.entity.RegressionCaseEntity;
import ru.vtb.tester3000.repository.RegressionCaseRepository;

import java.util.Collections;
import java.util.List;

/**
 * Снимок кодов и тегов из {@code regression_cases} на старте приложения (для Swagger defaults).
 */
@Component
public class RegressionCasesSnapshot {

    private final List<String> codes;
    private final List<String> regressionTags;

    public RegressionCasesSnapshot(RegressionCaseRepository repository) {
        this.codes = Collections.unmodifiableList(
                repository.findAllByOrderByTestCodeAsc().stream()
                        .map(RegressionCaseEntity::getTestCode)
                        .toList()
        );
        this.regressionTags = Collections.unmodifiableList(repository.findDistinctRegressionTags());
    }

    public List<String> getCodes() {
        return codes;
    }

    public List<String> getRegressionTags() {
        return regressionTags;
    }
}
