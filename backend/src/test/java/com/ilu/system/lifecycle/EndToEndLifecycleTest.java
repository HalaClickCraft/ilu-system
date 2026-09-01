package com.ilu.system.lifecycle;

import com.ilu.system.evaluation.entity.*;
import com.ilu.system.evaluation.repository.*;
import com.ilu.system.evaluation.service.EvaluationService;
import com.ilu.system.notification.service.NotificationService;
import com.ilu.system.operator.dto.CreateOperatorRequest;
import com.ilu.system.operator.entity.*;
import com.ilu.system.operator.repository.*;
import com.ilu.system.operator.service.OperatorService;
import com.ilu.system.recyclage.entity.RecyclagePlanning;
import com.ilu.system.recyclage.repository.RecyclagePlanningRepository;
import com.ilu.system.recyclage.service.RecyclageService;
import com.ilu.system.structure.entity.*;
import com.ilu.system.structure.repository.*;
import com.ilu.system.structure.service.StructureService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EndToEndLifecycleTest {

    // Structure Mocks
    @Mock private ProjectRepository projectRepo;
    @Mock private ZoneRepository zoneRepo;
    @Mock private WorkstationRepository wsRepo;
    @Mock private ProjectMemberRepository memberRepo;

    // Operator Mocks
    @Mock private OperatorRepository operatorRepo;
    @Mock private TeamRepository teamRepo;
    @Mock private WorkstationFormationRepository formationRepo;
    @Mock private FormationAssignmentRepository assignmentRepo;

    // Evaluation Mocks
    @Mock private EvaluationTemplateRepository templateRepo;
    @Mock private EvaluationSectionRepository sectionRepo;
    @Mock private EvaluationQuestionRepository questionRepo;
    @Mock private EvaluationSessionRepository sessionRepo;
    @Mock private EvaluationAnswerRepository answerRepo;

    // Recyclage & Notification Mocks
    @Mock private RecyclagePlanningRepository recyclageRepo;
    @Mock private NotificationService notificationService;

    private StructureService structureService;
    private OperatorService operatorService;
    private EvaluationService evaluationService;
    private RecyclageService recyclageService;

    @BeforeEach
    void setUp() {
        structureService = new StructureService(projectRepo, zoneRepo, wsRepo, memberRepo, null);
        operatorService = new OperatorService(
                operatorRepo, teamRepo, projectRepo, zoneRepo,
                formationRepo, assignmentRepo, recyclageRepo
        );
        evaluationService = new EvaluationService(
                templateRepo, sectionRepo, questionRepo, sessionRepo,
                answerRepo, operatorRepo, wsRepo, assignmentRepo,
                formationRepo, null, projectRepo, zoneRepo, recyclageRepo
        );
        recyclageService = new RecyclageService(
                recyclageRepo, operatorRepo, formationRepo, wsRepo,
                sessionRepo, templateRepo, notificationService
        );
    }

    @Test
    @DisplayName("FULL LIFECYCLE: Projet -> Poste -> Opérateur -> Formation -> Évaluation Initiale (L) -> Recyclage")
    void testCompleteAppLifecycle() {

        // ====================================================================
        // STEP 1: CREATE PROJECT, ZONE & WORKSTATION (STRUCTURE)
        // ====================================================================
        Project project = new Project();
        project.setId(100L);
        project.setName("Projet Assembly 2026");

        Zone zone = new Zone();
        zone.setId(200L);
        zone.setName("Zone Qualité / Prod");
        zone.setProject(project);

        Workstation ws = new Workstation();
        ws.setId(300L);
        ws.setName("Poste Soudeur 501");
        ws.setType("POSTE");
        ws.setZone(zone);
        ws.setTargetIluLevel("L");

        when(projectRepo.findById(100L)).thenReturn(Optional.of(project));
        when(memberRepo.save(any(ProjectMember.class))).thenAnswer(inv -> inv.getArgument(0));

        // Assign Chef d'Équipe to Project
        var pmDto = structureService.addMember(100L, "TL-99", "Hassan Chef", "TEAM_LEADER");
        assertNotNull(pmDto);
        assertEquals("TL-99", pmDto.employeeId());

        // ====================================================================
        // STEP 2: CREATE OPERATOR & ASSIGN TO CHEF D'ÉQUIPE TEAM
        // ====================================================================
        Team team = new Team();
        team.setId(1L);
        team.setTeamLeader("Hassan Chef");
        team.setTeamLeaderEmployeeId("TL-99");

        CreateOperatorRequest opReq = new CreateOperatorRequest();
        opReq.setEmployeeId("OP-888");
        opReq.setLastName("Mansouri");
        opReq.setFirstName("Tariq");
        opReq.setRole("Opérateur");
        opReq.setOperatorType("NOUVEAU_RECRU");
        opReq.setProjectId(100L);
        opReq.setTeamId(1L);

        when(operatorRepo.existsByEmployeeId("OP-888")).thenReturn(false);
        when(teamRepo.findById(1L)).thenReturn(Optional.of(team));
        when(projectRepo.findById(100L)).thenReturn(Optional.of(project));
        when(operatorRepo.save(any(Operator.class))).thenAnswer(inv -> inv.getArgument(0));

        Operator operator = operatorService.createOperator(opReq);
        assertNotNull(operator);
        assertEquals("OP-888", operator.getEmployeeId());
        assertEquals("Hassan Chef", operator.getTeam().getTeamLeader());

        // ====================================================================
        // STEP 3: INITIAL EVALUATION SESSION CHECK & LEVEL GAIN
        // ====================================================================

        // ====================================================================
        // STEP 4: PASS INITIAL EVALUATION SESSION & GAIN LEVEL L
        // ====================================================================
        EvaluationTemplate tpl = new EvaluationTemplate();
        tpl.setId(10L);
        tpl.setWorkstation(ws);

        EvaluationSession session = new EvaluationSession();
        session.setId(500L);
        session.setOperator(operator);
        session.setTemplate(tpl);
        session.setStatus(EvaluationSession.SessionStatus.PASSED);
        session.setNiveau("L");
        session.setScorePercentage(95.0);
        session.setCompletedAt(LocalDateTime.now().minusDays(2));

        when(sessionRepo.findPassedByOperatorAndWorkstation(operator.getId(), ws.getId()))
                .thenReturn(List.of(session));

        // Verify session evaluation result
        List<EvaluationSession> sessions = sessionRepo.findPassedByOperatorAndWorkstation(operator.getId(), ws.getId());
        assertFalse(sessions.isEmpty());
        assertEquals("PASSED", sessions.get(0).getStatus().name());
        assertEquals("L", sessions.get(0).getNiveau());

        // ====================================================================
        // STEP 5: SCHEDULE & COMPLETE RECYCLAGE SESSION
        // ====================================================================
        RecyclagePlanning recyclage = new RecyclagePlanning();
        recyclage.setId(600L);
        recyclage.setOperator(operator);
        recyclage.setWorkstation(ws);
        recyclage.setProjectId(100L);
        recyclage.setType(RecyclagePlanning.PlanningType.RECYCLAGE);
        recyclage.setStatus(RecyclagePlanning.PlanningStatus.PLANIFIEE);
        recyclage.setScheduledDate(LocalDate.now().plusMonths(6));

        when(recyclageRepo.save(any(RecyclagePlanning.class))).thenAnswer(inv -> inv.getArgument(0));

        // Complete recyclage
        recyclage.setStatus(RecyclagePlanning.PlanningStatus.TERMINEE);
        recyclage.setCompletedAt(LocalDateTime.now());

        RecyclagePlanning completedRecyclage = recyclageRepo.save(recyclage);
        assertNotNull(completedRecyclage);
        assertEquals(RecyclagePlanning.PlanningStatus.TERMINEE, completedRecyclage.getStatus());

        // SUCCESSFUL END-TO-END FLOW VERIFICATION
        assertTrue(operator.getActive());
        assertEquals("L", session.getNiveau());
    }
}
