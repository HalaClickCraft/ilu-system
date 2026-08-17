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

    @Query("SELECT s FROM EvaluationSession s " +
           "LEFT JOIN FETCH s.template t " +
           "LEFT JOIN FETCH t.workstation " +
           "WHERE s.operator.id = :operatorId AND s.status = 'PASSED' " +
           "ORDER BY s.createdAt DESC")
    List<EvaluationSession> findPassedSessionsForOperator(@Param("operatorId") Long operatorId);

    @Query("SELECT COUNT(s) > 0 FROM EvaluationSession s " +
           "WHERE s.operator.id = :operatorId AND s.decision = 'PASSED_GENERIC'")
    boolean hasPassedGeneric(@Param("operatorId") Long operatorId);

    @Query("SELECT s FROM EvaluationSession s " +
           "JOIN s.template t " +
           "JOIN t.workstation w " +
           "WHERE s.operator.id = :operatorId AND s.status = 'PASSED' AND w.id = :workstationId " +
           "ORDER BY s.createdAt DESC")
    List<EvaluationSession> findPassedByOperatorAndWorkstation(@Param("operatorId") Long operatorId,
                                                               @Param("workstationId") Long workstationId);

    @Query("SELECT s FROM EvaluationSession s " +
           "JOIN s.template t " +
           "JOIN t.workstation w " +
           "WHERE s.operator.id = :operatorId AND s.status = 'PASSED' " +
           "AND s.niveau = :niveau AND w.id = :workstationId " +
           "ORDER BY s.createdAt DESC")
    List<EvaluationSession> findPassedByOperatorAndWorkstationAndNiveau(@Param("operatorId") Long operatorId,
                                                                        @Param("workstationId") Long workstationId,
                                                                        @Param("niveau") String niveau);

    @Query("SELECT s FROM EvaluationSession s " +
           "WHERE s.operator.id = :operatorId AND s.status IN ('PASSED', 'FAILED', 'BLOCKED') " +
           "ORDER BY s.createdAt DESC")
    List<EvaluationSession> findCompletedSessionsForOperator(@Param("operatorId") Long operatorId);
}