package com.ilu.system.recyclage.repository;

import com.ilu.system.recyclage.entity.RecyclagePlanning;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static com.ilu.system.recyclage.entity.RecyclagePlanning.PlanningStatus;
import static com.ilu.system.recyclage.entity.RecyclagePlanning.PlanningType;

@Repository
public interface RecyclagePlanningRepository extends JpaRepository<RecyclagePlanning, Long> {

    List<RecyclagePlanning> findByOperator_Id(Long operatorId);

    void deleteByOperator_IdAndStatusIn(Long operatorId, List<PlanningStatus> statuses);

    void deleteByOperator_Id(Long operatorId);

    List<RecyclagePlanning> findByStatus(PlanningStatus status);

    List<RecyclagePlanning> findByStatusAndScheduledDateBetween(PlanningStatus status, LocalDate start, LocalDate end);

    List<RecyclagePlanning> findByProjectIdAndStatus(Long projectId, PlanningStatus status);

    List<RecyclagePlanning> findByScheduledDateBetween(LocalDate start, LocalDate end);

    List<RecyclagePlanning> findByOperator_IdAndStatus(Long operatorId, PlanningStatus status);

    Optional<RecyclagePlanning> findFirstByOperator_IdAndWorkstation_IdAndStatusOrderByScheduledDateAsc(
            Long operatorId, Long workstationId, PlanningStatus status);

    boolean existsByOperator_IdAndWorkstation_IdAndStatusAndType(Long operatorId, Long workstationId, PlanningStatus status, PlanningType type);

    boolean existsByOperator_IdAndWorkstation_IdAndScheduledDateAndType(
            Long operatorId, Long workstationId, LocalDate scheduledDate, PlanningType type);

    List<RecyclagePlanning> findByOperator_IdAndTypeIn(Long operatorId, List<PlanningType> types);

    Optional<RecyclagePlanning> findTopByOperator_IdAndWorkstation_IdAndTypeOrderByScheduledDateDesc(
            Long operatorId, Long workstationId, PlanningType type);
}
