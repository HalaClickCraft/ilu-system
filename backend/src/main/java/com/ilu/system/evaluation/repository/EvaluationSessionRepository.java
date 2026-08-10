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

    // Auto-trigger: find if operator already has a session for a specific formation
    boolean existsByOperatorIdAndFormationId(Long operatorId, Long formationId);

    boolean existsByOperatorIdAndPracticalFormationId(Long operatorId, Long practicalFormationId);
}