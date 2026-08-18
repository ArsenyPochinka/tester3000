package ru.vtb.tester3000.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    /**
     * Связка 39→104: Instruction.Id.Id (39) = Object.Id.Id (104).
     */
    @Query(value = """
            SELECT * FROM process
            WHERE step = '104_FIN_INSTRUCTION'
              AND result IS NOT NULL
              AND result::jsonb #>> '{Object,Id,Id}' = :objectId
            ORDER BY created_at DESC
            LIMIT 1
            """, nativeQuery = true)
    Optional<ProcessEntity> findLatestFinInstruction104ByObjectId(@Param("objectId") String objectId);
}
