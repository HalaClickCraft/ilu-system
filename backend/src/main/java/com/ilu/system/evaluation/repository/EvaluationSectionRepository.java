package com.ilu.system.evaluation.repository;

import com.ilu.system.evaluation.entity.EvaluationSection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EvaluationSectionRepository extends JpaRepository<EvaluationSection, Long> {

    List<EvaluationSection> findByTemplateIdOrderByDisplayOrderAsc(Long templateId);
}