package ru.vtb.tester3000.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.vtb.tester3000.entity.RegressionCaseEntity;

import java.util.Collection;
import java.util.List;

public interface RegressionCaseRepository extends JpaRepository<RegressionCaseEntity, String> {

    List<RegressionCaseEntity> findAllByOrderByTestCodeAsc();

    List<RegressionCaseEntity> findByTestCodeIn(Collection<String> testCodes);

    List<RegressionCaseEntity> findByRegressionTagOrderByTestCodeAsc(String regressionTag);

    List<RegressionCaseEntity> findByRegressionTagAndTestCodeInOrderByTestCodeAsc(
            String regressionTag,
            Collection<String> testCodes
    );

    @Query("select distinct c.regressionTag from RegressionCaseEntity c "
            + "where c.regressionTag is not null and c.regressionTag <> '' order by c.regressionTag")
    List<String> findDistinctRegressionTags();
}
