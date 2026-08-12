package com.ilu.system.evaluation.repository;

import com.ilu.system.evaluation.entity.EvaluationSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EvaluationSessionRepository extends JpaRepository<EvaluationSession, Long> {

    List<EvaluationSession> findByOperatorIdOrderByCreatedAtDesc(Long operatorId);

    Optional<EvaluationSession> findTopByOperatorIdAndFormationIdOrderByCreatedAtDesc(Long operatorId, Long formationId);

    @Query("SELECT s FROM EvaluationSession s WHERE s.status = 'IN_PROGRESS'")
    List<EvaluationSession> findInProgressSessions();

    boolean existsByOperatorIdAndFormationId(Long operatorId, Long formationId);

    boolean existsByOperatorIdAndPracticalFormationId(Long operatorId, Long practicalFormationId);

    @Query("SELECT s FROM EvaluationSession s " +
           "LEFT JOIN s.practicalFormation pf " +
           "LEFT JOIN s.template t " +
           "WHERE s.operator.id = :operatorId AND s.status = 'PASSED' " +
           "ORDER BY s.createdAt DESC")
    List<EvaluationSession> findPassedSessionsForOperator(@Param("operatorId") Long operatorId);

    @Query("SELECT s FROM EvaluationSession s " +
           "LEFT JOIN s.practicalFormation pf " +
           "LEFT JOIN s.template t " +
           "WHERE s.operator.id = :operatorId AND s.status = 'PASSED' AND s.niveau = :niveau " +
           "AND (pf.workstation.id = :workstationId OR t.workstation.id = :workstationId)")
    List<EvaluationSession> findPassedByOperatorAndWorkstationAndNiveau(@Param("operatorId") Long operatorId,
                                                                        @Param("workstationId") Long workstationId,
                                                                        @Param("niveau") String niveau);
}