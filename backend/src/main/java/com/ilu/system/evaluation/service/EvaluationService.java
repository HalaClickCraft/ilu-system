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
import com.ilu.system.structure.entity.Workstation;
import com.ilu.system.structure.repository.WorkstationRepository;
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

    public EvaluationService(EvaluationTemplateRepository templateRepo,
                             EvaluationSectionRepository sectionRepo,
                             EvaluationQuestionRepository questionRepo,
                             EvaluationSessionRepository sessionRepo,
                             EvaluationAnswerRepository answerRepo,
                             OperatorRepository operatorRepo,
                             WorkstationRepository workstationRepo,
                             FormationAssignmentRepository assignmentRepo,
                             WorkstationFormationRepository formationRepo,
                             UserRepository userRepo) {
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

        if (workstationId != null && type == EvaluationTemplate.TemplateType.POSTE_PRODUCTION) {
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
        section.setDisplayOrder(displayOrder != null ? displayOrder : (template.getSections() != null ? template.getSections().size() : 0) + 1);
        section.setComplementaryQuestions(complementaryQuestions);
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
        result.put("complementaryQuestions", section.getComplementaryQuestions());
        result.put("templateId", templateId);
        return result;
    }

    @Transactional
    public Map<String, Object> addQuestion(Long templateId, Long sectionId, String questionText,
                                        String expectedAnswer, String complementaryQuestions, Integer questionNumber,
                                        Set<String> userRoles, Long createdById) {
        EvaluationTemplate template = templateRepo.findById(templateId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Template introuvable"));

        EvaluationQuestion.ValidatorRole validatorRole = deriveValidatorRole(userRoles);

        EvaluationQuestion question = new EvaluationQuestion();
        question.setTemplate(template);
        question.setQuestionText(questionText);
        question.setExpectedAnswer(expectedAnswer);
        question.setComplementaryQuestions(complementaryQuestions);
        question.setQuestionNumber(questionNumber);
        question.setValidatorRole(validatorRole);
        question.setCreatedById(createdById);

        // Only AGENT_QUALITE questions need PENDING approval by RESP_QUALITE
        // CHEF_EQUIPE, RESP_HSE, RESP_QUALITE questions are auto-validated
        if (validatorRole == EvaluationQuestion.ValidatorRole.AGENT_QUALITE) {
            question.setStatus(EvaluationQuestion.QuestionStatus.PENDING);
        } else {
            question.setStatus(EvaluationQuestion.QuestionStatus.VALIDATED);
            question.setValidatedById(createdById);
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
        result.put("complementaryQuestions", question.getComplementaryQuestions());
        result.put("questionNumber", question.getQuestionNumber());
        result.put("validatorRole", question.getValidatorRole().name());
        result.put("status", question.getStatus().name());
        result.put("createdById", question.getCreatedById());
        if (question.getStatus() == EvaluationQuestion.QuestionStatus.PENDING) {
            result.put("message", "Question creee en attente de validation par le responsable");
        } else {
            result.put("message", "Question validee automatiquement");
        }
        return result;
    }

    /**
     * Maps the authenticated user's real roles to a ValidatorRole.
     * Prevents impersonation - the role comes from the JWT, not the request body.
     */
    private EvaluationQuestion.ValidatorRole deriveValidatorRole(Set<String> userRoles) {
        if (userRoles.contains("ADMIN")) return EvaluationQuestion.ValidatorRole.CHEF_EQUIPE;
        if (userRoles.contains("CHEF_EQUIPE")) return EvaluationQuestion.ValidatorRole.CHEF_EQUIPE;
        if (userRoles.contains("RESP_HSE")) return EvaluationQuestion.ValidatorRole.RESP_HSE;
        if (userRoles.contains("RESP_QUALITE")) return EvaluationQuestion.ValidatorRole.RESP_QUALITE;
        if (userRoles.contains("AGENT_QUALITE")) return EvaluationQuestion.ValidatorRole.AGENT_QUALITE;
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Role non autorise a creer des questions");
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

        long validatedCount = questionRepo.findByTemplateId(templateId).stream()
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
                                                Long formationId, Long evaluatorId) {
        Operator operator = operatorRepo.findById(operatorId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Operateur introuvable"));

        EvaluationTemplate template = templateRepo.findById(templateId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Template introuvable"));

        if (template.getStatus() != EvaluationTemplate.TemplateStatus.VALIDATED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Le template doit etre valide");
        }

        FormationAssignment formation = null;
        if (formationId != null) {
            formation = assignmentRepo.findById(formationId).orElse(null);
        }

        long seniorityMonths = 0;
        if (operator.getHireDate() != null) {
            seniorityMonths = Period.between(operator.getHireDate(), LocalDate.now()).toTotalMonths();
        }

        EvaluationSession session = new EvaluationSession();
        session.setOperator(operator);
        session.setTemplate(template);
        session.setFormation(formation);
        session.setEvaluatorId(evaluatorId);
        session.setOperatorSeniorityMonths(seniorityMonths);
        session.setStatus(EvaluationSession.SessionStatus.IN_PROGRESS);
        sessionRepo.save(session);

        List<EvaluationQuestion> questions = questionRepo.findValidatedQuestionsByTemplate(templateId);

        String evaluatorName = userRepo.findById(evaluatorId)
                .map(User::getName).orElse("Inconnu");
        session.setEvaluatorName(evaluatorName);
        sessionRepo.save(session);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("sessionId", session.getId());
        result.put("operatorName", operator.getLastName() + " " + operator.getFirstName());
        result.put("templateName", template.getName());
        result.put("totalQuestions", questions.size());
        result.put("seniorityMonths", seniorityMonths);
        result.put("status", "IN_PROGRESS");
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

        if (genericPct < 100.0) {
            session.setStatus(EvaluationSession.SessionStatus.BLOCKED);
            session.setDecision("BLOCKED_GENERIC");
            session.setCompletedAt(LocalDateTime.now());
            sessionRepo.save(session);
            return buildResult(session, "BLOQUE: La partie generique (HSE + Qualite) doit etre 100%");
        }

        String niveau = determineNiveau(session.getOperatorSeniorityMonths(), productionPct);
        session.setNiveau(niveau);

        if ("NON_VALIDE".equals(niveau)) {
            session.setStatus(EvaluationSession.SessionStatus.FAILED);
            session.setDecision("FAILED");
            session.setCompletedAt(LocalDateTime.now());
            sessionRepo.save(session);
            return buildResult(session, "Echec: Score insuffisant pour le niveau correspondant a l'anciennete");
        }

        session.setStatus(EvaluationSession.SessionStatus.PASSED);
        session.setDecision("PASSED_" + niveau);
        session.setCompletedAt(LocalDateTime.now());
        sessionRepo.save(session);

        return buildResult(session, "Reussite: Niveau " + niveau + " atteint");
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
            if (sessionRepo.existsByOperatorIdAndFormationId(operatorId, f.getId())) {
                continue;
            }

            Map<String, Object> item = new LinkedHashMap<>();
            item.put("formationId", f.getId());
            item.put("workstationId", f.getWorkstation().getId());
            item.put("workstationName", f.getWorkstation().getName());
            item.put("formationStartDate", f.getStartDate().toString());
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
                if (sessionRepo.existsByOperatorIdAndFormationId(op.getId(), f.getId())) {
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

    // ======================== POLYVALENCE MATRIX ========================

    public Map<String, Object> getPolyvalenceMatrix() {
        List<Operator> operators = operatorRepo.findAll();
        List<Workstation> workstations = workstationRepo.findAll();

        List<Map<String, Object>> rows = new ArrayList<>();
        for (Operator op : operators) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("operatorId", op.getId());
            row.put("operatorName", op.getLastName() + " " + op.getFirstName());
            row.put("employeeId", op.getEmployeeId());
            row.put("seniorityMonths", op.getHireDate() != null
                    ? Period.between(op.getHireDate(), LocalDate.now()).toTotalMonths() : 0);

            Map<String, String> niveauMap = new LinkedHashMap<>();
            for (Workstation ws : workstations) {
                String niveau = getNiveauForOperatorWorkstation(op.getId(), ws.getId());
                niveauMap.put(ws.getName(), niveau);
            }
            row.put("workstations", niveauMap);

            row.put("genericPassed", hasPassedGeneric(op.getId()));

            rows.add(row);
        }

        List<Map<String, String>> niveauRules = new ArrayList<>();
        Map<String, String> rule1 = new LinkedHashMap<>();
        rule1.put("niveau", "I");
        rule1.put("description", "< 6 mois anciennete, score 70-80%");
        niveauRules.add(rule1);
        Map<String, String> rule2 = new LinkedHashMap<>();
        rule2.put("niveau", "L");
        rule2.put("description", ">= 6 mois anciennete, score 81-90%");
        niveauRules.add(rule2);
        Map<String, String> rule3 = new LinkedHashMap<>();
        rule3.put("niveau", "U");
        rule3.put("description", ">= 12 mois anciennete, score 91-100%");
        niveauRules.add(rule3);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("workstations", workstations.stream()
                .map(ws -> Map.of("id", ws.getId(), "name", ws.getName()))
                .collect(Collectors.toList()));
        result.put("operators", rows);
        result.put("niveauRules", niveauRules);
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
            map.put("complementaryQuestions", q.getComplementaryQuestions());
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
        map.put("formationId", session.getFormation() != null ? session.getFormation().getId() : null);
        map.put("evaluatorName", session.getEvaluatorName());
        map.put("status", session.getStatus().name());
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

                    // For DRAFT templates, show ALL questions so users can manage them.
                    // For VALIDATED/ARCHIVED, show only VALIDATED questions.
                    List<EvaluationQuestion> questions;
                    if (template.getStatus() == EvaluationTemplate.TemplateStatus.DRAFT) {
                        questions = questionRepo.findByTemplateId(templateId).stream()
                                .filter(q -> q.getSection() != null && q.getSection().getId().equals(s.getId()))
                                .sorted(Comparator.comparing(q -> q.getQuestionNumber() != null ? q.getQuestionNumber() : 0))
                                .collect(Collectors.toList());
                    } else {
                        questions = questionRepo.findValidatedQuestionsByTemplate(templateId).stream()
                                .filter(q -> q.getSection() != null && q.getSection().getId().equals(s.getId()))
                                .collect(Collectors.toList());
                    }

                    List<Map<String, Object>> qs = questions.stream()
                            .map(q -> {
                                Map<String, Object> qMap = new LinkedHashMap<>();
                                qMap.put("id", q.getId());
                                qMap.put("questionText", q.getQuestionText());
                                qMap.put("expectedAnswer", q.getExpectedAnswer());
                                qMap.put("complementaryQuestions", q.getComplementaryQuestions());
                                qMap.put("questionNumber", q.getQuestionNumber());
                                qMap.put("validatorRole", q.getValidatorRole().name());
                                qMap.put("status", q.getStatus().name());
                                qMap.put("createdById", q.getCreatedById());
                                String creatorName = q.getCreatedById() != null
                                        ? userRepo.findById(q.getCreatedById()).map(User::getName).orElse("Inconnu") : "Inconnu";
                                qMap.put("createdByName", creatorName);
                                String creatorEmployeeId = q.getCreatedById() != null
                                        ? userRepo.findById(q.getCreatedById()).map(User::getEmployeeId).orElse(null) : null;
                                qMap.put("createdByEmployeeId", creatorEmployeeId);
                                return qMap;
                            }).collect(Collectors.toList());
                    sMap.put("questions", qs);
                    return sMap;
                }).collect(Collectors.toList());
        map.put("sections", sectionsList);

        return map;
    }

    // ======================== QUESTION / TEMPLATE UPDATE & DELETE ========================

    @Transactional
    public Map<String, Object> updateQuestion(Long questionId, String questionText,
                                           String expectedAnswer, String complementaryQuestions,
                                           Integer questionNumber, Long sectionId, Long currentUserId) {
        EvaluationQuestion question = questionRepo.findById(questionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Question introuvable"));
        if (question.getTemplate().getStatus() != EvaluationTemplate.TemplateStatus.DRAFT) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Seul un template en brouillon peut etre modifie");
        }
        // Ownership check: only the creator can edit their own question
        if (!question.getCreatedById().equals(currentUserId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Vous ne pouvez modifier que vos propres questions");
        }
        if (questionText != null) question.setQuestionText(questionText);
        if (expectedAnswer != null) question.setExpectedAnswer(expectedAnswer);
        if (complementaryQuestions != null) question.setComplementaryQuestions(complementaryQuestions);
        if (questionNumber != null) question.setQuestionNumber(questionNumber);
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
        result.put("complementaryQuestions", question.getComplementaryQuestions());
        result.put("questionNumber", question.getQuestionNumber());
        result.put("status", question.getStatus().name());
        return result;
    }

    @Transactional
    public Map<String, Object> deleteQuestion(Long questionId, Long currentUserId) {
        EvaluationQuestion question = questionRepo.findById(questionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Question introuvable"));
        if (question.getTemplate().getStatus() != EvaluationTemplate.TemplateStatus.DRAFT) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Seul un template en brouillon peut etre modifie");
        }
        // Ownership check: only the creator can delete their own question
        if (!question.getCreatedById().equals(currentUserId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Vous ne pouvez supprimer que vos propres questions");
        }
        questionRepo.delete(question);
        return Map.of("deleted", true);
    }

    @Transactional
    public Map<String, Object> updateTemplate(Long templateId, String name, String description, String targetNiveau) {
        EvaluationTemplate template = templateRepo.findById(templateId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Template introuvable"));
        if (template.getStatus() != EvaluationTemplate.TemplateStatus.DRAFT) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Seul un brouillon peut etre modifie");
        }
        if (name != null) template.setName(name);
        if (description != null) template.setDescription(description);
        if (targetNiveau != null) template.setTargetNiveau(targetNiveau);
        templateRepo.save(template);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", template.getId());
        result.put("name", template.getName());
        result.put("description", template.getDescription());
        result.put("type", template.getType().name());
        result.put("status", template.getStatus().name());
        return result;
    }

    @Transactional
    public Map<String, Object> deleteTemplate(Long templateId) {
        EvaluationTemplate template = templateRepo.findById(templateId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Template introuvable"));
        if (template.getStatus() != EvaluationTemplate.TemplateStatus.DRAFT) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Seul un brouillon peut etre supprime");
        }
        templateRepo.delete(template);
        return Map.of("deleted", true);
    }

    // ======================== PRIVATE HELPERS ========================

    private String determineNiveau(Long seniorityMonths, double productionPercentage) {
        if (seniorityMonths < 6) {
            if (productionPercentage >= 70 && productionPercentage <= 100) {
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
                        && (s.getStatus() == EvaluationSession.SessionStatus.PASSED
                            || s.getStatus() == EvaluationSession.SessionStatus.BLOCKED))
                .findFirst();
        return latest.map(EvaluationSession::getNiveau).orElse("-");
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
        result.put("message", message);
        return result;
    }
}