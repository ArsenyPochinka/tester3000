package ru.vtb.tester3000.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.vtb.tester3000.entity.RegressionTestMessageEntity;

import java.util.List;
import java.util.UUID;

public interface RegressionTestMessageRepository extends JpaRepository<RegressionTestMessageEntity, UUID> {

    List<RegressionTestMessageEntity> findByRunIdOrderByTestNameAsc(UUID runId);
}
