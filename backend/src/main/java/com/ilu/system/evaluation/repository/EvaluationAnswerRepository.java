package com.ilu.system.evaluation.repository;

import com.ilu.system.evaluation.entity.EvaluationAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EvaluationAnswerRepository extends JpaRepository<EvaluationAnswer, Long> {

    List<EvaluationAnswer> findBySessionId(Long sessionId);

    Optional<EvaluationAnswer> findBySessionIdAndQuestionId(Long sessionId, Long questionId);

    @Modifying
    @Query("DELETE FROM EvaluationAnswer a WHERE a.question.id = :questionId")
    void deleteByQuestionId(Long questionId);
}