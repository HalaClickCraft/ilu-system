package com.ilu.system.evaluation.repository;

import com.ilu.system.evaluation.entity.EvaluationTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EvaluationTemplateRepository extends JpaRepository<EvaluationTemplate, Long> {

    List<EvaluationTemplate> findByTypeAndStatus(EvaluationTemplate.TemplateType type,
                                                  EvaluationTemplate.TemplateStatus status);

    List<EvaluationTemplate> findByWorkstationId(Long workstationId);

    @Query("SELECT t FROM EvaluationTemplate t "
            + "WHERE t.status = 'VALIDATED' AND t.type = 'POSTE_PRODUCTION' "
            + "AND t.workstation.id = :workstationId")
    List<EvaluationTemplate> findValidatedProductionForWorkstation(@Param("workstationId") Long workstationId);

    @Query("SELECT t FROM EvaluationTemplate t "
            + "WHERE t.status = 'VALIDATED' AND t.type = 'ANIMATION' "
            + "AND t.workstation.id = :workstationId")
    List<EvaluationTemplate> findValidatedAnimationForWorkstation(@Param("workstationId") Long workstationId);

    @Query("SELECT t FROM EvaluationTemplate t WHERE t.status = 'VALIDATED' ORDER BY t.name ASC")
    List<EvaluationTemplate> findAllActive();

    @Query("SELECT t FROM EvaluationTemplate t WHERE t.type IN :types AND t.status = 'VALIDATED'")
    List<EvaluationTemplate> findValidatedGenericTemplates(@Param("types") List<EvaluationTemplate.TemplateType> types);

    Optional<EvaluationTemplate> findByIdAndStatus(Long id, EvaluationTemplate.TemplateStatus status);
}