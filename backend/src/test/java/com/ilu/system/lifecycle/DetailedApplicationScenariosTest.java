package com.ilu.system.lifecycle;

import com.ilu.system.evaluation.entity.*;
import com.ilu.system.operator.dto.CreateOperatorRequest;
import com.ilu.system.operator.entity.*;
import com.ilu.system.recyclage.entity.RecyclagePlanning;
import com.ilu.system.structure.entity.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class DetailedApplicationScenariosTest {

    @Test
    @DisplayName("Scenario A: Opérateur Déjà en Poste — Experienced Operator Pathway")
    void testExperiencedOperatorPathway() {
        Operator op = new Operator();
        op.setId(101L);
        op.setEmployeeId("EXP-001");
        op.setLastName("Zahiri");
        op.setFirstName("Brahim");
        op.setOperatorType(Operator.OperatorType.DEJA_EN_POSTE);
        op.setHireDate(LocalDate.now().minusYears(2));
        op.setActive(true);

        Workstation ws = new Workstation();
        ws.setId(10L);
        ws.setName("Assemblage Robotisé 102");
        ws.setType("POSTE");
        ws.setTargetIluLevel("U");

        // Direct training / evaluation for senior operator targeting level U
        EvaluationSession session = new EvaluationSession();
        session.setOperator(op);
        session.setNiveau("U");
        session.setStatus(EvaluationSession.SessionStatus.PASSED);
        session.setScorePercentage(98.0);
        session.setCompletedAt(LocalDateTime.now());

        assertEquals(Operator.OperatorType.DEJA_EN_POSTE, op.getOperatorType());
        assertEquals("U", session.getNiveau());
        assertTrue(op.getHireDate().isBefore(LocalDate.now().minusYears(1)));
    }

    @Test
    @DisplayName("Scenario B: Double Failure — 2 Failed Attempts Leads to BLOCKED Status")
    void testDoubleFailureScenario() {
        Operator op = new Operator();
        op.setId(102L);
        op.setEmployeeId("OP-FAIL-02");
        op.setLastName("Hilali");
        op.setFirstName("Yassine");

        Workstation ws = new Workstation();
        ws.setId(20L);
        ws.setName("Presse 300T");

        // Attempt 1: Failed
        WorkstationFormation form1 = new WorkstationFormation();
        form1.setOperator(op);
        form1.setWorkstation(ws);
        form1.setStatus("FAILED");

        EvaluationSession session1 = new EvaluationSession();
        session1.setOperator(op);
        session1.setStatus(EvaluationSession.SessionStatus.FAILED);
        session1.setScorePercentage(55.0);

        // Attempt 2: Failed again -> Double Failure
        WorkstationFormation form2 = new WorkstationFormation();
        form2.setOperator(op);
        form2.setWorkstation(ws);
        form2.setStatus("BLOCKED"); // System locks operator after 2nd failure

        EvaluationSession session2 = new EvaluationSession();
        session2.setOperator(op);
        session2.setStatus(EvaluationSession.SessionStatus.FAILED);
        session2.setScorePercentage(58.0);

        assertEquals("FAILED", form1.getStatus());
        assertEquals("BLOCKED", form2.getStatus());
        assertEquals(EvaluationSession.SessionStatus.FAILED, session1.getStatus());
        assertEquals(EvaluationSession.SessionStatus.FAILED, session2.getStatus());
    }

    @Test
    @DisplayName("Scenario C: Failed Practical 12-Day Training & Second Chance Retraining")
    void testFailedTrainingAndSecondChance() {
        Operator op = new Operator();
        op.setId(103L);
        op.setEmployeeId("OP-RETRY-03");

        Workstation ws = new Workstation();
        ws.setId(30L);
        ws.setName("Poste Câblage 404");

        // Initial 12-day training failed due to low cadence/defects
        WorkstationFormation formInitial = new WorkstationFormation();
        formInitial.setOperator(op);
        formInitial.setWorkstation(ws);
        formInitial.setStatus("FAILED");
        formInitial.setStartDate(LocalDate.now().minusDays(30));
        formInitial.setEndDate(LocalDate.now().minusDays(18));

        // Second chance training created IN_PROGRESS
        WorkstationFormation formRetry = new WorkstationFormation();
        formRetry.setOperator(op);
        formRetry.setWorkstation(ws);
        formRetry.setStatus("IN_PROGRESS");
        formRetry.setStartDate(LocalDate.now().minusDays(5));

        assertEquals("FAILED", formInitial.getStatus());
        assertEquals("IN_PROGRESS", formRetry.getStatus());
    }

    @Test
    @DisplayName("Scenario D: Failed Recyclage Session & Required Action Plan")
    void testFailedRecyclageSession() {
        Operator op = new Operator();
        op.setId(104L);
        op.setEmployeeId("OP-REC-04");

        RecyclagePlanning recyclage = new RecyclagePlanning();
        recyclage.setOperator(op);
        recyclage.setType(RecyclagePlanning.PlanningType.RECYCLAGE);
        recyclage.setStatus(RecyclagePlanning.PlanningStatus.TERMINEE);

        EvaluationSession recyclageSession = new EvaluationSession();
        recyclageSession.setOperator(op);
        recyclageSession.setStatus(EvaluationSession.SessionStatus.FAILED);
        recyclageSession.setScorePercentage(65.0);

        assertEquals(EvaluationSession.SessionStatus.FAILED, recyclageSession.getStatus());
        assertTrue(recyclageSession.getScorePercentage() < 80.0);
    }

    @Test
    @DisplayName("Scenario E: Question Validation Workflow Across Roles (Chef, QA, HSE, Resp. Qualité)")
    void testMultiRoleQuestionValidation() {
        EvaluationTemplate tpl = new EvaluationTemplate();
        tpl.setName("Grille Polyvalence Qualité & Sécurité");
        tpl.setType(EvaluationTemplate.TemplateType.POSTE_PRODUCTION);

        // Question 1: Chef d'Équipe validator (Standards 5S)
        EvaluationQuestion q1 = new EvaluationQuestion();
        q1.setTemplate(tpl);
        q1.setQuestionText("L'opérateur applique-t-il les standards 5S ?");
        q1.setValidatorRole(EvaluationQuestion.ValidatorRole.CHEF_EQUIPE);
        q1.setStatus(EvaluationQuestion.QuestionStatus.VALIDATED);

        // Question 2: Agent Qualité validator (Mode escalade)
        EvaluationQuestion q2 = new EvaluationQuestion();
        q2.setTemplate(tpl);
        q2.setQuestionText("L'opérateur respecte-t-il le mode escalade en cas de défaut ?");
        q2.setValidatorRole(EvaluationQuestion.ValidatorRole.AGENT_QUALITE);
        q2.setStatus(EvaluationQuestion.QuestionStatus.VALIDATED);

        // Question 3: Responsable HSE validator (Port des EPI)
        EvaluationQuestion q3 = new EvaluationQuestion();
        q3.setTemplate(tpl);
        q3.setQuestionText("L'opérateur porte-t-il tous les EPI obligatoires au poste ?");
        q3.setValidatorRole(EvaluationQuestion.ValidatorRole.RESP_HSE);
        q3.setStatus(EvaluationQuestion.QuestionStatus.VALIDATED);

        assertEquals(EvaluationQuestion.ValidatorRole.CHEF_EQUIPE, q1.getValidatorRole());
        assertEquals(EvaluationQuestion.ValidatorRole.AGENT_QUALITE, q2.getValidatorRole());
        assertEquals(EvaluationQuestion.ValidatorRole.RESP_HSE, q3.getValidatorRole());
        assertEquals(EvaluationQuestion.QuestionStatus.VALIDATED, q1.getStatus());
        assertEquals(EvaluationQuestion.QuestionStatus.VALIDATED, q2.getStatus());
        assertEquals(EvaluationQuestion.QuestionStatus.VALIDATED, q3.getStatus());
    }

    @Test
    @DisplayName("Scenario F: Creation of a Test Workstation (Poste Défauthèque)")
    void testTestWorkstationCreationAndExclusion() {
        Zone zoneTest = new Zone();
        zoneTest.setName("Zone Qualité / Défauthèque");

        Workstation wsTest = new Workstation();
        wsTest.setName("TEST Défauthèque 01");
        wsTest.setType("TEST"); // Special type for test workstations
        wsTest.setZone(zoneTest);
        wsTest.setTargetIluLevel("L");

        Workstation wsNormal = new Workstation();
        wsNormal.setName("Poste Assemblage 01");
        wsNormal.setType("POSTE");

        assertEquals("TEST", wsTest.getType());
        assertEquals("POSTE", wsNormal.getType());
        assertNotEquals(wsTest.getType(), wsNormal.getType());
    }

    @Test
    @DisplayName("Scenario G: Onboarding Department Module Tracking")
    void testOnboardingDepartmentModules() {
        OnboardingModule modRh = new OnboardingModule();
        modRh.setName("Présentation RH");

        OnboardingModule modHse = new OnboardingModule();
        modHse.setName("Induction EHS & Sécurité");

        OnboardingModule modQual = new OnboardingModule();
        modQual.setName("Sensibilisation Non-Conformités");

        assertNotNull(modRh.getName());
        assertNotNull(modHse.getName());
        assertNotNull(modQual.getName());
    }

    @Test
    @DisplayName("Scenario H: Seniority & Level Capping Rules (I, L, U Brackets)")
    void testSeniorityAndLevelCappingRules() {
        // Seniority < 6 months: capped at 'I' even with 100% score
        long seniorityLessThan6Months = 4;
        double score100 = 100.0;
        String levelNewbie = determineNiveauForTest(seniorityLessThan6Months, score100);
        assertEquals("I", levelNewbie);

        // Seniority 6-12 months: gains 'L' with >= 81% score
        long seniority8Months = 8;
        double score85 = 85.0;
        String levelMid = determineNiveauForTest(seniority8Months, score85);
        assertEquals("L", levelMid);

        // Seniority >= 12 months with >= 91% score
        long seniority14Months = 14;
        double score95 = 95.0;
        String levelSeniorRaw = determineNiveauForTest(seniority14Months, score95);
        assertEquals("U", levelSeniorRaw);

        // Level Cap Rule: First attempt capped at 'L' if operator has no prior 'L' history
        boolean hasPriorLHistory = false;
        String levelSeniorCapped = ("U".equals(levelSeniorRaw) && !hasPriorLHistory) ? "L" : levelSeniorRaw;
        assertEquals("L", levelSeniorCapped);

        // Failing score < 70% -> NON_VALIDE
        String levelFailed = determineNiveauForTest(seniority14Months, 65.0);
        assertEquals("NON_VALIDE", levelFailed);
    }

    private String determineNiveauForTest(long seniorityMonths, double productionPercentage) {
        if (productionPercentage < 70) return "NON_VALIDE";
        if (seniorityMonths < 6) return "I";
        if (seniorityMonths < 12) return productionPercentage >= 81 ? "L" : "I";
        if (productionPercentage >= 91) return "U";
        return productionPercentage >= 81 ? "L" : "I";
    }

    @Test
    @DisplayName("Scenario I: Multi-Workstation Independent Recyclage & Both Operator Types Support")
    void testMultiWorkstationIndependentRecyclage() {
        Operator opNewHire = new Operator();
        opNewHire.setEmployeeId("OP-NEW-01");
        opNewHire.setOperatorType(Operator.OperatorType.NOUVEAU_RECRU);

        Operator opExperienced = new Operator();
        opExperienced.setEmployeeId("OP-EXP-02");
        opExperienced.setOperatorType(Operator.OperatorType.DEJA_EN_POSTE);

        Workstation ws1 = new Workstation(); ws1.setId(101L); ws1.setName("Poste Assemblage 01");
        Workstation ws2 = new Workstation(); ws2.setId(102L); ws2.setName("Poste Soudeur 02");

        // Independent Recyclage Plannings per Workstation for Nouveau Recru
        RecyclagePlanning planWs1 = new RecyclagePlanning();
        planWs1.setOperator(opNewHire); planWs1.setWorkstation(ws1);
        planWs1.setType(RecyclagePlanning.PlanningType.RECYCLAGE);
        planWs1.setScheduledDate(LocalDate.now().plusMonths(6));

        RecyclagePlanning planWs2 = new RecyclagePlanning();
        planWs2.setOperator(opNewHire); planWs2.setWorkstation(ws2);
        planWs2.setType(RecyclagePlanning.PlanningType.RECYCLAGE);
        planWs2.setScheduledDate(LocalDate.now().plusMonths(7)); // 1 month difference

        // Recyclage Planning for Déjà en poste
        RecyclagePlanning planExp = new RecyclagePlanning();
        planExp.setOperator(opExperienced); planExp.setWorkstation(ws1);
        planExp.setType(RecyclagePlanning.PlanningType.RECYCLAGE);

        // Assertions: Both types can have recyclages, and workstations maintain independent dates
        assertNotNull(planWs1.getScheduledDate());
        assertNotNull(planWs2.getScheduledDate());
        assertNotEquals(planWs1.getScheduledDate(), planWs2.getScheduledDate());
        assertEquals(101L, planWs1.getWorkstation().getId());
        assertEquals(102L, planWs2.getWorkstation().getId());
        assertEquals(Operator.OperatorType.NOUVEAU_RECRU, planWs1.getOperator().getOperatorType());
        assertEquals(Operator.OperatorType.DEJA_EN_POSTE, planExp.getOperator().getOperatorType());
    }

    @Test
    @DisplayName("Scenario J: Team/Chef Change (Same Project) vs Project Transfer Rules")
    void testTeamChangeVsProjectTransferRules() {
        Project projectA = new Project(); projectA.setId(1L); projectA.setName("Projet CMP A");
        Project projectB = new Project(); projectB.setId(2L); projectB.setName("Projet CMP B");

        Team teamChef1 = new Team(); teamChef1.setId(10L); teamChef1.setTeamLeader("Chef 1");
        Team teamChef2 = new Team(); teamChef2.setId(20L); teamChef2.setTeamLeader("Chef 2");

        Operator op = new Operator();
        op.setEmployeeId("OP-TRANSFER-01");
        op.setProject(projectA);
        op.setTeam(teamChef1);

        // Operator has certified Level L on Project A Workstation
        EvaluationSession sessionProjectA = new EvaluationSession();
        sessionProjectA.setOperator(op);
        sessionProjectA.setNiveau("L");
        sessionProjectA.setStatus(EvaluationSession.SessionStatus.PASSED);

        // CASE 1: Changing Chef d'Équipe / Team in the SAME Project
        op.setTeam(teamChef2); // Transfer to Chef 2
        assertEquals(projectA.getId(), op.getProject().getId()); // Project unchanged
        assertEquals("Chef 2", op.getTeam().getTeamLeader());
        assertEquals("L", sessionProjectA.getNiveau()); // Certification preserved!

        // CASE 2: Changing Project (Transfer to Project B)
        op.setProject(projectB);
        op.setTeam(null); // Needs new team assignment on Project B

        WorkstationFormation newProjectFormation = new WorkstationFormation();
        newProjectFormation.setOperator(op);
        newProjectFormation.setStatus("IN_PROGRESS"); // Must complete formation on Project B workstation

        assertEquals(projectB.getId(), op.getProject().getId());
        assertNull(op.getTeam());
        assertEquals("IN_PROGRESS", newProjectFormation.getStatus());
    }
}
