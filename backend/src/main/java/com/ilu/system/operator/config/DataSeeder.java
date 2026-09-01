package com.ilu.system.operator.config;

import com.ilu.system.auth.entity.Role;
import com.ilu.system.auth.entity.User;
import com.ilu.system.auth.repository.RoleRepository;
import com.ilu.system.auth.repository.UserRepository;
import com.ilu.system.auth.service.UserService;
import com.ilu.system.evaluation.entity.EvaluationQuestion;
import com.ilu.system.evaluation.entity.EvaluationAnswer;
import com.ilu.system.evaluation.entity.EvaluationSection;
import com.ilu.system.evaluation.entity.EvaluationSession;
import com.ilu.system.evaluation.entity.EvaluationTemplate;
import com.ilu.system.evaluation.repository.EvaluationQuestionRepository;
import com.ilu.system.evaluation.repository.EvaluationAnswerRepository;
import com.ilu.system.evaluation.repository.EvaluationSectionRepository;
import com.ilu.system.evaluation.repository.EvaluationSessionRepository;
import com.ilu.system.evaluation.repository.EvaluationTemplateRepository;
import com.ilu.system.operator.entity.DailyFormationTracking;
import com.ilu.system.operator.entity.Operator;
import com.ilu.system.operator.entity.WorkstationFormation;
import com.ilu.system.operator.repository.DailyFormationTrackingRepository;
import com.ilu.system.operator.repository.OperatorRepository;
import com.ilu.system.operator.repository.WorkstationFormationRepository;
import com.ilu.system.operator.service.OnboardingService;
import com.ilu.system.structure.entity.Project;
import com.ilu.system.structure.entity.Workstation;
import com.ilu.system.structure.entity.Zone;
import com.ilu.system.structure.repository.ProjectRepository;
import com.ilu.system.structure.repository.WorkstationRepository;
import com.ilu.system.structure.repository.ZoneRepository;
import com.ilu.system.recyclage.entity.RecyclagePlanning;
import com.ilu.system.recyclage.repository.RecyclagePlanningRepository;

import com.ilu.system.structure.entity.ProjectMember;
import com.ilu.system.structure.repository.ProjectMemberRepository;
import com.ilu.system.operator.entity.OnboardingModule;
import com.ilu.system.operator.entity.OperatorOnboarding;
import com.ilu.system.operator.repository.OnboardingModuleRepository;
import com.ilu.system.operator.repository.OperatorOnboardingRepository;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Component
public class DataSeeder implements CommandLineRunner {

    private final UserService userService;
    private final OnboardingService onboardingService;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final ZoneRepository zoneRepository;
    private final WorkstationRepository workstationRepository;
    private final OperatorRepository operatorRepository;
    private final WorkstationFormationRepository formationRepository;
    private final DailyFormationTrackingRepository dailyTrackingRepository;
    private final EvaluationTemplateRepository templateRepository;
    private final EvaluationSectionRepository sectionRepository;
    private final EvaluationQuestionRepository questionRepository;
    private final EvaluationAnswerRepository answerRepository;
    private final EvaluationSessionRepository sessionRepository;
    private final RecyclagePlanningRepository recyclagePlanningRepository;
    private final OnboardingModuleRepository onboardingModuleRepository;
    private final OperatorOnboardingRepository operatorOnboardingRepository;

    public DataSeeder(UserService userService,
                      OnboardingService onboardingService,
                      UserRepository userRepository,
                      RoleRepository roleRepository,
                      PasswordEncoder passwordEncoder,
                      ProjectRepository projectRepository,
                      ProjectMemberRepository projectMemberRepository,
                      ZoneRepository zoneRepository,
                      WorkstationRepository workstationRepository,
                      OperatorRepository operatorRepository,
                      WorkstationFormationRepository formationRepository,
                      DailyFormationTrackingRepository dailyTrackingRepository,
                      EvaluationTemplateRepository templateRepository,
                      EvaluationSectionRepository sectionRepository,
                      EvaluationQuestionRepository questionRepository,
                      EvaluationAnswerRepository answerRepository,
                      EvaluationSessionRepository sessionRepository,
                      RecyclagePlanningRepository recyclagePlanningRepository,
                      OnboardingModuleRepository onboardingModuleRepository,
                      OperatorOnboardingRepository operatorOnboardingRepository) {
        this.userService = userService;
        this.onboardingService = onboardingService;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.projectRepository = projectRepository;
        this.projectMemberRepository = projectMemberRepository;
        this.zoneRepository = zoneRepository;
        this.workstationRepository = workstationRepository;
        this.operatorRepository = operatorRepository;
        this.formationRepository = formationRepository;
        this.dailyTrackingRepository = dailyTrackingRepository;
        this.templateRepository = templateRepository;
        this.sectionRepository = sectionRepository;
        this.questionRepository = questionRepository;
        this.answerRepository = answerRepository;
        this.sessionRepository = sessionRepository;
        this.recyclagePlanningRepository = recyclagePlanningRepository;
        this.onboardingModuleRepository = onboardingModuleRepository;
        this.operatorOnboardingRepository = operatorOnboardingRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        userService.seedRoles();
        onboardingService.seedModules();
        seedUsers();
        seedMockData(); // Re-enabled as requested by the user for full testing
    }

    private void seedUsers() {
        createUserIfNotFound("ADM002", "Administrateur OPmobility", "admin123", "ADMIN", "Direction");
        createUserIfNotFound("admin", "Administrateur Système", "admin123", "ADMIN", "Direction");
        createUserIfNotFound("RH-001", "Responsable RH", "admin123", "RH", "Ressources Humaines");
        createUserIfNotFound("CHEF-001", "Chef d'Équipe ", "admin123", "CHEF_EQUIPE", "Production");
        createUserIfNotFound("CHEF-002", "Chef d'Équipe ", "admin123", "CHEF_EQUIPE", "Production");
        createUserIfNotFound("AQ-001", "Agent Qualité", "admin123", "AGENT_QUALITE", "Qualité");
        createUserIfNotFound("AQ-002", "Responsable Qualité", "admin123", "RESP_QUALITE", "Qualité");
        createUserIfNotFound("RHSE-001", "Responsable HSE", "admin123", "RESP_HSE", "HSE");
        createUserIfNotFound("SUP-001", "Superviseur Usine", "admin123", "SUPERVISEUR", "Production");
        createUserIfNotFound("PRO-001", "Chef Dept Process", "admin123", "DEPT_PROCESS", "Process");
        createUserIfNotFound("MAI-001", "Chef Dept Maintenance", "admin123", "DEPT_MAINTENANCE", "Maintenance");
        createUserIfNotFound("DG-001", "Chef Dept DGT Mfg", "admin123", "DEPT_DGT_MANUFACTURING", "DGT Manufacturing");
    }

    private void createUserIfNotFound(String employeeId, String name, String rawPassword, String roleLabel, String department) {
        Optional<User> existing = userRepository.findByEmployeeId(employeeId);
        if (existing.isEmpty()) {
            User user = new User();
            user.setEmployeeId(employeeId);
            user.setName(name);
            user.setNationalId("CIN_" + employeeId);
            user.setPassword(passwordEncoder.encode(rawPassword));
            user.setMustChangePassword(false);
            user.setDepartment(department);
            user.setActive(true);

            Role role = roleRepository.findByLabel(roleLabel).orElseGet(() -> {
                Role r = new Role();
                r.setLabel(roleLabel);
                return roleRepository.save(r);
            });

            Set<Role> roles = new HashSet<>();
            roles.add(role);
            user.setRoles(roles);

            userRepository.save(user);
        } else {
            User user = existing.get();
            user.setActive(true);
            userRepository.save(user);
        }
    }

    private void seedMockData() {
        // 1. Projects & Structures
        Project project = projectRepository.findByName("KJ92 2026").orElseGet(() -> {
            Project p = new Project();
            p.setName("KJ92 2026");
            return projectRepository.save(p);
        });

        // Clean up old incorrect project member IDs to prevent duplicates/confusion
        projectMemberRepository.findByProjectId(project.getId()).stream()
            .filter(pm -> pm.getEmployeeId().equals("CHEF001") ||
                          pm.getEmployeeId().equals("CHEF002") ||
                          pm.getEmployeeId().equals("QUAL001") ||
                          pm.getEmployeeId().equals("QUAL002") ||
                          pm.getEmployeeId().equals("SUP001"))
            .forEach(projectMemberRepository::delete);

        // Seed Project Members for permissions
        seedProjectMember(project, "CHEF-001", "Chef d'Équipe ", ProjectMember.ProjectRole.TEAM_LEADER);
        seedProjectMember(project, "CHEF-002", "Chef d'Équipe ", ProjectMember.ProjectRole.TEAM_LEADER);
        seedProjectMember(project, "AQ-001", "Agent Qualité", ProjectMember.ProjectRole.MEMBER);
        seedProjectMember(project, "AQ-002", "Responsable Qualité", ProjectMember.ProjectRole.QUALITY_MANAGER);
        seedProjectMember(project, "SUP-001", "Superviseur Usine", ProjectMember.ProjectRole.PROJECT_MANAGER);
        seedProjectMember(project, "RHSE-001", "Responsable HSE", ProjectMember.ProjectRole.MEMBER);

        if (operatorRepository.count() > 0) {
            return; // Already seeded, prevent duplicates
        }

        // Seed Zones from OPmobility Formulaire
        Zone zoneSC = seedZone(project, "Zone SC");
        Zone zoneSQ52 = seedZone(project, "Zone SQ52");
        Zone zoneX52 = seedZone(project, "Zone X52");
        Zone zoneCMP = seedZone(project, "Zone CMP");
        Zone zoneKJ = seedZone(project, "Zone KJ");

        // Seed 15 Workstations from OPmobility Formulaire
        Workstation wsRacSc = seedWorkstation("RAC SC", zoneSC, "L", 100, 0);
        Workstation wsCecSc = seedWorkstation("CéC SC", zoneSC, "L", 100, 0);

        Workstation wsPdrSq52 = seedWorkstation("PDR SQ52", zoneSQ52, "L", 100, 0);
        Workstation wsRacSq52 = seedWorkstation("RAC SQ52", zoneSQ52, "L", 100, 0);
        Workstation wsCacSq52L1 = seedWorkstation("CAC SQ52 L1", zoneSQ52, "L", 100, 0);
        Workstation wsCacSq52L2 = seedWorkstation("CAC SQ52 L2", zoneSQ52, "L", 100, 0);

        Workstation wsRecX52 = seedWorkstation("RÉC X52", zoneX52, "L", 100, 0);
        Workstation wsCecX52 = seedWorkstation("CéC X52", zoneX52, "L", 100, 0);

        Workstation wsPdrCmp = seedWorkstation("PDR CMP", zoneCMP, "L", 100, 0);
        Workstation wsRacCmp = seedWorkstation("RAC CMP", zoneCMP, "L", 100, 0);
        Workstation wsCacCmp = seedWorkstation("CAC CMP", zoneCMP, "L", 100, 0);

        Workstation wsPdrKj = seedWorkstation("PDR KJ", zoneKJ, "L", 100, 0);
        Workstation wsRecKjMono = seedWorkstation("RÉC KJ MONO", zoneKJ, "L", 100, 0);
        Workstation wsRacKjEvap = seedWorkstation("RAC KJ EVAP", zoneKJ, "L", 100, 0);
        Workstation wsCacKj = seedWorkstation("CAC KJ", zoneKJ, "L", 100, 0);

        // Seed Evaluation Templates
        EvaluationTemplate genericTpl = new EvaluationTemplate();
        genericTpl.setName("Evaluation Générique Tronc Commun");
        genericTpl.setType(EvaluationTemplate.TemplateType.GENERIC_COMMON);
        genericTpl.setStatus(EvaluationTemplate.TemplateStatus.VALIDATED);
        genericTpl = templateRepository.save(genericTpl);

        EvaluationSection genericSec = new EvaluationSection();
        genericSec.setTemplate(genericTpl);
        genericSec.setTitle("Sécurité & Tronc Commun");
        genericSec = sectionRepository.save(genericSec);

        createQuestion(genericTpl, genericSec, 1, "L'opérateur respecte-t-il le port des EPI obligatoires ?", "Oui", EvaluationQuestion.ValidatorRole.RESP_HSE, EvaluationQuestion.QuestionStatus.VALIDATED);
        createQuestion(genericTpl, genericSec, 2, "L'opérateur applique-t-il les standards 5S ?", "Oui", EvaluationQuestion.ValidatorRole.CHEF_EQUIPE, EvaluationQuestion.QuestionStatus.VALIDATED);
        createQuestion(genericTpl, genericSec, 3, "L'opérateur sait-il identifier une pièce non conforme ?", "Oui", EvaluationQuestion.ValidatorRole.AGENT_QUALITE, EvaluationQuestion.QuestionStatus.VALIDATED);

        EvaluationTemplate coolingTpl = new EvaluationTemplate();
        coolingTpl.setName("Evaluation - POST COOLING 50962");
        coolingTpl.setType(EvaluationTemplate.TemplateType.POSTE_PRODUCTION);
        coolingTpl.setStatus(EvaluationTemplate.TemplateStatus.VALIDATED);
        coolingTpl.setWorkstation(wsRacSc);
        coolingTpl = templateRepository.save(coolingTpl);

        EvaluationSection coolingSec = new EvaluationSection();
        coolingSec.setTemplate(coolingTpl);
        coolingSec.setTitle("Process & Qualité RAC SC");
        coolingSec = sectionRepository.save(coolingSec);

        createQuestion(coolingTpl, coolingSec, 1, "L'opérateur maîtrise-t-il les standards d'assemblage RAC SC ?", "Oui", EvaluationQuestion.ValidatorRole.CHEF_EQUIPE, EvaluationQuestion.QuestionStatus.VALIDATED);
        createQuestion(coolingTpl, coolingSec, 2, "L'opérateur inspecte-t-il les aspects visuels de la tubulure ?", "Oui", EvaluationQuestion.ValidatorRole.AGENT_QUALITE, EvaluationQuestion.QuestionStatus.VALIDATED);

        // Seed a DRAFT template for CéC SC to test question validation workflow
        EvaluationTemplate cuttingTpl = new EvaluationTemplate();
        cuttingTpl.setName("Evaluation - CéC SC");
        cuttingTpl.setType(EvaluationTemplate.TemplateType.POSTE_PRODUCTION);
        cuttingTpl.setStatus(EvaluationTemplate.TemplateStatus.DRAFT);
        cuttingTpl.setWorkstation(wsCecSc);
        cuttingTpl = templateRepository.save(cuttingTpl);

        EvaluationSection cuttingSec = new EvaluationSection();
        cuttingSec.setTemplate(cuttingTpl);
        cuttingSec.setTitle("Process & Qualité Découpe");
        cuttingSec = sectionRepository.save(cuttingSec);

        createQuestion(cuttingTpl, cuttingSec, 1, "L'opérateur respecte-t-il le standard de découpe ?", "Oui", EvaluationQuestion.ValidatorRole.CHEF_EQUIPE, EvaluationQuestion.QuestionStatus.PENDING);
        createQuestion(cuttingTpl, cuttingSec, 2, "L'opérateur contrôle-t-il les bavures de découpe ?", "Oui", EvaluationQuestion.ValidatorRole.AGENT_QUALITE, EvaluationQuestion.QuestionStatus.PENDING);

        // ==================== 8 OPERATORS FOR TEST CASES ====================

        // Case 1: Operator in Onboarding (Phase 1 - Incomplete)
        Operator op1 = createOperator("OP001", "Alami", "Youssef", "Opérateur", LocalDate.now().minusDays(15), Operator.OperatorType.NOUVEAU_RECRU, project, zoneSC);
        seedOnboardingModule(op1.getId(), "Présentation RH", "RH001");
        seedOnboardingModule(op1.getId(), "EHS Induction", "HSE001");

        // Case 2: Operator Ready for Practical Training (Phase 1 - 100% Onboarded)
        Operator op2 = createOperator("OP002", "Berrada", "Selma", "Opérateur", LocalDate.now().minusDays(20), Operator.OperatorType.NOUVEAU_RECRU, project, zoneSC);
        seedOnboardingModule(op2.getId(), "Présentation RH", "RH001");
        seedOnboardingModule(op2.getId(), "Code de conduite", "RH001");
        seedOnboardingModule(op2.getId(), "Réglement interne", "RH001");
        seedOnboardingModule(op2.getId(), "Cybersecurity", "RH001");
        seedOnboardingModule(op2.getId(), "EHS Induction", "HSE001");
        seedOnboardingModule(op2.getId(), "6NN", "HSE001");
        seedOnboardingModule(op2.getId(), "Fire awareness", "HSE001");
        seedOnboardingModule(op2.getId(), "LOTO", "HSE001");
        seedOnboardingModule(op2.getId(), "Stop 5", "HSE001");
        seedOnboardingModule(op2.getId(), "Standard Work Instructions", "PROC001");
        seedOnboardingModule(op2.getId(), "Finition", "PROC001");
        seedOnboardingModule(op2.getId(), "Assemblage", "PROC001");
        seedOnboardingModule(op2.getId(), "HLT", "PROC001");
        seedOnboardingModule(op2.getId(), "POKE YOKE", "PROC001");
        seedOnboardingModule(op2.getId(), "Soudeur Robot", "PROC001");
        seedOnboardingModule(op2.getId(), "DASIP", "PROC001");
        seedOnboardingModule(op2.getId(), "Mode escalade", "QUAL001");
        seedOnboardingModule(op2.getId(), "Traitement non conformités", "QUAL001");
        seedOnboardingModule(op2.getId(), "Fiches SATO", "QUAL001");
        seedOnboardingModule(op2.getId(), "Modules qualité", "QUAL001");
        seedOnboardingModule(op2.getId(), "SR", "QUAL001");
        seedOnboardingModule(op2.getId(), "TPM", "MAINT001");
        seedOnboardingModule(op2.getId(), "5S", "CHEF001");
        seedOnboardingModule(op2.getId(), "PES", "DGT001");

        // Case 3: Operator in Active 12-Day Training (Phase 2 - In Progress)
        Operator op3 = createOperator("OP003", "Tazi", "Mehdi", "Opérateur", LocalDate.now().minusDays(30), Operator.OperatorType.NOUVEAU_RECRU, project, zoneSC);
        WorkstationFormation form3 = new WorkstationFormation();
        form3.setOperator(op3);
        form3.setWorkstation(wsRacSc);
        form3.setStatus("IN_PROGRESS");
        form3.setStartDate(LocalDate.now().minusDays(5));
        form3 = formationRepository.save(form3);
        for (int d = 1; d <= 5; d++) {
            DailyFormationTracking tracking = new DailyFormationTracking();
            tracking.setFormation(form3);
            tracking.setDayNumber(d);
            tracking.setTrackingDate(LocalDate.now().minusDays(6 - d));
            tracking.setCadence(105);
            tracking.setDefauts(1);
            tracking.setComment("Très bonne cadence, opérateur appliqué.");
            dailyTrackingRepository.save(tracking);
        }

        // Case 4: Operator Completed Training Successfully (Phase 2 - Passed, Level L Certified)
        Operator op4 = createOperator("OP004", "Chraibi", "Amina", "Opérateur", LocalDate.now().minusMonths(8), Operator.OperatorType.NOUVEAU_RECRU, project, zoneSC);
        WorkstationFormation form4 = new WorkstationFormation();
        form4.setOperator(op4);
        form4.setWorkstation(wsRacSc);
        form4.setStatus("COMPLETED");
        form4.setStartDate(LocalDate.now().minusDays(20));
        form4.setEndDate(LocalDate.now().minusDays(8));
        formationRepository.save(form4);
        EvaluationSession session4 = new EvaluationSession();
        session4.setOperator(op4);
        session4.setTemplate(coolingTpl);
        session4.setStatus(EvaluationSession.SessionStatus.PASSED);
        session4.setDecision("PASSED_POSTE");
        session4.setNiveau("L");
        session4.setScorePercentage(100.0);
        session4.setGenericPercentage(100.0);
        session4.setProductionPercentage(100.0);
        session4.setEvaluatorName("Chef d'Équipe");
        session4.setCompletedAt(LocalDateTime.now().minusDays(8));
        sessionRepository.save(session4);
        seedPassedGenericEvaluation(op4, genericTpl);

        // Case 5: Operator Failed 1st Attempt (Phase 2 - Failed -> Seconde Chance Formation Active)
        Operator op5 = createOperator("OP005", "Kabbaj", "Hamza", "Opérateur", LocalDate.now().minusDays(50), Operator.OperatorType.NOUVEAU_RECRU, project, zoneSC);
        WorkstationFormation form5Failed = new WorkstationFormation();
        form5Failed.setOperator(op5);
        form5Failed.setWorkstation(wsRacSc);
        form5Failed.setStatus("FAILED");
        form5Failed.setStartDate(LocalDate.now().minusDays(35));
        form5Failed.setEndDate(LocalDate.now().minusDays(20));
        formationRepository.save(form5Failed);
        WorkstationFormation form5Retry = new WorkstationFormation();
        form5Retry.setOperator(op5);
        form5Retry.setWorkstation(wsRacSc);
        form5Retry.setStatus("IN_PROGRESS");
        form5Retry.setStartDate(LocalDate.now().minusDays(10));
        formationRepository.save(form5Retry);

        // Case 6: Operator in Double Failure (Phase 3 - Locked / Blocked)
        Operator op6 = createOperator("OP006", "El Amrani", "Karim", "Opérateur", LocalDate.now().minusDays(60), Operator.OperatorType.NOUVEAU_RECRU, project, zoneSC);
        WorkstationFormation form6Failed1 = new WorkstationFormation();
        form6Failed1.setOperator(op6);
        form6Failed1.setWorkstation(wsCecSc);
        form6Failed1.setStatus("FAILED");
        form6Failed1.setStartDate(LocalDate.now().minusDays(50));
        form6Failed1.setEndDate(LocalDate.now().minusDays(35));
        formationRepository.save(form6Failed1);
        WorkstationFormation form6Blocked = new WorkstationFormation();
        form6Blocked.setOperator(op6);
        form6Blocked.setWorkstation(wsCecSc);
        form6Blocked.setStatus("BLOCKED");
        form6Blocked.setStartDate(LocalDate.now().minusDays(30));
        formationRepository.save(form6Blocked);

        // Case 7: Operator Ready for Recyclage (Planned Refresher)
        Operator op7 = createOperator("OP007", "Benjelloun", "Leila", "Opérateur", LocalDate.now().minusYears(2), Operator.OperatorType.DEJA_EN_POSTE, project, zoneSC);
        RecyclagePlanning planning = new RecyclagePlanning();
        planning.setOperator(op7);
        planning.setWorkstation(wsRacSc);
        planning.setType(RecyclagePlanning.PlanningType.RECYCLAGE);
        planning.setScheduledDate(LocalDate.now().plusDays(10));
        planning.setStatus(RecyclagePlanning.PlanningStatus.PLANIFIEE);
        planning.setSource(RecyclagePlanning.PlanningSource.ANNUELLE);
        planning.setProjectId(project.getId());
        recyclagePlanningRepository.save(planning);
        seedPassedGenericEvaluation(op7, genericTpl);

        // Case 8: Level L Operator targeting Level U (Seniority Gate: 13 months seniority)
        Operator op8 = createOperator("OP008", "Sadiki", "Omar", "Opérateur", LocalDate.now().minusMonths(13), Operator.OperatorType.DEJA_EN_POSTE, project, zoneSC);
        EvaluationSession session8 = new EvaluationSession();
        session8.setOperator(op8);
        session8.setTemplate(coolingTpl);
        session8.setStatus(EvaluationSession.SessionStatus.PASSED);
        session8.setDecision("PASSED_POSTE");
        session8.setNiveau("L");
        session8.setScorePercentage(100.0);
        session8.setGenericPercentage(100.0);
        session8.setProductionPercentage(100.0);
        session8.setEvaluatorName("Chef d'Équipe");
        session8.setCompletedAt(LocalDateTime.now().minusMonths(12));
        sessionRepository.save(session8);
        seedPassedGenericEvaluation(op8, genericTpl);
    }

    private void seedProjectMember(Project project, String empId, String name, ProjectMember.ProjectRole role) {
        if (!projectMemberRepository.existsByProjectIdAndEmployeeId(project.getId(), empId)) {
            ProjectMember pm = new ProjectMember();
            pm.setProject(project);
            pm.setEmployeeId(empId);
            pm.setEmployeeName(name);
            pm.setProjectRole(role);
            projectMemberRepository.save(pm);
        }
    }

    private Operator createOperator(String empId, String lastName, String firstName, String role, LocalDate hireDate, Operator.OperatorType type, Project project, Zone zone) {
        Operator op = new Operator();
        op.setEmployeeId(empId);
        op.setLastName(lastName);
        op.setFirstName(firstName);
        op.setRole(role);
        op.setHireDate(hireDate);
        op.setOperatorType(type);
        op.setProject(project);
        op.setZone(zone);
        op.setActive(true);
        return operatorRepository.save(op);
    }

    private Zone seedZone(Project project, String name) {
        return zoneRepository.findByProjectId(project.getId()).stream()
            .filter(z -> z.getName().equalsIgnoreCase(name))
            .findFirst()
            .orElseGet(() -> {
                Zone z = new Zone();
                z.setName(name);
                z.setProject(project);
                return zoneRepository.save(z);
            });
    }

    private Workstation seedWorkstation(String name, Zone zone, String targetLevel, int cadence, int defects) {
        return workstationRepository.findByZoneId(zone.getId()).stream()
            .filter(w -> w.getName().equalsIgnoreCase(name))
            .findFirst()
            .orElseGet(() -> {
                Workstation w = new Workstation();
                w.setName(name);
                w.setZone(zone);
                w.setTargetIluLevel(targetLevel);
                w.setTargetCadence(cadence);
                w.setQualityObjective(defects);
                return workstationRepository.save(w);
            });
    }

    private void seedOnboardingModule(Long operatorId, String moduleName, String validatedBy) {
        OnboardingModule module = onboardingModuleRepository.findAll().stream()
            .filter(m -> m.getName().equalsIgnoreCase(moduleName))
            .findFirst()
            .orElseThrow(() -> new RuntimeException("Module not found in seed: " + moduleName));

        OperatorOnboarding record = new OperatorOnboarding(operatorId, module.getId());
        record.setCompleted(true);
        record.setCompletedDate(LocalDate.now());
        record.setValidatedBy(validatedBy);
        record.setComment("Validé automatiquement lors de la création.");
        operatorOnboardingRepository.save(record);
    }

    /** Creates real generic answers because the production gate checks answers, not only a score field. */
    private void seedPassedGenericEvaluation(Operator operator, EvaluationTemplate genericTemplate) {
        EvaluationSession session = new EvaluationSession();
        session.setOperator(operator);
        session.setTemplate(genericTemplate);
        session.setStatus(EvaluationSession.SessionStatus.PASSED);
        session.setDecision("PASSED_GENERIC");
        session.setNiveau("U");
        session.setGenericTotal(3);
        session.setGenericCorrect(3);
        session.setGenericPercentage(100.0);
        session.setScorePercentage(100.0);
        session.setMode("INITIAL");
        session.setCompletedAt(LocalDateTime.now().minusDays(1));
        session = sessionRepository.save(session);

        for (EvaluationQuestion question : questionRepository.findValidatedQuestionsByTemplate(genericTemplate.getId())) {
            EvaluationAnswer answer = new EvaluationAnswer();
            answer.setSession(session);
            answer.setQuestion(question);
            answer.setAnswer(1);
            answerRepository.save(answer);
        }
    }

    private void createQuestion(EvaluationTemplate template, EvaluationSection section, int number, String text, String expected, EvaluationQuestion.ValidatorRole role, EvaluationQuestion.QuestionStatus status) {
        EvaluationQuestion q = new EvaluationQuestion();
        q.setTemplate(template);
        q.setSection(section);
        q.setQuestionNumber(number);
        q.setQuestionText(text);
        q.setExpectedAnswer(expected);
        q.setValidatorRole(role);
        q.setStatus(status);
        questionRepository.save(q);
    }
}
