package com.ilu.system.evaluation.repository;

import com.ilu.system.evaluation.entity.EvaluationQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EvaluationQuestionRepository extends JpaRepository<EvaluationQuestion, Long> {

    List<EvaluationQuestion> findByStatus(EvaluationQuestion.QuestionStatus status);

    @Query("SELECT q FROM EvaluationQuestion q WHERE q.status = 'PENDING' ORDER BY q.createdAt DESC")
    List<EvaluationQuestion> findPendingQuestions();

    @Query("SELECT q FROM EvaluationQuestion q WHERE q.template.id = :templateId AND q.status = 'VALIDATED' ORDER BY q.questionNumber ASC")
    List<EvaluationQuestion> findValidatedQuestionsByTemplate(@Param("templateId") Long templateId);

    List<EvaluationQuestion> findByTemplateId(Long templateId);
}