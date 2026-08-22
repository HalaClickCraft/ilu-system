package com.ilu.system.evaluation.service;

import com.ilu.system.auth.entity.User;
import com.ilu.system.auth.repository.UserRepository;
import com.ilu.system.evaluation.entity.*;
import com.ilu.system.evaluation.repository.*;
import com.ilu.system.operator.entity.FormationAssignment;
import com.ilu.system.operator.entity.Operator;
import com.ilu.system.operator.entity.WorkstationFormation;
import com.ilu.system.operator.repository.FormationAssignmentRepository;
import com.ilu.system.operator.repository.OperatorRepository;
import com.ilu.system.operator.repository.WorkstationFormationRepository;
import com.ilu.system.structure.entity.Project;
import com.ilu.system.structure.entity.Workstation;
import com.ilu.system.structure.repository.ProjectRepository;
import com.ilu.system.structure.repository.WorkstationRepository;
import com.ilu.system.recyclage.entity.RecyclagePlanning;
import com.ilu.system.recyclage.repository.RecyclagePlanningRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.*;
import java.util.stream.Collectors;


@Service
public class EvaluationService {

    private final EvaluationTemplateRepository templateRepo;
    private final EvaluationSectionRepository sectionRepo;
    private final EvaluationQuestionRepository questionRepo;
    private final EvaluationSessionRepository sessionRepo;
    private final EvaluationAnswerRepository answerRepo;
    private final OperatorRepository operatorRepo;
    private final WorkstationRepository workstationRepo;
    private final FormationAssignmentRepository assignmentRepo;
    private final WorkstationFormationRepository formationRepo;
    private final UserRepository userRepo;
    private final ProjectRepository projectRepo;
    private final RecyclagePlanningRepository recyclagePlanningRepo;

    public EvaluationService(EvaluationTemplateRepository templateRepo,
                             EvaluationSectionRepository sectionRepo,
                             EvaluationQuestionRepository questionRepo,
                             EvaluationSessionRepository sessionRepo,
                             EvaluationAnswerRepository answerRepo,
                             OperatorRepository operatorRepo,
                             WorkstationRepository workstationRepo,
                             FormationAssignmentRepository assignmentRepo,
                             WorkstationFormationRepository formationRepo,
                             UserRepository userRepo,
                             ProjectRepository projectRepo,
                             RecyclagePlanningRepository recyclagePlanningRepo) {
        this.templateRepo = templateRepo;
        this.sectionRepo = sectionRepo;
        this.questionRepo = questionRepo;
        this.sessionRepo = sessionRepo;
        this.answerRepo = answerRepo;
        this.operatorRepo = operatorRepo;
        this.workstationRepo = workstationRepo;
        this.assignmentRepo = assignmentRepo;
        this.formationRepo = formationRepo;
        this.userRepo = userRepo;
        this.projectRepo = projectRepo;
        this.recyclagePlanningRepo = recyclagePlanningRepo;
    }

    // ======================== TEMPLATE CRUD ========================

    @Transactional
    public Map<String, Object> createTemplate(String name, String description, String typeStr,
                                               Long workstationId, String targetNiveau, Long createdById) {
        EvaluationTemplate.TemplateType type;
        try {
            type = EvaluationTemplate.TemplateType.valueOf(typeStr);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Type invalide: " + typeStr);
        }

        EvaluationTemplate template = new EvaluationTemplate();
        template.setName(name);
        template.setDescription(description);
        template.setType(type);
        template.setTargetNiveau(targetNiveau);
        template.setCreatedById(createdById);

        if (workstationId != null && (type == EvaluationTemplate.TemplateType.POSTE_PRODUCTION
                || type == EvaluationTemplate.TemplateType.ANIMATION)) {
            Workstation ws = workstationRepo.findById(workstationId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Poste introuvable"));
            template.setWorkstation(ws);
        }

        templateRepo.save(template);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", template.getId());
        result.put("name", template.getName());
        result.put("type", template.getType().name());
        result.put("status", template.getStatus().name());
        result.put("message", "Template cree avec succes");
        return result;
    }

    @Transactional
    public Map<String, Object> addSection(Long templateId, String title, Integer displayOrder, String complementaryQuestions) {
        EvaluationTemplate template = templateRepo.findById(templateId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Template introuvable"));

        EvaluationSection section = new EvaluationSection();
        section.setTemplate(template);
        section.setTitle(title);
        section.setComplementaryQuestions(complementaryQuestions);
        section.setDisplayOrder(displayOrder != null ? displayOrder : (template.getSections() != null ? template.getSections().size() : 0) + 1);
        sectionRepo.save(section);

        if (template.getSections() == null) {
            template.setSections(new ArrayList<>());
        }
        template.getSections().add(section);
        templateRepo.save(template);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", section.getId());
        result.put("title", section.getTitle());
        result.put("displayOrder", section.getDisplayOrder());
        result.put("templateId", templateId);
        return result;
    }

    @Transactional
    public Map<String, Object> addQuestion(Long templateId, Long sectionId, String questionText,
                                            String expectedAnswer, Integer questionNumber,
                                            String validatorRoleStr, String complementaryQuestions,
                                            Long createdById) {
        EvaluationTemplate template = templateRepo.findById(templateId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Template introuvable"));

        EvaluationQuestion.ValidatorRole validatorRole = resolveValidatorRole(createdById, validatorRoleStr);
        boolean needsResponsibleValidation = validatorRole == EvaluationQuestion.ValidatorRole.AGENT_QUALITE;

        EvaluationQuestion question = new EvaluationQuestion();
        question.setTemplate(template);
        question.setQuestionText(questionText);
        question.setExpectedAnswer(expectedAnswer);
        question.setComplementaryQuestions(complementaryQuestions);
        question.setQuestionNumber(questionNumber);
        question.setValidatorRole(validatorRole);
        question.setCreatedById(createdById);

        if (sectionId != null) {
            EvaluationSection section = sectionRepo.findById(sectionId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Section introuvable"));
            question.setSection(section);
        }

        question.setStatus(needsResponsibleValidation
                ? EvaluationQuestion.QuestionStatus.PENDING
                : EvaluationQuestion.QuestionStatus.VALIDATED);
        if (!needsResponsibleValidation) {
            question.setValidatedById(createdById);
        }
        questionRepo.save(question);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", question.getId());
        result.put("questionText", question.getQuestionText());
        result.put("validatorRole", question.getValidatorRole().name());
        result.put("status", question.getStatus().name());
        result.put("message", "Question creee en attente de validation");
        return result;
    }

    @Transactional
    public Map<String, Object> updateQuestion(Long questionId, String questionText,
                                               String expectedAnswer, String validatorRoleStr,
                                               Integer questionNumber, Long sectionId, Long templateId,
                                               String complementaryQuestions) {
        EvaluationQuestion question = questionRepo.findById(questionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Question introuvable"));

        if (questionText != null) {
            question.setQuestionText(questionText);
        }
        if (expectedAnswer != null) {
            question.setExpectedAnswer(expectedAnswer);
        }
        if (complementaryQuestions != null) {
            question.setComplementaryQuestions(complementaryQuestions);
        }
        if (questionNumber != null) {
            question.setQuestionNumber(questionNumber);
        }
        if (validatorRoleStr != null) {
            try {
                question.setValidatorRole(EvaluationQuestion.ValidatorRole.valueOf(validatorRoleStr));
            } catch (Exception e) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Role validateur invalide: " + validatorRoleStr);
            }
        }
        if (question.getValidatorRole() == EvaluationQuestion.ValidatorRole.AGENT_QUALITE) {
            if (question.getStatus() != EvaluationQuestion.QuestionStatus.PENDING &&
                    question.getStatus() != EvaluationQuestion.QuestionStatus.REJECTED) {
                question.setStatus(EvaluationQuestion.QuestionStatus.PENDING);
            }
        } else {
            question.setStatus(EvaluationQuestion.QuestionStatus.VALIDATED);
            question.setValidatedById(question.getCreatedById());
        }
        if (sectionId != null) {
            EvaluationSection section = sectionRepo.findById(sectionId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Section introuvable"));
            question.setSection(section);
        }
        questionRepo.save(question);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", question.getId());
        result.put("questionText", question.getQuestionText());
        result.put("expectedAnswer", question.getExpectedAnswer());
        result.put("questionNumber", question.getQuestionNumber());
        result.put("validatorRole", question.getValidatorRole().name());
        result.put("status", question.getStatus().name());
        result.put("message", "Question mise a jour avec succes");
        return result;
    }

    @Transactional
    public Map<String, Object> deleteQuestion(Long questionId, Long templateId) {
        EvaluationQuestion question = questionRepo.findById(questionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Question introuvable"));

        answerRepo.deleteByQuestionId(questionId);
        questionRepo.delete(question);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", questionId);
        result.put("message", "Question supprimee avec succes");
        return result;
    }

    // ======================== QUESTION VALIDATION ========================

    @Transactional
    public Map<String, Object> validateQuestion(Long questionId, Long validatedById) {
        EvaluationQuestion question = questionRepo.findById(questionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Question introuvable"));

        if (question.getStatus() != EvaluationQuestion.QuestionStatus.PENDING) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cette question n'est plus en attente");
        }

        question.setStatus(EvaluationQuestion.QuestionStatus.VALIDATED);
        question.setValidatedById(validatedById);
        questionRepo.save(question);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", question.getId());
        result.put("status", "VALIDATED");
        result.put("message", "Question validee avec succes");
        return result;
    }

    @Transactional
    public Map<String, Object> rejectQuestion(Long questionId, Long validatedById, String reason) {
        EvaluationQuestion question = questionRepo.findById(questionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Question introuvable"));

        if (question.getStatus() != EvaluationQuestion.QuestionStatus.PENDING) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cette question n'est plus en attente");
        }

        question.setStatus(EvaluationQuestion.QuestionStatus.REJECTED);
        question.setValidatedById(validatedById);
        question.setRejectionReason(reason);
        questionRepo.save(question);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", question.getId());
        result.put("status", "REJECTED");
        result.put("message", "Question rejetee");
        return result;
    }

    // ======================== TEMPLATE VALIDATION ========================

    @Transactional
    public Map<String, Object> validateTemplate(Long templateId, Long validatedById) {
        EvaluationTemplate template = templateRepo.findById(templateId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Template introuvable"));

        List<EvaluationQuestion> questions = questionRepo.findByTemplateId(templateId);
        if (questions.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Le template doit contenir au moins une question avant validation");
        }

        List<EvaluationQuestion.ValidatorRole> requiredRoles = getRequiredContributorRoles(template.getType());
        List<String> pendingRoles = new ArrayList<>();
        for (EvaluationQuestion.ValidatorRole role : requiredRoles) {
            long validatedForRole = questions.stream()
                    .filter(q -> q.getValidatorRole() == role)
                    .filter(q -> q.getStatus() == EvaluationQuestion.QuestionStatus.VALIDATED)
                    .count();
            long pendingForRole = questions.stream()
                    .filter(q -> q.getValidatorRole() == role)
                    .filter(q -> q.getStatus() == EvaluationQuestion.QuestionStatus.PENDING)
                    .count();
            if (validatedForRole == 0 || pendingForRole > 0) {
                pendingRoles.add(role.name());
            }
        }

        if (!pendingRoles.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "En attente de questions de: " + String.join(", ", pendingRoles));
        }

        long validatedCount = questions.stream()
                .filter(q -> q.getStatus() == EvaluationQuestion.QuestionStatus.VALIDATED).count();
        if (validatedCount == 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Le template doit avoir au moins une question validee");
        }

        template.setStatus(EvaluationTemplate.TemplateStatus.VALIDATED);
        templateRepo.save(template);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", template.getId());
        result.put("status", "VALIDATED");
        result.put("validatedQuestions", validatedCount);
        result.put("message", "Template valide avec succes");
        return result;
    }

    // ======================== EVALUATION SESSION ========================

    @Transactional
    public Map<String, Object> startEvaluation(Long operatorId, Long templateId,
                                                Long formationId, Long evaluatorId,
                                                String mode, Long nextTemplateId, Long planningId) {
        Operator operator = operatorRepo.findById(operatorId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Operateur introuvable"));

        EvaluationTemplate template = templateRepo.findById(templateId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Template introuvable"));

        if (template.getStatus() != EvaluationTemplate.TemplateStatus.VALIDATED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Le template doit etre valide");
        }

        FormationAssignment formation = null;
        WorkstationFormation workstationFormation = null;
        if (formationId != null) {
            workstationFormation = formationRepo.findById(formationId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Formation pratique introuvable"));
            if (!operatorId.equals(workstationFormation.getOperator().getId())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La formation ne correspond pas a cet operateur");
            }
            if (!"COMPLETED".equals(workstationFormation.getStatus())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La formation pratique doit etre terminee avant l'evaluation");
            }
            if (template.getWorkstation() != null
                    && !template.getWorkstation().getId().equals(workstationFormation.getWorkstation().getId())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Le template ne correspond pas au poste de la formation");
            }
        }
        RecyclagePlanning planning = null;
        if (planningId != null) {
            planning = recyclagePlanningRepo.findById(planningId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Planning introuvable"));
            if (!operatorId.equals(planning.getOperator().getId())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Le planning ne correspond pas a cet operateur");
            }
            if (template.getWorkstation() != null
                    && !template.getWorkstation().getId().equals(planning.getWorkstation().getId())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Le template ne correspond pas au poste planifie");
            }
            if (planning.getStatus() != RecyclagePlanning.PlanningStatus.PLANIFIEE
                    && planning.getStatus() != RecyclagePlanning.PlanningStatus.EN_COURS) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Le planning n'est pas executable");
            }
        }

        long seniorityMonths = 0;
        if (operator.getHireDate() != null) {
            seniorityMonths = Period.between(operator.getHireDate(), LocalDate.now()).toTotalMonths();
        }

        // Check for existing IN_PROGRESS session for same operator + template to prevent duplicates
        Optional<EvaluationSession> existingSession = sessionRepo.findByOperatorIdOrderByCreatedAtDesc(operatorId).stream()
                .filter(s -> s.getTemplate().getId().equals(templateId)
                        && s.getStatus() == EvaluationSession.SessionStatus.IN_PROGRESS)
                .findFirst();

        if (existingSession.isPresent()) {
                EvaluationSession es = existingSession.get();
                Map<String, Object> existingResult = new LinkedHashMap<>();
                existingResult.put("sessionId", es.getId());
                existingResult.put("operatorName", operator.getLastName() + " " + operator.getFirstName());
                existingResult.put("templateName", template.getName());
                existingResult.put("templateType", template.getType().name());
                existingResult.put("totalQuestions", questionRepo.findValidatedQuestionsByTemplate(templateId).size());
                existingResult.put("seniorityMonths", seniorityMonths);
                existingResult.put("status", "IN_PROGRESS");
                existingResult.put("mode", es.getMode());
                existingResult.put("nextTemplateId", es.getNextTemplateId());
                existingResult.put("resumed", true);
                existingResult.put("message", "Session existante reprise");
                return existingResult;
        }

        EvaluationSession session = new EvaluationSession();
        session.setOperator(operator);
        session.setTemplate(template);
        session.setFormation(formation);
        session.setEvaluatorId(evaluatorId);
        session.setOperatorSeniorityMonths(seniorityMonths);
        session.setMode(mode);
        session.setNextTemplateId(nextTemplateId);
        session.setPlanningId(planningId);
        session.setWorkstationFormationId(workstationFormation != null ? workstationFormation.getId() : null);
        session.setStatus(EvaluationSession.SessionStatus.IN_PROGRESS);
        sessionRepo.save(session);

        markPlanningInProgress(planning, operatorId, template.getWorkstation());

        String evaluatorName = userRepo.findById(evaluatorId)
                .map(User::getName).orElse("Inconnu");
        session.setEvaluatorName(evaluatorName);
        sessionRepo.save(session);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("sessionId", session.getId());
        result.put("operatorName", operator.getLastName() + " " + operator.getFirstName());
        result.put("templateName", template.getName());
        result.put("templateType", template.getType().name());
        result.put("totalQuestions", questionRepo.findValidatedQuestionsByTemplate(templateId).size());
        result.put("seniorityMonths", seniorityMonths);
        result.put("status", "IN_PROGRESS");
        result.put("mode", mode);
        result.put("nextTemplateId", nextTemplateId);
        result.put("message", "Evaluation demarree");
        return result;
    }

    @Transactional
    public Map<String, Object> submitAnswers(Long sessionId, List<Map<String, Object>> answersData,
                                              Long answeredById) {
        EvaluationSession session = sessionRepo.findById(sessionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Session introuvable"));

        if (session.getStatus() != EvaluationSession.SessionStatus.IN_PROGRESS) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La session n'est plus en cours");
        }

        int savedCount = 0;
        for (Map<String, Object> answerData : answersData) {
            Long questionId = Long.valueOf(answerData.get("questionId").toString());
            Integer answer = Integer.valueOf(answerData.get("answer").toString());
            String comment = answerData.get("comment") != null ? answerData.get("comment").toString() : null;

            if (answer != 0 && answer != 1) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "La reponse doit etre 0 (incorrect) ou 1 (correct)");
            }

            EvaluationQuestion question = questionRepo.findById(questionId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Question introuvable: " + questionId));
            if (!question.getTemplate().getId().equals(session.getTemplate().getId())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "La question n'appartient pas au template de cette session");
            }

            Optional<EvaluationAnswer> existing = answerRepo.findBySessionIdAndQuestionId(sessionId, questionId);
            EvaluationAnswer evalAnswer;
            if (existing.isPresent()) {
                evalAnswer = existing.get();
            } else {
                evalAnswer = new EvaluationAnswer();
                evalAnswer.setSession(session);
                evalAnswer.setQuestion(question);
            }
            evalAnswer.setAnswer(answer);
            evalAnswer.setComment(comment);
            evalAnswer.setAnsweredById(answeredById);
            answerRepo.save(evalAnswer);
            savedCount++;
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("sessionId", sessionId);
        result.put("savedAnswers", savedCount);
        result.put("message", "Reponses enregistrees");
        return result;
    }

    @Transactional
    public Map<String, Object> completeEvaluation(Long sessionId) {
        EvaluationSession session = sessionRepo.findById(sessionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Session introuvable"));

        if (session.getStatus() != EvaluationSession.SessionStatus.IN_PROGRESS) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La session n'est plus en cours");
        }

        List<EvaluationAnswer> answers = answerRepo.findBySessionId(sessionId);
        if (answers.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Aucune reponse enregistree");
        }

        int genericTotal = 0, genericCorrect = 0;
        int productionTotal = 0, productionCorrect = 0;

        for (EvaluationAnswer ans : answers) {
            EvaluationQuestion q = ans.getQuestion();
            int score = ans.getAnswer();
            boolean isGeneric = q.getTemplate().getType() == EvaluationTemplate.TemplateType.GENERIC_COMMON;
            if (isGeneric) {
                genericTotal++;
                genericCorrect += score;
            } else {
                productionTotal++;
                productionCorrect += score;
            }
        }

        int totalQuestions = genericTotal + productionTotal;
        int correctAnswers = genericCorrect + productionCorrect;

        double genericPct = genericTotal > 0 ? (double) genericCorrect / genericTotal * 100.0 : 0.0;
        double productionPct = productionTotal > 0 ? (double) productionCorrect / productionTotal * 100.0 : 0.0;
        double overallPct = totalQuestions > 0 ? (double) correctAnswers / totalQuestions * 100.0 : 0.0;

        session.setGenericTotal(genericTotal);
        session.setGenericCorrect(genericCorrect);
        session.setGenericPercentage(Math.round(genericPct * 10.0) / 10.0);
        session.setProductionTotal(productionTotal);
        session.setProductionCorrect(productionCorrect);
        session.setProductionPercentage(Math.round(productionPct * 10.0) / 10.0);
        session.setTotalQuestions(totalQuestions);
        session.setCorrectAnswers(correctAnswers);
        session.setScorePercentage(Math.round(overallPct * 10.0) / 10.0);

        boolean isGenericTemplate = session.getTemplate().getType() == EvaluationTemplate.TemplateType.GENERIC_COMMON;

        if (isGenericTemplate) {
            if (genericPct < 100.0) {
                session.setStatus(EvaluationSession.SessionStatus.BLOCKED);
                session.setDecision("BLOCKED_GENERIC");
                session.setNiveau("NON_APTE");
            } else {
                session.setStatus(EvaluationSession.SessionStatus.PASSED);
                session.setDecision("PASSED_GENERIC");
                session.setNiveau("U");
            }
            session.setCompletedAt(LocalDateTime.now());
            sessionRepo.save(session);
            return buildResult(session, genericPct < 100.0
                    ? "BLOQUE: La partie generique doit etre a 100%"
                    : "Partie generique reussie a 100%");
        }

        // PRODUCTION template: verify generic was passed, then determine niveau
        boolean genericOk = hasPassedGeneric(session.getOperator().getId());
        if (!genericOk) {
            session.setStatus(EvaluationSession.SessionStatus.BLOCKED);
            session.setDecision("BLOCKED_GENERIC");
            session.setNiveau("NON_APTE");
            session.setCompletedAt(LocalDateTime.now());
            sessionRepo.save(session);
            return buildResult(session, "BLOQUE: L'operateur n'a pas 100% a la partie generique");
        }

        String niveau = determineNiveau(session.getOperatorSeniorityMonths(), productionPct);
        session.setNiveau(niveau);

        if ("NON_VALIDE".equals(niveau)) {
            session.setStatus(EvaluationSession.SessionStatus.FAILED);
            session.setDecision("FAILED");
            session.setCompletedAt(LocalDateTime.now());
            sessionRepo.save(session);
            completeMatchingPlanning(session);
            createSecondChanceFormationAfterEvaluationFailure(session);
            return buildResult(session, "Echec: Score insuffisant pour le niveau correspondant a l'anciennete");
        }

        // An operator who is already L must pass the dedicated Animation
        // questionnaire before their production result can promote them to U.
        if (session.getTemplate().getType() == EvaluationTemplate.TemplateType.POSTE_PRODUCTION
                && "U".equals(niveau)
                && session.getTemplate().getWorkstation() != null
                && hasPassedLevel(session.getOperator().getId(), session.getTemplate().getWorkstation().getId(), "L")) {
            EvaluationTemplate animationTemplate = templateRepo
                    .findValidatedAnimationForWorkstation(session.getTemplate().getWorkstation().getId())
                    .stream()
                    .findFirst()
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "Un template Animation valide est requis pour le passage L vers U sur ce poste"));

            session.setNiveau("L");
            session.setStatus(EvaluationSession.SessionStatus.PASSED);
            session.setDecision("PENDING_ANIMATION");
            session.setNextTemplateId(animationTemplate.getId());
            session.setCompletedAt(LocalDateTime.now());
            sessionRepo.save(session);
            return buildResult(session, "Questionnaire Animation requis avant le passage au niveau U");
        }

        session.setStatus(EvaluationSession.SessionStatus.PASSED);
        session.setDecision("PASSED_" + niveau);
        session.setCompletedAt(LocalDateTime.now());
        sessionRepo.save(session);
        completeMatchingPlanning(session);

        return buildResult(session, "Reussite: Niveau " + niveau + " atteint");
    }

    private void markPlanningInProgress(RecyclagePlanning explicitPlanning, Long operatorId, Workstation workstation) {
        if (explicitPlanning != null) {
            if (explicitPlanning.getStatus() == RecyclagePlanning.PlanningStatus.PLANIFIEE) {
                explicitPlanning.setStatus(RecyclagePlanning.PlanningStatus.EN_COURS);
                recyclagePlanningRepo.save(explicitPlanning);
            }
            return;
        }
        if (workstation == null) return;
        recyclagePlanningRepo.findFirstByOperator_IdAndWorkstation_IdAndStatusOrderByScheduledDateAsc(
                        operatorId, workstation.getId(), RecyclagePlanning.PlanningStatus.PLANIFIEE)
                .ifPresent(planning -> {
                    planning.setStatus(RecyclagePlanning.PlanningStatus.EN_COURS);
                    recyclagePlanningRepo.save(planning);
                });
    }

    private void completeMatchingPlanning(EvaluationSession session) {
        Workstation workstation = session.getTemplate().getWorkstation();
        Optional<RecyclagePlanning> planning = session.getPlanningId() != null
                ? recyclagePlanningRepo.findById(session.getPlanningId()) : Optional.empty();
        if (planning.isEmpty() && workstation == null) return;
        if (planning.isEmpty()) {
            planning = recyclagePlanningRepo.findFirstByOperator_IdAndWorkstation_IdAndStatusOrderByScheduledDateAsc(
                    session.getOperator().getId(), workstation.getId(), RecyclagePlanning.PlanningStatus.EN_COURS);
        }
        if (planning.isEmpty()) {
            planning = recyclagePlanningRepo.findFirstByOperator_IdAndWorkstation_IdAndStatusOrderByScheduledDateAsc(
                    session.getOperator().getId(), workstation.getId(), RecyclagePlanning.PlanningStatus.PLANIFIEE);
        }
        planning.ifPresent(item -> {
            item.setStatus(RecyclagePlanning.PlanningStatus.TERMINEE);
            item.setCompletedAt(LocalDateTime.now());
            item.setNiveauObtenu(session.getNiveau());
            item.setEvaluationSessionId(session.getId());
            recyclagePlanningRepo.save(item);
        });
    }

    private void createSecondChanceFormationAfterEvaluationFailure(EvaluationSession session) {
        Workstation workstation = session.getTemplate().getWorkstation();
        if (workstation == null) return;

        long failures = sessionRepo.findByOperatorIdOrderByCreatedAtDesc(session.getOperator().getId()).stream()
                .filter(candidate -> candidate.getStatus() == EvaluationSession.SessionStatus.FAILED)
                .filter(candidate -> candidate.getTemplate() != null && candidate.getTemplate().getWorkstation() != null)
                .filter(candidate -> workstation.getId().equals(candidate.getTemplate().getWorkstation().getId()))
                .count();
        if (failures != 1 || hasInProgressFormation(session.getOperator().getId(), workstation.getId())) return;

        WorkstationFormation retry = new WorkstationFormation();
        retry.setOperator(session.getOperator());
        retry.setWorkstation(workstation);
        retry.setStartDate(LocalDate.now());
        retry.setStatus("IN_PROGRESS");
        retry.setAchievedLevel("0");
        retry.setTargetLevel(workstation.getTargetIluLevel() != null ? workstation.getTargetIluLevel() : "3");
        retry.setQualityObjective(workstation.getQualityObjective());
        formationRepo.save(retry);
    }

    private boolean hasInProgressFormation(Long operatorId, Long workstationId) {
        return formationRepo.findByOperator_Id(operatorId).stream()
                .anyMatch(formation -> workstationId.equals(formation.getWorkstation().getId())
                        && "IN_PROGRESS".equals(formation.getStatus()));
    }
        // ======================== AUTO TEMPLATE RESOLUTION ========================

    /**
     * For INITIAL evaluation: auto-pick the generic template (one global, no workstation)
     * and the production template (matched by workstation).
     */
    public Map<String, Object> resolveTemplatesForInitial(Long operatorId, Long formationId) {
        Operator operator = operatorRepo.findById(operatorId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Operateur introuvable"));

        WorkstationFormation formation = formationId != null
                ? formationRepo.findById(formationId).orElse(null) : null;

        String workstationName = null;
        Long wsId = null;

        if (formation != null && formation.getWorkstation() != null) {
            workstationName = formation.getWorkstation().getName();
            wsId = formation.getWorkstation().getId();
        } else if (formationId != null) {
            FormationAssignment assignment = assignmentRepo.findById(formationId).orElse(null);
            if (assignment != null && assignment.getWorkstation() != null) {
                workstationName = assignment.getWorkstation().getName();
                wsId = assignment.getWorkstation().getId();
            }
        }

        // 1) Find ONE validated GENERIC_COMMON template (no workstation = global for all postes)
        EvaluationTemplate genericTemplate = null;
        for (EvaluationTemplate t : templateRepo.findAll()) {
            if (t.getType() == EvaluationTemplate.TemplateType.GENERIC_COMMON
                    && t.getStatus() == EvaluationTemplate.TemplateStatus.VALIDATED
                    && t.getWorkstation() == null) {
                genericTemplate = t;
                break;
            }
        }

        if (genericTemplate == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Aucun template generique valide. Creez et validez un template GENERIC_COMMON (sans poste assigne).");
        }

        // 2) Find validated POSTE_PRODUCTION template for the operator's workstation
        EvaluationTemplate productionTemplate = null;
        if (wsId != null) {
            for (EvaluationTemplate t : templateRepo.findAll()) {
                if (t.getType() == EvaluationTemplate.TemplateType.POSTE_PRODUCTION
                        && t.getStatus() == EvaluationTemplate.TemplateStatus.VALIDATED
                        && t.getWorkstation() != null
                        && t.getWorkstation().getId().equals(wsId)) {
                    productionTemplate = t;
                    break;
                }
            }
        }

        if (productionTemplate == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Aucun template de production valide pour le poste '" + (workstationName != null ? workstationName : "inconnu")
                            + "'. Creez et validez un template POSTE_PRODUCTION pour ce poste d'abord.");
        }

        boolean alreadyPassedGeneric = hasPassedGeneric(operatorId);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("operatorId", operatorId);
        result.put("operatorName", operator.getLastName() + " " + operator.getFirstName());
        result.put("workstationName", workstationName);
        result.put("alreadyPassedGeneric", alreadyPassedGeneric);

        if (alreadyPassedGeneric) {
            result.put("startWithProduction", true);
            result.put("productionTemplateId", productionTemplate.getId());
            result.put("productionTemplateName", productionTemplate.getName());
        } else {
            result.put("startWithProduction", false);
            result.put("genericTemplateId", genericTemplate.getId());
            result.put("genericTemplateName", genericTemplate.getName());
            result.put("productionTemplateId", productionTemplate.getId());
            result.put("productionTemplateName", productionTemplate.getName());
        }

        return result;
    }


    // ======================== AUTO-TRIGGER ========================

    public List<Map<String, Object>> getPendingEvaluationsForOperator(Long operatorId) {
        Operator operator = operatorRepo.findById(operatorId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Operateur introuvable"));

        List<WorkstationFormation> completedFormations = formationRepo.findAll().stream()
                .filter(f -> f.getOperator().getId().equals(operatorId)
                        && "COMPLETED".equals(f.getStatus()))
                .collect(Collectors.toList());

        List<Map<String, Object>> pending = new ArrayList<>();
        for (WorkstationFormation f : completedFormations) {
            if (hasPassedInitialEvaluationForWorkstation(operatorId, f)) {
                continue;
            }

            Map<String, Object> item = new LinkedHashMap<>();
            item.put("formationId", f.getId());
            item.put("workstationId", f.getWorkstation().getId());
            item.put("workstationName", f.getWorkstation().getName());
            item.put("formationStartDate", f.getStartDate() != null ? f.getStartDate().toString() : null);
            item.put("formationEndDate", f.getEndDate() != null ? f.getEndDate().toString() : null);
            item.put("hasEvaluation", false);
            item.put("seniorityMonths", operator.getHireDate() != null
                    ? Period.between(operator.getHireDate(), LocalDate.now()).toTotalMonths() : 0);
            pending.add(item);
        }

        return pending;
    }

    public List<Map<String, Object>> getAllPendingEvaluations() {
        List<Operator> operators = operatorRepo.findAll();
        List<Map<String, Object>> allPending = new ArrayList<>();

        for (Operator op : operators) {
            List<WorkstationFormation> completed = formationRepo.findAll().stream()
                    .filter(f -> f.getOperator().getId().equals(op.getId())
                            && "COMPLETED".equals(f.getStatus()))
                    .collect(Collectors.toList());

            for (WorkstationFormation f : completed) {
                if (hasPassedInitialEvaluationForWorkstation(op.getId(), f)) {
                    continue;
                }
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("operatorId", op.getId());
                item.put("operatorName", op.getLastName() + " " + op.getFirstName());
                item.put("operatorEmployeeId", op.getEmployeeId());
                item.put("formationId", f.getId());
                item.put("workstationName", f.getWorkstation().getName());
                item.put("formationEndDate", f.getEndDate() != null ? f.getEndDate().toString() : null);
                allPending.add(item);
            }
        }
        return allPending;
    }

      // ======================== EVALUATION HISTORY ========================

    public Map<String, Object> getEvaluationHistory() {
        List<EvaluationSession> allSessions = sessionRepo.findAll().stream()
                .filter(s -> s.getStatus() != EvaluationSession.SessionStatus.IN_PROGRESS)
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .collect(Collectors.toList());

        Map<String, Long> attemptCounters = new HashMap<>();
        List<Map<String, Object>> history = new ArrayList<>();
        List<Map<String, Object>> waitingForProduction = new ArrayList<>();

        Set<Long> genericPassedOps = allSessions.stream()
                .filter(s -> "PASSED".equals(s.getDecision())
                        && s.getTemplate().getType() == EvaluationTemplate.TemplateType.GENERIC_COMMON)
                .map(s -> s.getOperator().getId())
                .collect(Collectors.toSet());

        Set<Long> productionDoneOps = allSessions.stream()
                .filter(s -> s.getTemplate().getType() == EvaluationTemplate.TemplateType.POSTE_PRODUCTION
                        && (s.getStatus() == EvaluationSession.SessionStatus.PASSED
                            || s.getStatus() == EvaluationSession.SessionStatus.FAILED
                            || s.getStatus() == EvaluationSession.SessionStatus.BLOCKED))
                .map(s -> s.getOperator().getId())
                .collect(Collectors.toSet());

        Set<Long> waitingOps = new HashSet<>(genericPassedOps);
        waitingOps.removeAll(productionDoneOps);

        for (Long waitingOpId : waitingOps) {
            operatorRepo.findById(waitingOpId).ifPresent(op -> {
                Map<String, Object> wItem = new LinkedHashMap<>();
                wItem.put("operatorId", op.getId());
                wItem.put("operatorName", op.getLastName() + " " + op.getFirstName());
                wItem.put("employeeId", op.getEmployeeId());
                allSessions.stream()
                        .filter(s -> s.getOperator().getId().equals(waitingOpId)
                                && "PASSED".equals(s.getDecision())
                                && s.getTemplate().getType() == EvaluationTemplate.TemplateType.GENERIC_COMMON)
                        .findFirst()
                        .ifPresent(s -> wItem.put("genericPassedDate", s.getCompletedAt() != null ? s.getCompletedAt().toString() : null));
                waitingForProduction.add(wItem);
            });
        }

        for (EvaluationSession session : allSessions) {
            Long opId = session.getOperator().getId();
            boolean isGeneric = session.getTemplate().getType() == EvaluationTemplate.TemplateType.GENERIC_COMMON;
            String wsKey = isGeneric ? "GENERIC_" + opId : "PROD_" + opId + "_" + (session.getTemplate().getWorkstation() != null ? session.getTemplate().getWorkstation().getId() : 0);

            long attempt = attemptCounters.getOrDefault(wsKey, 0L) + 1;
            attemptCounters.put(wsKey, attempt);

            Map<String, Object> item = new LinkedHashMap<>();
            item.put("sessionId", session.getId());
            item.put("operatorId", opId);
            item.put("operatorName", session.getOperator().getLastName() + " " + session.getOperator().getFirstName());
            item.put("employeeId", session.getOperator().getEmployeeId());
            item.put("templateType", session.getTemplate().getType().name());
            item.put("templateName", session.getTemplate().getName());
            item.put("workstationName", session.getTemplate().getWorkstation() != null ? session.getTemplate().getWorkstation().getName() : null);
            item.put("status", session.getStatus().name());
            item.put("decision", session.getDecision());
            item.put("niveau", session.getNiveau());
            item.put("genericPercentage", session.getGenericPercentage());
            item.put("productionPercentage", session.getProductionPercentage());
            item.put("scorePercentage", session.getScorePercentage());
            item.put("evaluatorName", session.getEvaluatorName());
            item.put("createdAt", session.getCreatedAt() != null ? session.getCreatedAt().toString() : null);
            item.put("completedAt", session.getCompletedAt() != null ? session.getCompletedAt().toString() : null);
            item.put("attemptNumber", attempt);
            item.put("isSecondChance", attempt > 1);
            history.add(item);
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("history", history);
        result.put("waitingForProduction", waitingForProduction);
        result.put("totalSessions", history.size());
        result.put("totalPassed", history.stream().filter(h -> "PASSED".equals(h.get("status"))).count());
        result.put("totalFailed", history.stream().filter(h -> "FAILED".equals(h.get("status"))).count());
        result.put("totalBlocked", history.stream().filter(h -> "BLOCKED".equals(h.get("status"))).count());
        result.put("totalSecondChance", history.stream().filter(h -> Boolean.TRUE.equals(h.get("isSecondChance"))).count());
        return result;
    }

    // ======================== POLYVALENCE MATRIX ========================

    private Optional<EvaluationSession> getLatestPassedSessionForWorkstation(Long operatorId, Long workstationId) {
        return sessionRepo.findByOperatorIdOrderByCreatedAtDesc(operatorId).stream()
                .filter(s -> s.getTemplate().getWorkstation() != null
                        && s.getTemplate().getWorkstation().getId().equals(workstationId)
                        && (s.getStatus() == EvaluationSession.SessionStatus.PASSED
                            || s.getStatus() == EvaluationSession.SessionStatus.COMPLETED))
                .findFirst();
    }

    private Optional<EvaluationSession> getLatestPassedGenericSession(Long operatorId) {
        return sessionRepo.findByOperatorIdOrderByCreatedAtDesc(operatorId).stream()
                .filter(s -> (s.getStatus() == EvaluationSession.SessionStatus.PASSED
                            || s.getStatus() == EvaluationSession.SessionStatus.COMPLETED)
                        && s.getTemplate().getType() == EvaluationTemplate.TemplateType.GENERIC_COMMON)
                .filter(s -> {
                    List<EvaluationAnswer> ans = answerRepo.findBySessionId(s.getId());
                    long genTotal = ans.stream()
                            .filter(a -> a.getQuestion().getTemplate().getType() == EvaluationTemplate.TemplateType.GENERIC_COMMON)
                            .count();
                    long genCorrect = ans.stream()
                            .filter(a -> a.getQuestion().getTemplate().getType() == EvaluationTemplate.TemplateType.GENERIC_COMMON && a.getAnswer() == 1)
                            .count();
                    return genTotal > 0 && genCorrect == genTotal;
                })
                .findFirst();
    }

public Map<String, Object> getPolyvalenceMatrix(Long projectId) {
        List<Workstation> workstations;
        String projectName = null;

        if (projectId != null) {
            Optional<Project> projectOpt = projectRepo.findById(projectId);
            if (projectOpt.isEmpty()) {
                Map<String, Object> empty = new LinkedHashMap<>();
                empty.put("workstations", new ArrayList<>());
                empty.put("operators", new ArrayList<>());
                return empty;
            }
            projectName = projectOpt.get().getName();
            workstations = workstationRepo.findByProjectId(projectId);
        } else {
            workstations = workstationRepo.findAll();
        }

        // Only keep operators who have at least one formation on a workstation in this project
        Set<Long> wsIds = workstations.stream().map(Workstation::getId).collect(Collectors.toSet());
        List<Long> operatorIds = new ArrayList<>();
        if (!wsIds.isEmpty()) {
            operatorIds = formationRepo.findAll().stream()
                    .filter(wf -> wsIds.contains(wf.getWorkstation().getId()))
                    .map(wf -> wf.getOperator().getId())
                    .distinct()
                    .collect(Collectors.toList());
        }
        List<Operator> operators = operatorIds.isEmpty()
                ? new ArrayList<>()
                : operatorRepo.findAllById(operatorIds);

        List<Map<String, Object>> rows = new ArrayList<>();
        java.time.format.DateTimeFormatter dtf = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy");

        for (Operator op : operators) {
            // Departed and currently absent operators must not occupy a position in the matrix.
            if (!Boolean.TRUE.equals(op.getActive())) {
                continue;
            }
            Optional<EvaluationSession> latestGen = getLatestPassedGenericSession(op.getId());

            Map<String, Object> row = new LinkedHashMap<>();
            row.put("operatorId", op.getId());
            row.put("operatorName", op.getLastName() + " " + op.getFirstName());
            row.put("employeeId", op.getEmployeeId());
            row.put("seniorityMonths", op.getHireDate() != null
                    ? Period.between(op.getHireDate(), LocalDate.now()).toTotalMonths() : 0);

            row.put("genericPassed", latestGen.isPresent());
            if (latestGen.isPresent()) {
                EvaluationSession genericSession = latestGen.get();
                row.put("genericLevel", "U");
                row.put("genericMode", genericSession.getMode() != null ? genericSession.getMode() : "INITIAL");
                row.put("genericDate", genericSession.getCompletedAt() != null ? genericSession.getCompletedAt().format(dtf) : (genericSession.getCreatedAt() != null ? genericSession.getCreatedAt().format(dtf) : LocalDate.now().format(dtf)));
            } else {
                row.put("genericLevel", "");
                row.put("genericMode", "");
                row.put("genericDate", "");
            }

            Map<Long, Map<String, Object>> wsDataMap = new LinkedHashMap<>();
            for (Workstation ws : workstations) {
                Optional<EvaluationSession> latestWs = getLatestPassedSessionForWorkstation(op.getId(), ws.getId());
                Map<String, Object> wsVal = new LinkedHashMap<>();
                if (latestWs.isPresent()) {
                    EvaluationSession s = latestWs.get();
                    String lvl = s.getNiveau();
                    lvl = ("NON_APTE".equals(lvl) || "-".equals(lvl) || "NON_VALIDE".equals(lvl)) ? "" : lvl;
                    wsVal.put("level", lvl);
                    wsVal.put("mode", s.getMode() != null ? s.getMode() : "INITIAL");
                    wsVal.put("date", s.getCompletedAt() != null ? s.getCompletedAt().format(dtf) : (s.getCreatedAt() != null ? s.getCreatedAt().format(dtf) : LocalDate.now().format(dtf)));
                } else {
                    wsVal.put("level", "");
                    wsVal.put("mode", "");
                    wsVal.put("date", "");
                }
                recyclagePlanningRepo.findTopByOperator_IdAndWorkstation_IdAndTypeOrderByScheduledDateDesc(
                                op.getId(), ws.getId(), RecyclagePlanning.PlanningType.RECYCLAGE)
                        .ifPresent(recyclage -> {
                            wsVal.put("recyclageStatus", recyclage.getStatus().name());
                            wsVal.put("recyclageDate", recyclage.getScheduledDate().format(dtf));
                            wsVal.put("recyclageLevel", recyclage.getNiveauObtenu());
                        });
                wsDataMap.put(ws.getId(), wsVal);
            }
            row.put("workstations", wsDataMap);

            rows.add(row);
        }

        List<Map<String, Object>> wsInfo = workstations.stream()
                .map(ws -> {
                    Map<String, Object> map = new LinkedHashMap<>();
                    map.put("id", ws.getId());
                    map.put("name", ws.getName());
                    map.put("zoneName", ws.getZone() != null ? ws.getZone().getName() : "");
                    map.put("projectName", ws.getZone() != null && ws.getZone().getProject() != null ? ws.getZone().getProject().getName() : "");
                    map.put("targetLevel", ws.getTargetIluLevel() != null ? ws.getTargetIluLevel() : "L");
                    return map;
                })
                .collect(Collectors.toList());

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("projectName", projectName);
        result.put("workstations", wsInfo);
        result.put("operators", rows);
        return result;
    }
    // ======================== GETTERS ========================

    public List<Map<String, Object>> getAllTemplates() {
        return templateRepo.findAll().stream().map(t -> {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", t.getId());
            map.put("name", t.getName());
            map.put("description", t.getDescription());
            map.put("type", t.getType().name());
            map.put("status", t.getStatus().name());
            map.put("targetNiveau", t.getTargetNiveau());
            map.put("workstationId", t.getWorkstation() != null ? t.getWorkstation().getId() : null);
            map.put("workstationName", t.getWorkstation() != null ? t.getWorkstation().getName() : null);
            map.put("createdAt", t.getCreatedAt());
            long qCount = questionRepo.findByTemplateId(t.getId()).stream()
                    .filter(q -> q.getStatus() == EvaluationQuestion.QuestionStatus.VALIDATED).count();
            map.put("validatedQuestionCount", qCount);
            return map;
        }).collect(Collectors.toList());
    }

    public List<Map<String, Object>> getPendingQuestions() {
        return questionRepo.findPendingQuestions().stream().map(q -> {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", q.getId());
            map.put("questionText", q.getQuestionText());
            map.put("expectedAnswer", q.getExpectedAnswer());
            map.put("validatorRole", q.getValidatorRole().name());
            map.put("templateId", q.getTemplate().getId());
            map.put("templateName", q.getTemplate().getName());
            map.put("createdById", q.getCreatedById());
            map.put("createdAt", q.getCreatedAt());
            String creatorName = q.getCreatedById() != null
                    ? userRepo.findById(q.getCreatedById()).map(User::getName).orElse("Inconnu") : "Inconnu";
            map.put("createdByName", creatorName);
            return map;
        }).collect(Collectors.toList());
    }

    public Map<String, Object> getSessionDetail(Long sessionId) {
        EvaluationSession session = sessionRepo.findById(sessionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Session introuvable"));

        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", session.getId());
        map.put("operatorId", session.getOperator().getId());
        map.put("operatorName", session.getOperator().getLastName() + " " + session.getOperator().getFirstName());
        map.put("templateId", session.getTemplate().getId());
        map.put("templateName", session.getTemplate().getName());
        map.put("templateType", session.getTemplate().getType().name());
        map.put("formationId", session.getFormation() != null ? session.getFormation().getId() : null);
        map.put("workstationFormationId", session.getWorkstationFormationId());
        map.put("planningId", session.getPlanningId());
        map.put("evaluatorName", session.getEvaluatorName());
        map.put("status", session.getStatus().name());
        map.put("mode", session.getMode());
        map.put("nextTemplateId", session.getNextTemplateId());
        if (session.getTemplate().getWorkstation() != null) {
            map.put("workstationName", session.getTemplate().getWorkstation().getName());
        }
        map.put("genericTotal", session.getGenericTotal());
        map.put("genericCorrect", session.getGenericCorrect());
        map.put("genericPercentage", session.getGenericPercentage());
        map.put("productionTotal", session.getProductionTotal());
        map.put("productionCorrect", session.getProductionCorrect());
        map.put("productionPercentage", session.getProductionPercentage());
        map.put("totalQuestions", session.getTotalQuestions());
        map.put("correctAnswers", session.getCorrectAnswers());
        map.put("scorePercentage", session.getScorePercentage());
        map.put("decision", session.getDecision());
        map.put("niveau", session.getNiveau());
        map.put("seniorityMonths", session.getOperatorSeniorityMonths());
        map.put("createdAt", session.getCreatedAt());
        map.put("completedAt", session.getCompletedAt());

        List<Map<String, Object>> answersList = answerRepo.findBySessionId(sessionId).stream().map(a -> {
            Map<String, Object> aMap = new LinkedHashMap<>();
            aMap.put("questionId", a.getQuestion().getId());
            aMap.put("questionText", a.getQuestion().getQuestionText());
            aMap.put("answer", a.getAnswer());
            aMap.put("comment", a.getComment());
            return aMap;
        }).collect(Collectors.toList());
        map.put("answers", answersList);

        return map;
    }

    public Map<String, Object> getTemplateWithQuestions(Long templateId) {
        EvaluationTemplate template = templateRepo.findById(templateId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Template introuvable"));

        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", template.getId());
        map.put("name", template.getName());
        map.put("description", template.getDescription());
        map.put("type", template.getType().name());
        map.put("status", template.getStatus().name());
        map.put("targetNiveau", template.getTargetNiveau());
        map.put("workstationId", template.getWorkstation() != null ? template.getWorkstation().getId() : null);
        map.put("workstationName", template.getWorkstation() != null ? template.getWorkstation().getName() : null);

        List<Map<String, Object>> sectionsList = sectionRepo.findByTemplateIdOrderByDisplayOrderAsc(templateId).stream()
                .map(s -> {
                    Map<String, Object> sMap = new LinkedHashMap<>();
                    sMap.put("id", s.getId());
                    sMap.put("title", s.getTitle());
                    sMap.put("displayOrder", s.getDisplayOrder());
                    sMap.put("complementaryQuestions", s.getComplementaryQuestions());

                    List<Map<String, Object>> qs = questionRepo.findByTemplateId(templateId).stream()
                            .filter(q -> q.getSection() != null && q.getSection().getId().equals(s.getId()))
                            .sorted(Comparator.comparing(EvaluationQuestion::getQuestionNumber,
                                    Comparator.nullsLast(Integer::compareTo)))
                            .map(q -> {
                                Map<String, Object> qMap = new LinkedHashMap<>();
                                qMap.put("id", q.getId());
                                qMap.put("questionText", q.getQuestionText());
                                qMap.put("expectedAnswer", q.getExpectedAnswer());
                                qMap.put("questionNumber", q.getQuestionNumber());
                                qMap.put("validatorRole", q.getValidatorRole().name());
                                qMap.put("status", q.getStatus().name());
                                qMap.put("createdById", q.getCreatedById());
                                qMap.put("createdByEmployeeId", q.getCreatedById() != null
                                        ? userRepo.findById(q.getCreatedById()).map(User::getEmployeeId).orElse(null)
                                        : null);
                                qMap.put("createdByName", q.getCreatedById() != null
                                        ? userRepo.findById(q.getCreatedById()).map(User::getName).orElse("Inconnu")
                                        : "Inconnu");
                                qMap.put("complementaryQuestions", q.getComplementaryQuestions());
                                return qMap;
                            }).collect(Collectors.toList());
                    sMap.put("questions", qs);
                    return sMap;
                }).collect(Collectors.toList());
        map.put("sections", sectionsList);

        return map;
    }

    // ======================== PRIVATE HELPERS ========================

    private EvaluationQuestion.ValidatorRole resolveValidatorRole(Long createdById, String validatorRoleStr) {
        if (validatorRoleStr != null) {
            try {
                return EvaluationQuestion.ValidatorRole.valueOf(validatorRoleStr);
            } catch (Exception ignored) {
                // fallback to auto-detection below
            }
        }

        if (createdById != null) {
            User creator = userRepo.findById(createdById).orElse(null);
            if (creator != null) {
                for (var role : creator.getRoles()) {
                    String label = role.getLabel();
                    if ("CHEF_EQUIPE".equals(label)) return EvaluationQuestion.ValidatorRole.CHEF_EQUIPE;
                    if ("AGENT_QUALITE".equals(label)) return EvaluationQuestion.ValidatorRole.AGENT_QUALITE;
                    if ("RESP_HSE".equals(label)) return EvaluationQuestion.ValidatorRole.RESP_HSE;
                    if ("RESP_QUALITE".equals(label)) return EvaluationQuestion.ValidatorRole.RESP_QUALITE;
                }
            }
        }

        return EvaluationQuestion.ValidatorRole.CHEF_EQUIPE;
    }

    private List<EvaluationQuestion.ValidatorRole> getRequiredContributorRoles(EvaluationTemplate.TemplateType type) {
        if (type == EvaluationTemplate.TemplateType.GENERIC_COMMON) {
            return List.of(
                    EvaluationQuestion.ValidatorRole.CHEF_EQUIPE,
                    EvaluationQuestion.ValidatorRole.RESP_HSE,
                    EvaluationQuestion.ValidatorRole.AGENT_QUALITE
            );
        }
        if (type == EvaluationTemplate.TemplateType.POSTE_PRODUCTION || type == EvaluationTemplate.TemplateType.ANIMATION) {
            return List.of(
                    EvaluationQuestion.ValidatorRole.CHEF_EQUIPE,
                    EvaluationQuestion.ValidatorRole.AGENT_QUALITE
            );
        }
        return List.of(EvaluationQuestion.ValidatorRole.CHEF_EQUIPE);
    }

    private String determineNiveau(Long seniorityMonths, double productionPercentage) {
        if (seniorityMonths < 6) {
            if (productionPercentage >= 70) {
                return "I";
            }
            return "NON_VALIDE";
        } else if (seniorityMonths < 12) {
            if (productionPercentage >= 81) {
                return "L";
            }
            return "NON_VALIDE";
        } else {
            if (productionPercentage >= 91) {
                return "U";
            }
            return "NON_VALIDE";
        }
    }

    private String getNiveauForOperatorWorkstation(Long operatorId, Long workstationId) {
        Optional<EvaluationSession> latest = sessionRepo.findByOperatorIdOrderByCreatedAtDesc(operatorId).stream()
                .filter(s -> s.getTemplate().getWorkstation() != null
                        && s.getTemplate().getWorkstation().getId().equals(workstationId)
                        && s.getStatus() == EvaluationSession.SessionStatus.PASSED)
                .findFirst();
        return latest.map(s -> {
            String n = s.getNiveau();
            return ("NON_APTE".equals(n) || "-".equals(n) || "NON_VALIDE".equals(n)) ? "" : n;
        }).orElse("");
    }

    private boolean hasPassedLevel(Long operatorId, Long workstationId, String niveau) {
        return sessionRepo.findPassedByOperatorAndWorkstationAndNiveau(operatorId, workstationId, niveau)
                .stream()
                .anyMatch(session -> session.getTemplate().getType() == EvaluationTemplate.TemplateType.POSTE_PRODUCTION);
    }

    private boolean hasPassedGeneric(Long operatorId) {
        return sessionRepo.findByOperatorIdOrderByCreatedAtDesc(operatorId).stream()
                .filter(s -> s.getStatus() == EvaluationSession.SessionStatus.PASSED)
                .anyMatch(s -> {
                    List<EvaluationAnswer> ans = answerRepo.findBySessionId(s.getId());
                    long genTotal = ans.stream()
                            .filter(a -> a.getQuestion().getTemplate().getType() == EvaluationTemplate.TemplateType.GENERIC_COMMON)
                            .count();
                    long genCorrect = ans.stream()
                            .filter(a -> a.getQuestion().getTemplate().getType() == EvaluationTemplate.TemplateType.GENERIC_COMMON && a.getAnswer() == 1)
                            .count();
                    return genTotal > 0 && genCorrect == genTotal;
                });
    }

    private boolean hasPassedInitialEvaluationForWorkstation(Long operatorId, WorkstationFormation formation) {
        if (formation == null || formation.getWorkstation() == null) {
            return false;
        }

        Long workstationId = formation.getWorkstation().getId();
        return sessionRepo.findByOperatorIdOrderByCreatedAtDesc(operatorId).stream()
                .filter(s -> s.getStatus() == EvaluationSession.SessionStatus.PASSED)
                .filter(s -> s.getTemplate() != null)
                .filter(s -> s.getTemplate().getWorkstation() != null)
                .anyMatch(s -> {
                    boolean sameWorkstation = s.getTemplate().getWorkstation().getId().equals(workstationId);
                    if (!sameWorkstation) {
                        return false;
                    }
                    boolean sameFormation = formation.getId() != null
                            && s.getFormation() != null
                            && formation.getId().equals(s.getFormation().getId());
                    return sameFormation || s.getTemplate().getType() == EvaluationTemplate.TemplateType.POSTE_PRODUCTION;
                });
    }

    private Map<String, Object> buildResult(EvaluationSession session, String message) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("sessionId", session.getId());
        result.put("status", session.getStatus().name());
        result.put("decision", session.getDecision());
        result.put("niveau", session.getNiveau());
        result.put("genericTotal", session.getGenericTotal());
        result.put("genericCorrect", session.getGenericCorrect());
        result.put("genericPercentage", session.getGenericPercentage());
        result.put("productionTotal", session.getProductionTotal());
        result.put("productionCorrect", session.getProductionCorrect());
        result.put("productionPercentage", session.getProductionPercentage());
        result.put("totalQuestions", session.getTotalQuestions());
        result.put("correctAnswers", session.getCorrectAnswers());
        result.put("scorePercentage", session.getScorePercentage());
        result.put("seniorityMonths", session.getOperatorSeniorityMonths());
        result.put("nextTemplateId", session.getNextTemplateId());
        result.put("message", message);
        return result;
    }

    public List<Map<String, Object>> getDoubleFailures() {
        List<Operator> operators = operatorRepo.findAll().stream()
                .filter(u -> u.getActive() != null && u.getActive())
                .collect(Collectors.toList());
        List<Workstation> workstations = workstationRepo.findAll();
        List<Map<String, Object>> list = new ArrayList<>();
        java.time.format.DateTimeFormatter dtf = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy");

        for (Operator op : operators) {
            for (Workstation ws : workstations) {
                List<WorkstationFormation> formations = formationRepo.findByOperator_Id(op.getId()).stream()
                        .filter(f -> f.getWorkstation().getId().equals(ws.getId()) && "FAILED".equals(f.getStatus()))
                        .collect(Collectors.toList());

                List<EvaluationSession> sessions = sessionRepo.findByOperatorIdOrderByCreatedAtDesc(op.getId()).stream()
                        .filter(s -> s.getTemplate() != null
                                && s.getTemplate().getWorkstation() != null
                                && s.getTemplate().getWorkstation().getId().equals(ws.getId())
                                && (s.getStatus() == EvaluationSession.SessionStatus.FAILED || s.getStatus() == EvaluationSession.SessionStatus.BLOCKED))
                        .collect(Collectors.toList());

                long failedCount = formations.size() + sessions.size();

                if (failedCount >= 2) {
                    Map<String, Object> map = new LinkedHashMap<>();
                    map.put("operatorId", op.getId());
                    map.put("operatorName", op.getLastName() + " " + op.getFirstName());
                    map.put("employeeId", op.getEmployeeId());
                    map.put("workstationName", ws.getName());
                    map.put("failedCount", failedCount);

                    List<Map<String, Object>> failures = new ArrayList<>();
                    for (WorkstationFormation f : formations) {
                        Map<String, Object> fMap = new LinkedHashMap<>();
                        fMap.put("type", "Suivi de Formation (12j)");
                        fMap.put("date", f.getEndDate() != null ? f.getEndDate().format(dtf) : (f.getStartDate() != null ? f.getStartDate().format(dtf) : ""));
                        fMap.put("details", "Moyenne cadence ou defauts hors objectifs");
                        failures.add(fMap);
                    }
                    for (EvaluationSession s : sessions) {
                        Map<String, Object> sMap = new LinkedHashMap<>();
                        sMap.put("type", "Evaluation " + (s.getMode() != null ? s.getMode() : "INITIAL"));
                        String dStr = s.getCompletedAt() != null ? s.getCompletedAt().format(dtf) : (s.getCreatedAt() != null ? s.getCreatedAt().format(dtf) : "");
                        sMap.put("date", dStr);
                        sMap.put("details", "Score insuffisant: " + (s.getScorePercentage() != null ? s.getScorePercentage() : 0.0) + "%");
                        failures.add(sMap);
                    }
                    map.put("failures", failures);
                    list.add(map);
                }
            }
        }
        return list;
    }

}
