package ru.vtb.tester3000.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.vtb.tester3000.entity.ProcessEntity;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProcessRepository extends JpaRepository<ProcessEntity, UUID> {

    List<ProcessEntity> findByRunIdOrderByCreatedAtAsc(UUID runId);

    Optional<ProcessEntity> findFirstByReqIdAndStepInOrderByCreatedAtDesc(
            String reqId,
            Collection<ProcessEntity.Step> steps
    );
}
