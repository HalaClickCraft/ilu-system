package com.ilu.system.evaluation.service;

import com.ilu.system.auth.repository.UserRepository;
import com.ilu.system.evaluation.entity.EvaluationAnswer;
import com.ilu.system.evaluation.entity.EvaluationQuestion;
import com.ilu.system.evaluation.entity.EvaluationSession;
import com.ilu.system.evaluation.entity.EvaluationTemplate;
import com.ilu.system.evaluation.repository.*;
import com.ilu.system.operator.entity.Operator;
import com.ilu.system.operator.entity.WorkstationFormation;
import com.ilu.system.operator.repository.FormationAssignmentRepository;
import com.ilu.system.operator.repository.OperatorRepository;
import com.ilu.system.operator.repository.WorkstationFormationRepository;
import com.ilu.system.recyclage.repository.RecyclagePlanningRepository;
import com.ilu.system.structure.entity.Workstation;
import com.ilu.system.structure.repository.ProjectRepository;
import com.ilu.system.structure.repository.WorkstationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EvaluationServicePendingEvaluationTest {

    @Mock
    private EvaluationTemplateRepository templateRepo;
    @Mock
    private EvaluationSectionRepository sectionRepo;
    @Mock
    private EvaluationQuestionRepository questionRepo;
    @Mock
    private EvaluationSessionRepository sessionRepo;
    @Mock
    private EvaluationAnswerRepository answerRepo;
    @Mock
    private OperatorRepository operatorRepo;
    @Mock
    private WorkstationRepository workstationRepo;
    @Mock
    private FormationAssignmentRepository assignmentRepo;
    @Mock
    private WorkstationFormationRepository formationRepo;
    @Mock
    private UserRepository userRepo;
    @Mock
    private ProjectRepository projectRepo;
    @Mock
    private RecyclagePlanningRepository recyclagePlanningRepo;

    private EvaluationService evaluationService;

    @BeforeEach
    void setUp() {
        evaluationService = new EvaluationService(
                templateRepo, sectionRepo, questionRepo, sessionRepo,
                answerRepo, operatorRepo, workstationRepo, assignmentRepo,
                formationRepo, userRepo, projectRepo, recyclagePlanningRepo
        );
    }

    @Test
    void getAllPendingEvaluations_shouldExcludeOperatorAlreadyPassedProductionOnWorkstation() {
        Operator operator = new Operator();
        operator.setId(7L);
        operator.setLastName("Dupont");
        operator.setFirstName("Jean");
        operator.setEmployeeId("E-007");
        operator.setHireDate(LocalDate.now().minusYears(2));

        Workstation workstation = new Workstation();
        workstation.setId(13L);
        workstation.setName("Usinage");

        WorkstationFormation formation = new WorkstationFormation();
        formation.setId(42L);
        formation.setOperator(operator);
        formation.setWorkstation(workstation);
        formation.setStatus("COMPLETED");
        formation.setEndDate(LocalDate.now());

        when(operatorRepo.findAll()).thenReturn(List.of(operator));
        when(formationRepo.findAll()).thenReturn(List.of(formation));

        EvaluationTemplate template = new EvaluationTemplate();
        template.setId(88L);
        template.setType(EvaluationTemplate.TemplateType.POSTE_PRODUCTION);
        template.setWorkstation(workstation);

        EvaluationSession passedProduction = new EvaluationSession();
        passedProduction.setId(99L);
        passedProduction.setOperator(operator);
        passedProduction.setStatus(EvaluationSession.SessionStatus.PASSED);
        passedProduction.setTemplate(template);

        when(sessionRepo.findByOperatorIdOrderByCreatedAtDesc(7L)).thenReturn(List.of(passedProduction));

        List<Map<String, Object>> pending = evaluationService.getAllPendingEvaluations();

        assertTrue(pending.isEmpty());
    }
}
