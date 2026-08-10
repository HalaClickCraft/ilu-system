package com.ilu.system.evaluation.service;

import com.ilu.system.auth.entity.User;
import com.ilu.system.auth.repository.UserRepository;
import com.ilu.system.evaluation.entity.EvaluationAnswer;
import com.ilu.system.evaluation.entity.EvaluationQuestion;
import com.ilu.system.evaluation.entity.EvaluationSection;
import com.ilu.system.evaluation.entity.EvaluationSession;
import com.ilu.system.evaluation.entity.EvaluationTemplate;
import com.ilu.system.evaluation.repository.EvaluationAnswerRepository;
import com.ilu.system.evaluation.repository.EvaluationQuestionRepository;
import com.ilu.system.evaluation.repository.EvaluationSectionRepository;
import com.ilu.system.evaluation.repository.EvaluationSessionRepository;
import com.ilu.system.evaluation.repository.EvaluationTemplateRepository;
import com.ilu.system.operator.entity.Operator;
import com.ilu.system.operator.entity.WorkstationFormation;
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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
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
    private final WorkstationFormationRepository formationRepo;
    private final UserRepository userRepo;

    public EvaluationService(EvaluationTemplateRepository templateRepo,
                             EvaluationSectionRepository sectionRepo,
                             EvaluationQuestionRepository questionRepo,
                             EvaluationSessionRepository sessionRepo,
                             EvaluationAnswerRepository answerRepo,
                             OperatorRepository operatorRepo,
                             WorkstationRepository workstationRepo,
                             WorkstationFormationRepository formationRepo,
                             UserRepository userRepo) {
        this.templateRepo = templateRepo;
        this.sectionRepo = sectionRepo;
        this.questionRepo = questionRepo;
        this.sessionRepo = sessionRepo;
        this.answerRepo = answerRepo;
        this.operatorRepo = operatorRepo;
        this.workstationRepo = workstationRepo;
        this.formationRepo = formationRepo;
        this.userRepo = userRepo;
    }

    @Transactional
    public Map<String, Object> createTemplate(String name, String description, String typeName,
                                               Collection<Long> workstationIds, String targetNiveau,
                                               Long createdById, Set<String> roles) {
        EvaluationTemplate.TemplateType type = enumValue(EvaluationTemplate.TemplateType.class, typeName, "Type invalide");
        requireTemplateOwner(type, roles);
        EvaluationTemplate template = new EvaluationTemplate();
        template.setName(required(name, "Le nom est obligatoire"));
        template.setDescription(description);
        template.setType(type);
        template.setTargetNiveau(targetNiveau);
        template.setCreatedById(createdById);
        if (workstationIds != null) {
            for (Long workstationId : workstationIds) {
                Workstation workstation = workstationRepo.findById(workstationId)
                        .orElseThrow(() -> notFound("Poste introuvable"));
                template.getWorkstations().add(workstation);
            }
        }
        if (type == EvaluationTemplate.TemplateType.POSTE_PRODUCTION && template.getWorkstations().isEmpty()) {
            throw badRequest("Un template Production doit etre assigne a au moins un poste");
        }
        templateRepo.save(template);
        return templateSummary(template, "Template cree avec succes");
    }

    @Transactional
    public Map<String, Object> addSection(Long templateId, String title, String description, Integer displayOrder,
                                          String domainName, Set<String> roles) {
        EvaluationTemplate template = findTemplate(templateId);
        requireTemplateOwner(template.getType(), roles);
        EvaluationSection.SectionDomain domain = sectionDomainForTemplate(template.getType(), domainName);
        requireRole(domain.getResponsibleRole(), roles);
        EvaluationSection section = new EvaluationSection();
        section.setTemplate(template);
        section.setTitle(required(title, "Le titre est obligatoire"));
        section.setDescription(description);
        section.setDisplayOrder(displayOrder != null ? displayOrder : sectionRepo.findByTemplateIdOrderByDisplayOrderAsc(templateId).size() + 1);
        section.setDomain(domain);
        section.setResponsibleRole(domain.getResponsibleRole());
        sectionRepo.save(section);
        return sectionMap(section);
    }

    @Transactional
    public Map<String, Object> addQuestion(Long templateId, Long sectionId, String questionText, String expectedAnswer,
                                            Integer questionNumber, String responseTypeName, Long createdById, Set<String> roles) {
        EvaluationTemplate template = findTemplate(templateId);
        if (sectionId == null) {
            throw badRequest("Une section est obligatoire pour ajouter une question");
        }
        EvaluationSection section = sectionRepo.findById(sectionId).orElseThrow(() -> notFound("Section introuvable"));
        if (section.getTemplate() == null || !section.getTemplate().getId().equals(templateId)) {
            throw badRequest("La section n'appartient pas au template");
        }
        EvaluationSection.SectionDomain domain = effectiveSectionDomain(section);
        requireTemplateDomain(template, domain);
        requireRole(domain.getResponsibleRole(), roles);
        EvaluationQuestion question = new EvaluationQuestion();
        question.setTemplate(template);
        question.setSection(section);
        question.setQuestionText(required(questionText, "La question est obligatoire"));
        question.setExpectedAnswer(expectedAnswer);
        question.setQuestionNumber(questionNumber != null ? questionNumber : nextQuestionNumber(templateId));
        // Kept persisted for existing questionnaire and gating queries; its value is section-owned.
        question.setCategory(domain.getQuestionCategory());
        question.setValidatorRole(domain.getResponsibleRole());
        EvaluationQuestion.ResponseType responseType = responseTypeName == null ? EvaluationQuestion.ResponseType.BINARY
                : enumValue(EvaluationQuestion.ResponseType.class, responseTypeName, "Type de reponse invalide");
        if (responseType != EvaluationQuestion.ResponseType.BINARY) {
            throw badRequest("Les questions d'evaluation doivent utiliser le controle binaire 0/1");
        }
        question.setResponseType(responseType);
        question.setCreatedById(createdById);
        // Quality questions authored by AGENT_QUALITE require the explicit RESP_QUALITE approval.
        question.setStatus(domain == EvaluationSection.SectionDomain.QUALITY
                ? EvaluationQuestion.QuestionStatus.PENDING : EvaluationQuestion.QuestionStatus.VALIDATED);
        questionRepo.save(question);
        Map<String, Object> result = questionMap(question);
        result.put("message", question.getStatus() == EvaluationQuestion.QuestionStatus.PENDING
                ? "Question creee en attente de validation Qualite" : "Question publiee");
        return result;
    }

    @Transactional
    public Map<String, Object> validateQuestion(Long questionId, Long validatedById, Set<String> roles) {
        EvaluationQuestion question = findQuestion(questionId);
        requireQualityApproval(question, roles);
        if (question.getStatus() != EvaluationQuestion.QuestionStatus.PENDING) throw badRequest("Cette question n'est plus en attente");
        question.setStatus(EvaluationQuestion.QuestionStatus.VALIDATED);
        question.setValidatedById(validatedById);
        return questionMap(question);
    }

    @Transactional
    public Map<String, Object> rejectQuestion(Long questionId, Long validatedById, String reason, Set<String> roles) {
        EvaluationQuestion question = findQuestion(questionId);
        requireQualityApproval(question, roles);
        if (question.getStatus() != EvaluationQuestion.QuestionStatus.PENDING) throw badRequest("Cette question n'est plus en attente");
        question.setStatus(EvaluationQuestion.QuestionStatus.REJECTED);
        question.setValidatedById(validatedById);
        question.setRejectionReason(reason);
        return questionMap(question);
    }

    @Transactional
    public Map<String, Object> validateTemplate(Long templateId, Set<String> roles) {
        EvaluationTemplate template = findTemplate(templateId);
        requireTemplateOwner(template.getType(), roles);
        if (questionRepo.findByTemplateId(templateId).stream().noneMatch(this::isPublished)) {
            throw badRequest("Le template doit avoir au moins une question publiee");
        }
        template.setStatus(EvaluationTemplate.TemplateStatus.VALIDATED);
        return templateSummary(template, "Template valide avec succes");
    }

    @Transactional
    public Map<String, Object> startEvaluation(Long operatorId, Long practicalFormationId, Long evaluatorId, Set<String> roles) {
        requireEvaluator(roles);
        Operator operator = operatorRepo.findById(operatorId).orElseThrow(() -> notFound("Operateur introuvable"));
        WorkstationFormation formation = resolveFormation(operatorId, practicalFormationId);
        if (sessionRepo.existsByOperatorIdAndPracticalFormationId(operatorId, formation.getId())) {
            throw badRequest("Une evaluation existe deja pour cette formation");
        }
        EvaluationTemplate productionTemplate = selectProductionTemplate(formation.getWorkstation().getId());
        List<EvaluationQuestion> questions = questionnaireQuestions(productionTemplate);
        if (questions.stream().noneMatch(q -> isGeneric(questionCategory(q))) || questions.stream().noneMatch(q -> questionCategory(q) == EvaluationQuestion.QuestionCategory.PRODUCTION)) {
            throw badRequest("Le questionnaire doit contenir des questions communes et Production publiees");
        }
        EvaluationSession session = new EvaluationSession();
        session.setOperator(operator);
        session.setPracticalFormation(formation);
        session.setTemplate(productionTemplate);
        session.setEvaluatorId(evaluatorId);
        session.setEvaluatorName(userRepo.findById(evaluatorId).map(User::getName).orElse("Inconnu"));
        session.setOperatorSeniorityMonths(operator.getHireDate() == null ? 0
                : Period.between(operator.getHireDate(), LocalDate.now()).toTotalMonths());
        session.setStatus(EvaluationSession.SessionStatus.IN_PROGRESS);
        sessionRepo.save(session);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("sessionId", session.getId());
        result.put("operatorName", operator.getLastName() + " " + operator.getFirstName());
        result.put("workstationName", formation.getWorkstation().getName());
        result.put("templateName", productionTemplate.getName());
        result.put("totalQuestions", questions.size());
        result.put("status", session.getStatus().name());
        return result;
    }

    @Transactional
    public Map<String, Object> submitAnswers(Long sessionId, List<Map<String, Object>> answersData, Long answeredById, Set<String> roles) {
        requireEvaluator(roles);
        EvaluationSession session = findSession(sessionId);
        if (session.getStatus() != EvaluationSession.SessionStatus.IN_PROGRESS) throw badRequest("La session n'est plus en cours");
        List<EvaluationQuestion> questionnaire = questionnaireQuestions(session.getTemplate());
        Map<Long, EvaluationQuestion> allowedQuestions = questionnaire.stream()
                .collect(Collectors.toMap(EvaluationQuestion::getId, q -> q));
        boolean productionEnabled = genericPassed(sessionId, questionnaire);
        int saved = 0;
        for (Map<String, Object> data : answersData == null ? List.<Map<String, Object>>of() : answersData) {
            Long questionId = Long.valueOf(data.get("questionId").toString());
            EvaluationQuestion question = allowedQuestions.get(questionId);
            if (question == null) throw badRequest("Question absente de ce questionnaire");
            EvaluationQuestion.QuestionCategory category = questionCategory(question);
            if (!canAnswer(category, roles)) throw forbidden("Vous ne pouvez pas modifier cette question");
            if (category == EvaluationQuestion.QuestionCategory.PRODUCTION && !productionEnabled) {
                throw badRequest("Les questions Production sont verrouillees tant que les questions communes ne sont pas toutes reussies");
            }
            Integer answer = integerAnswer(data.get("answer"));
            Optional<EvaluationAnswer> existing = answerRepo.findBySessionIdAndQuestionId(sessionId, questionId);
            EvaluationAnswer evaluationAnswer = existing.orElseGet(EvaluationAnswer::new);
            evaluationAnswer.setSession(session);
            evaluationAnswer.setQuestion(question);
            evaluationAnswer.setAnswer(answer);
            evaluationAnswer.setComment(data.get("comment") == null ? null : data.get("comment").toString());
            evaluationAnswer.setAnsweredById(answeredById);
            answerRepo.save(evaluationAnswer);
            saved++;
            productionEnabled = genericPassed(sessionId, questionnaire);
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("sessionId", sessionId);
        result.put("savedAnswers", saved);
        result.put("productionEnabled", genericPassed(sessionId, questionnaire));
        return result;
    }

    @Transactional
    public Map<String, Object> completeEvaluation(Long sessionId, Set<String> roles) {
        requireEvaluator(roles);
        EvaluationSession session = findSession(sessionId);
        if (session.getStatus() != EvaluationSession.SessionStatus.IN_PROGRESS) throw badRequest("La session n'est plus en cours");
        List<EvaluationQuestion> questions = questionnaireQuestions(session.getTemplate());
        Map<Long, EvaluationAnswer> answers = answerRepo.findBySessionId(sessionId).stream()
                .collect(Collectors.toMap(a -> a.getQuestion().getId(), a -> a));
        if (!questions.stream().allMatch(question -> answers.containsKey(question.getId()))) {
            throw badRequest("Toutes les questions actuellement publiees doivent etre renseignees");
        }
        if (!genericPassed(sessionId, questions)) {
            throw badRequest("La partie commune doit etre completee et reussie a 100% avant la Production");
        }
        int genericTotal = 0, genericCorrect = 0, productionTotal = 0, productionCorrect = 0;
        for (EvaluationQuestion question : questions) {
            int score = answers.get(question.getId()).getAnswer();
            if (isGeneric(questionCategory(question))) {
                genericTotal++;
                genericCorrect += score;
            } else if (questionCategory(question) == EvaluationQuestion.QuestionCategory.PRODUCTION) {
                productionTotal++;
                productionCorrect += score;
            }
        }
        double genericPct = percentage(genericCorrect, genericTotal);
        double productionPct = percentage(productionCorrect, productionTotal);
        session.setGenericTotal(genericTotal);
        session.setGenericCorrect(genericCorrect);
        session.setGenericPercentage(genericPct);
        session.setProductionTotal(productionTotal);
        session.setProductionCorrect(productionCorrect);
        session.setProductionPercentage(productionPct);
        session.setTotalQuestions(genericTotal + productionTotal);
        session.setCorrectAnswers(genericCorrect + productionCorrect);
        session.setScorePercentage(percentage(genericCorrect + productionCorrect, genericTotal + productionTotal));
        String niveau = determineNiveau(session.getOperatorSeniorityMonths(), productionPct);
        session.setNiveau(niveau);
        session.setStatus("NON_VALIDE".equals(niveau) ? EvaluationSession.SessionStatus.FAILED : EvaluationSession.SessionStatus.PASSED);
        session.setDecision("NON_VALIDE".equals(niveau) ? "FAILED" : "PASSED_" + niveau);
        session.setCompletedAt(LocalDateTime.now());
        return buildResult(session);
    }

    public List<Map<String, Object>> getAllTemplates(Set<String> roles) {
        requireEvaluator(roles);
        return templateRepo.findAll().stream().map(t -> templateSummary(t, null)).collect(Collectors.toList());
    }

    public Map<String, Object> getTemplateWithQuestions(Long templateId, Set<String> roles) {
        requireEvaluator(roles);
        EvaluationTemplate template = findTemplate(templateId);
        Map<String, Object> map = templateSummary(template, null);
        map.put("sections", sectionsForTemplates(List.of(template), false));
        return map;
    }

    public List<Map<String, Object>> getPendingQuestions(Set<String> roles) {
        if (!has(roles, "RESP_QUALITE")) throw forbidden("Seul le responsable qualite peut consulter ces questions");
        return questionRepo.findPendingQuestions().stream()
                .filter(q -> questionCategory(q) == EvaluationQuestion.QuestionCategory.QUALITY)
                .map(this::questionMap).collect(Collectors.toList());
    }

    public Map<String, Object> getSessionDetail(Long sessionId, Set<String> roles) {
        requireEvaluator(roles);
        EvaluationSession session = findSession(sessionId);
        List<EvaluationQuestion> questions = questionnaireQuestions(session.getTemplate());
        Map<String, Object> map = sessionMap(session);
        map.put("productionEnabled", genericPassed(sessionId, questions));
        map.put("sections", sectionsForQuestionnaire(session.getTemplate(), questions));
        map.put("answers", answerRepo.findBySessionId(sessionId).stream().map(a -> {
            Map<String, Object> answer = new LinkedHashMap<>();
            answer.put("questionId", a.getQuestion().getId());
            answer.put("answer", a.getAnswer());
            answer.put("comment", a.getComment());
            return answer;
        }).collect(Collectors.toList()));
        return map;
    }

    public List<Map<String, Object>> getPendingEvaluationsForOperator(Long operatorId, Set<String> roles) {
        requireEvaluator(roles);
        return completedFormations(operatorId).stream()
                .filter(f -> !sessionRepo.existsByOperatorIdAndPracticalFormationId(operatorId, f.getId()))
                .map(this::pendingMap).collect(Collectors.toList());
    }

    public List<Map<String, Object>> getAllPendingEvaluations(Set<String> roles) {
        requireEvaluator(roles);
        return formationRepo.findAll().stream()
                .filter(f -> "COMPLETED".equals(f.getStatus()))
                .filter(f -> !sessionRepo.existsByOperatorIdAndPracticalFormationId(f.getOperator().getId(), f.getId()))
                .map(this::pendingMap).collect(Collectors.toList());
    }

    public Map<String, Object> getPolyvalenceMatrix(Set<String> roles) {
        requireEvaluator(roles);
        List<Map<String, Object>> rows = new ArrayList<>();
        for (Operator operator : operatorRepo.findAll()) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("operatorId", operator.getId());
            row.put("operatorName", operator.getLastName() + " " + operator.getFirstName());
            Map<String, String> levels = new LinkedHashMap<>();
            for (Workstation workstation : workstationRepo.findAll()) levels.put(workstation.getName(), latestLevel(operator.getId(), workstation.getId()));
            row.put("workstations", levels);
            rows.add(row);
        }
        return Map.of("workstations", workstationRepo.findAll().stream().map(w -> Map.of("id", w.getId(), "name", w.getName())).collect(Collectors.toList()),
                "operators", rows);
    }

    private List<EvaluationQuestion> questionnaireQuestions(EvaluationTemplate productionTemplate) {
        List<EvaluationTemplate> templates = new ArrayList<>(templateRepo.findValidatedGenericTemplates(List.of(
                EvaluationTemplate.TemplateType.GENERIC_HSE, EvaluationTemplate.TemplateType.GENERIC_QUALITY,
                EvaluationTemplate.TemplateType.GENERIC_COMMON)));
        templates.add(productionTemplate);
        return templates.stream().flatMap(t -> publishedQuestions(t.getId()).stream())
                .filter(q -> questionCategory(q) != EvaluationQuestion.QuestionCategory.ANIMATION)
                .filter(q -> isGeneric(questionCategory(q)) || questionCategory(q) == EvaluationQuestion.QuestionCategory.PRODUCTION)
                .sorted(Comparator.comparing((EvaluationQuestion q) -> isGeneric(questionCategory(q)) ? 0 : 1)
                        .thenComparing(q -> q.getSection() == null ? 0 : q.getSection().getDisplayOrder(), Comparator.nullsLast(Integer::compareTo))
                        .thenComparing(EvaluationQuestion::getQuestionNumber, Comparator.nullsLast(Integer::compareTo)))
                .collect(Collectors.toList());
    }

    private List<Map<String, Object>> sectionsForQuestionnaire(EvaluationTemplate production, List<EvaluationQuestion> questions) {
        List<EvaluationTemplate> templates = new ArrayList<>(templateRepo.findValidatedGenericTemplates(List.of(
                EvaluationTemplate.TemplateType.GENERIC_HSE, EvaluationTemplate.TemplateType.GENERIC_QUALITY,
                EvaluationTemplate.TemplateType.GENERIC_COMMON)));
        templates.add(production);
        return sectionMaps(templates, questions);
    }

    private List<Map<String, Object>> sectionsForTemplates(List<EvaluationTemplate> templates, boolean validatedOnly) {
        List<EvaluationQuestion> questions = templates.stream().flatMap(t -> (validatedOnly
                ? publishedQuestions(t.getId()) : questionRepo.findByTemplateId(t.getId())).stream()).collect(Collectors.toList());
        return sectionMaps(templates, questions);
    }

    private List<Map<String, Object>> sectionMaps(List<EvaluationTemplate> templates, List<EvaluationQuestion> questions) {
        List<EvaluationSection> sections = templates.stream()
                .flatMap(t -> sectionRepo.findByTemplateIdOrderByDisplayOrderAsc(t.getId()).stream())
                .sorted(Comparator.comparing(EvaluationSection::getDisplayOrder, Comparator.nullsLast(Integer::compareTo)))
                .collect(Collectors.toList());
        List<Map<String, Object>> result = new ArrayList<>();
        for (EvaluationSection section : sections) {
            Map<String, Object> map = sectionMap(section);
            map.put("questions", questions.stream().filter(q -> q.getSection() != null && q.getSection().getId().equals(section.getId()))
                    .map(this::questionMap).collect(Collectors.toList()));
            result.add(map);
        }
        return result;
    }

    private WorkstationFormation resolveFormation(Long operatorId, Long formationId) {
        if (formationId != null) {
            WorkstationFormation formation = formationRepo.findById(formationId).orElseThrow(() -> notFound("Formation pratique introuvable"));
            if (!formation.getOperator().getId().equals(operatorId) || !"COMPLETED".equals(formation.getStatus())) {
                throw badRequest("La formation pratique doit etre terminee et appartenir a cet operateur");
            }
            return formation;
        }
        return completedFormations(operatorId).stream().max(Comparator.comparing(WorkstationFormation::getEndDate,
                Comparator.nullsLast(Comparator.naturalOrder()))).orElseThrow(() -> badRequest("Aucune formation pratique terminee"));
    }

    private EvaluationTemplate selectProductionTemplate(Long workstationId) {
        List<EvaluationTemplate> templates = templateRepo.findValidatedProductionForWorkstation(workstationId);
        if (templates.isEmpty()) throw badRequest("Aucun questionnaire Production valide pour ce poste");
        if (templates.size() > 1) throw badRequest("Plusieurs questionnaires Production sont assignes a ce poste");
        return templates.get(0);
    }

    private List<WorkstationFormation> completedFormations(Long operatorId) {
        return formationRepo.findAll().stream().filter(f -> f.getOperator().getId().equals(operatorId) && "COMPLETED".equals(f.getStatus())).collect(Collectors.toList());
    }

    private boolean genericPassed(Long sessionId, List<EvaluationQuestion> questions) {
        List<EvaluationQuestion> generic = questions.stream().filter(q -> isGeneric(questionCategory(q))).collect(Collectors.toList());
        Map<Long, EvaluationAnswer> answers = answerRepo.findBySessionId(sessionId).stream()
                .collect(Collectors.toMap(a -> a.getQuestion().getId(), a -> a));
        return !generic.isEmpty() && generic.stream().allMatch(q -> answers.containsKey(q.getId()) && answers.get(q.getId()).getAnswer() == 1);
    }

    private boolean isGeneric(EvaluationQuestion.QuestionCategory category) {
        return category != EvaluationQuestion.QuestionCategory.PRODUCTION && category != EvaluationQuestion.QuestionCategory.ANIMATION;
    }

    private EvaluationQuestion.QuestionCategory questionCategory(EvaluationQuestion question) {
        if (question.getSection() != null && question.getSection().getDomain() != null) {
            return question.getSection().getDomain().getQuestionCategory();
        }
        if (question.getCategory() != null) return question.getCategory();
        return question.getSection() == null
                ? sectionDomainForTemplate(question.getTemplate().getType(), null).getQuestionCategory()
                : effectiveSectionDomain(question.getSection()).getQuestionCategory();
    }

    private EvaluationSection.SectionDomain effectiveSectionDomain(EvaluationSection section) {
        if (section.getDomain() != null) return section.getDomain();
        EvaluationTemplate.TemplateType type = section.getTemplate().getType();
        if (type != EvaluationTemplate.TemplateType.GENERIC_COMMON) {
            return sectionDomainForTemplate(type, null);
        }
        List<EvaluationQuestion> existing = questionRepo.findByTemplateId(section.getTemplate().getId()).stream()
                .filter(question -> question.getSection() != null && section.getId().equals(question.getSection().getId()))
                .filter(question -> question.getCategory() != null)
                .toList();
        if (!existing.isEmpty()) {
            return domainForCategory(existing.get(0).getCategory());
        }
        return EvaluationSection.SectionDomain.FIVE_S;
    }

    private EvaluationSection.SectionDomain sectionDomainForTemplate(EvaluationTemplate.TemplateType type, String domainName) {
        List<EvaluationSection.SectionDomain> permitted = sectionDomainsForTemplate(type);
        if (permitted.size() == 1) {
            EvaluationSection.SectionDomain domain = permitted.get(0);
            if (domainName != null && !domain.name().equals(domainName)) {
                throw badRequest("Domaine incompatible avec le template");
            }
            return domain;
        }
        EvaluationSection.SectionDomain domain = enumValue(EvaluationSection.SectionDomain.class, domainName, "Domaine de section invalide");
        if (!permitted.contains(domain)) throw badRequest("Domaine incompatible avec le template");
        return domain;
    }

    private List<EvaluationSection.SectionDomain> sectionDomainsForTemplate(EvaluationTemplate.TemplateType type) {
        return switch (type) {
            case GENERIC_HSE -> List.of(EvaluationSection.SectionDomain.SECURITY_ENVIRONMENT);
            case GENERIC_QUALITY -> List.of(EvaluationSection.SectionDomain.QUALITY);
            case GENERIC_COMMON -> List.of(EvaluationSection.SectionDomain.SECURITY_ENVIRONMENT,
                    EvaluationSection.SectionDomain.QUALITY, EvaluationSection.SectionDomain.FIVE_S,
                    EvaluationSection.SectionDomain.TRACEABILITY, EvaluationSection.SectionDomain.PRODUCTION_ALARMS);
            case POSTE_PRODUCTION -> List.of(EvaluationSection.SectionDomain.PRODUCTION);
            case ANIMATION -> List.of(EvaluationSection.SectionDomain.ANIMATION);
        };
    }

    private EvaluationSection.SectionDomain domainForCategory(EvaluationQuestion.QuestionCategory category) {
        return switch (category) {
            case HSE, SECURITY_ENVIRONMENT -> EvaluationSection.SectionDomain.SECURITY_ENVIRONMENT;
            case QUALITY -> EvaluationSection.SectionDomain.QUALITY;
            case FIVE_S -> EvaluationSection.SectionDomain.FIVE_S;
            case TRACEABILITY -> EvaluationSection.SectionDomain.TRACEABILITY;
            case PRODUCTION_ALARMS -> EvaluationSection.SectionDomain.PRODUCTION_ALARMS;
            case PRODUCTION -> EvaluationSection.SectionDomain.PRODUCTION;
            case ANIMATION -> EvaluationSection.SectionDomain.ANIMATION;
        };
    }

    private boolean canAnswer(EvaluationQuestion.QuestionCategory category, Set<String> roles) {
        return has(roles, "ADMIN") || has(roles, domainForCategory(category).getResponsibleRole().name());
    }

    private void requireQualityApproval(EvaluationQuestion question, Set<String> roles) {
        if (questionCategory(question) != EvaluationQuestion.QuestionCategory.QUALITY || !has(roles, "RESP_QUALITE")) {
            throw forbidden("Seul RESP_QUALITE peut valider une question Qualite");
        }
    }

    private void requireTemplateOwner(EvaluationTemplate.TemplateType type, Set<String> roles) {
        if (type == EvaluationTemplate.TemplateType.GENERIC_HSE && !has(roles, "RESP_HSE") && !has(roles, "ADMIN")) throw forbidden("RESP_HSE gere les questions Securite/Environnement");
        if (type == EvaluationTemplate.TemplateType.GENERIC_QUALITY && !has(roles, "AGENT_QUALITE") && !has(roles, "ADMIN")) throw forbidden("AGENT_QUALITE gere les questions Qualite");
        if (type == EvaluationTemplate.TemplateType.GENERIC_COMMON
                && !has(roles, "CHEF_EQUIPE") && !has(roles, "RESP_HSE") && !has(roles, "AGENT_QUALITE") && !has(roles, "ADMIN")) {
            throw forbidden("Un responsable de domaine gere les sections de la partie generique");
        }
        if ((type == EvaluationTemplate.TemplateType.POSTE_PRODUCTION || type == EvaluationTemplate.TemplateType.ANIMATION)
                && !has(roles, "CHEF_EQUIPE") && !has(roles, "ADMIN")) throw forbidden("CHEF_EQUIPE gere ce template");
    }

    private void requireTemplateDomain(EvaluationTemplate template, EvaluationSection.SectionDomain domain) {
        if (!sectionDomainsForTemplate(template.getType()).contains(domain)) {
            throw badRequest("Domaine de section incompatible avec le template");
        }
    }

    private void requireRole(EvaluationQuestion.ValidatorRole role, Set<String> roles) {
        if (!has(roles, role.name()) && !has(roles, "ADMIN")) throw forbidden("Role fonctionnel insuffisant");
    }

    private void requireEvaluator(Set<String> roles) {
        if (!has(roles, "ADMIN") && !has(roles, "RESP_HSE") && !has(roles, "AGENT_QUALITE")
                && !has(roles, "RESP_QUALITE") && !has(roles, "CHEF_EQUIPE")) throw forbidden("Role evaluateur requis");
    }

    private boolean has(Set<String> roles, String role) { return roles != null && roles.contains(role); }
    // Existing non-quality questions were created as PENDING by the former workflow.
    // They are treated as published so the migration does not hide live templates.
    private boolean isPublished(EvaluationQuestion question) {
        return question.getStatus() == EvaluationQuestion.QuestionStatus.VALIDATED
                || (question.getStatus() == EvaluationQuestion.QuestionStatus.PENDING
                && questionCategory(question) != EvaluationQuestion.QuestionCategory.QUALITY);
    }
    private List<EvaluationQuestion> publishedQuestions(Long templateId) {
        return questionRepo.findByTemplateId(templateId).stream().filter(this::isPublished).collect(Collectors.toList());
    }
    private int nextQuestionNumber(Long templateId) { return questionRepo.findByTemplateId(templateId).stream().map(EvaluationQuestion::getQuestionNumber).filter(n -> n != null).max(Integer::compareTo).orElse(0) + 1; }
    private Integer integerAnswer(Object value) { try { int answer = Integer.parseInt(value.toString()); if (answer == 0 || answer == 1) return answer; } catch (Exception ignored) { } throw badRequest("La reponse doit etre 0 ou 1"); }
    private double percentage(int value, int total) { return total == 0 ? 0.0 : Math.round((value * 1000.0 / total)) / 10.0; }
    private String determineNiveau(Long seniorityMonths, double production) { if (seniorityMonths < 6) return production >= 70 && production <= 80 ? "I" : "NON_VALIDE"; if (seniorityMonths < 12) return production >= 81 ? "L" : "NON_VALIDE"; return production >= 91 ? "U" : "NON_VALIDE"; }
    private EvaluationTemplate findTemplate(Long id) { return templateRepo.findById(id).orElseThrow(() -> notFound("Template introuvable")); }
    private EvaluationQuestion findQuestion(Long id) { return questionRepo.findById(id).orElseThrow(() -> notFound("Question introuvable")); }
    private EvaluationSession findSession(Long id) { return sessionRepo.findById(id).orElseThrow(() -> notFound("Session introuvable")); }
    private ResponseStatusException notFound(String message) { return new ResponseStatusException(HttpStatus.NOT_FOUND, message); }
    private ResponseStatusException badRequest(String message) { return new ResponseStatusException(HttpStatus.BAD_REQUEST, message); }
    private ResponseStatusException forbidden(String message) { return new ResponseStatusException(HttpStatus.FORBIDDEN, message); }
    private String required(String value, String message) { if (value == null || value.isBlank()) throw badRequest(message); return value; }
    private <T extends Enum<T>> T enumValue(Class<T> type, String value, String message) { try { return Enum.valueOf(type, value); } catch (Exception e) { throw badRequest(message); } }

    private Map<String, Object> templateSummary(EvaluationTemplate template, String message) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", template.getId()); map.put("name", template.getName()); map.put("description", template.getDescription());
        map.put("type", template.getType().name()); map.put("status", template.getStatus().name()); map.put("targetNiveau", template.getTargetNiveau());
        List<Long> ids = template.getWorkstations().stream().map(Workstation::getId).collect(Collectors.toList());
        if (ids.isEmpty() && template.getWorkstation() != null) ids = List.of(template.getWorkstation().getId());
        map.put("workstationIds", ids); map.put("workstationId", ids.isEmpty() ? null : ids.get(0));
        map.put("workstationName", template.getWorkstation() == null ? null : template.getWorkstation().getName());
        map.put("validatedQuestionCount", publishedQuestions(template.getId()).size());
        if (message != null) map.put("message", message);
        return map;
    }

    private Map<String, Object> sectionMap(EvaluationSection section) {
        Map<String, Object> map = new LinkedHashMap<>();
        EvaluationSection.SectionDomain domain = effectiveSectionDomain(section);
        map.put("id", section.getId()); map.put("title", section.getTitle()); map.put("description", section.getDescription());
        map.put("displayOrder", section.getDisplayOrder()); map.put("domain", domain.name());
        map.put("responsibleRole", domain.getResponsibleRole().name());
        return map;
    }

    private Map<String, Object> questionMap(EvaluationQuestion question) {
        Map<String, Object> map = new LinkedHashMap<>();
        EvaluationQuestion.QuestionCategory category = questionCategory(question);
        EvaluationQuestion.ValidatorRole role = question.getSection() != null && question.getSection().getDomain() != null
                ? question.getSection().getDomain().getResponsibleRole()
                : domainForCategory(category).getResponsibleRole();
        map.put("id", question.getId()); map.put("questionText", question.getQuestionText()); map.put("expectedAnswer", question.getExpectedAnswer());
        map.put("questionNumber", question.getQuestionNumber()); map.put("responseType", (question.getResponseType() == null ? EvaluationQuestion.ResponseType.BINARY : question.getResponseType()).name());
        map.put("category", category.name()); map.put("responsibleRole", role.name()); map.put("validatorRole", role.name());
        map.put("status", question.getStatus().name()); map.put("templateId", question.getTemplate().getId()); map.put("sectionId", question.getSection() == null ? null : question.getSection().getId());
        map.put("sectionTitle", question.getSection() == null ? null : question.getSection().getTitle());
        map.put("sectionDomain", question.getSection() == null ? null : effectiveSectionDomain(question.getSection()).name());
        map.put("templateName", question.getTemplate().getName());
        map.put("createdAt", question.getCreatedAt());
        map.put("createdByName", question.getCreatedById() == null ? null : userRepo.findById(question.getCreatedById()).map(User::getName).orElse(null));
        return map;
    }

    private Map<String, Object> sessionMap(EvaluationSession session) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", session.getId()); map.put("operatorId", session.getOperator().getId()); map.put("operatorName", session.getOperator().getLastName() + " " + session.getOperator().getFirstName());
        map.put("templateId", session.getTemplate().getId()); map.put("templateName", session.getTemplate().getName());
        map.put("formationId", session.getPracticalFormation() != null ? session.getPracticalFormation().getId()
                : session.getFormation() == null ? null : session.getFormation().getId());
        map.put("workstationName", session.getPracticalFormation() != null ? session.getPracticalFormation().getWorkstation().getName()
                : session.getFormation() == null ? null : session.getFormation().getWorkstation().getName());
        if (session.getPracticalFormation() != null && session.getPracticalFormation().getWorkstation().getZone() != null
                && session.getPracticalFormation().getWorkstation().getZone().getProject() != null) {
            map.put("projectId", session.getPracticalFormation().getWorkstation().getZone().getProject().getId());
            map.put("projectName", session.getPracticalFormation().getWorkstation().getZone().getProject().getName());
        }
        map.put("evaluatorName", session.getEvaluatorName()); map.put("status", session.getStatus().name()); map.put("genericTotal", session.getGenericTotal()); map.put("genericCorrect", session.getGenericCorrect()); map.put("genericPercentage", session.getGenericPercentage());
        map.put("productionTotal", session.getProductionTotal()); map.put("productionCorrect", session.getProductionCorrect()); map.put("productionPercentage", session.getProductionPercentage());
        map.put("totalQuestions", session.getTotalQuestions()); map.put("correctAnswers", session.getCorrectAnswers()); map.put("scorePercentage", session.getScorePercentage());
        map.put("decision", session.getDecision()); map.put("niveau", session.getNiveau()); map.put("seniorityMonths", session.getOperatorSeniorityMonths());
        return map;
    }

    private Map<String, Object> pendingMap(WorkstationFormation formation) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("operatorId", formation.getOperator().getId()); item.put("operatorName", formation.getOperator().getLastName() + " " + formation.getOperator().getFirstName());
        item.put("operatorEmployeeId", formation.getOperator().getEmployeeId()); item.put("formationId", formation.getId());
        item.put("workstationId", formation.getWorkstation().getId()); item.put("workstationName", formation.getWorkstation().getName());
        if (formation.getWorkstation().getZone() != null && formation.getWorkstation().getZone().getProject() != null) {
            item.put("projectId", formation.getWorkstation().getZone().getProject().getId());
            item.put("projectName", formation.getWorkstation().getZone().getProject().getName());
        }
        item.put("seniorityMonths", formation.getOperator().getHireDate() == null ? 0 : Period.between(formation.getOperator().getHireDate(), LocalDate.now()).toTotalMonths());
        return item;
    }

    private Map<String, Object> buildResult(EvaluationSession session) {
        Map<String, Object> result = sessionMap(session);
        result.put("sessionId", session.getId());
        return result;
    }

    private String latestLevel(Long operatorId, Long workstationId) {
        return sessionRepo.findByOperatorIdOrderByCreatedAtDesc(operatorId).stream()
                .filter(s -> (s.getPracticalFormation() != null && s.getPracticalFormation().getWorkstation().getId().equals(workstationId))
                        || (s.getPracticalFormation() == null && s.getTemplate().getWorkstation() != null
                        && s.getTemplate().getWorkstation().getId().equals(workstationId)))
                .filter(s -> s.getStatus() == EvaluationSession.SessionStatus.PASSED).findFirst().map(EvaluationSession::getNiveau).orElse("-");
    }
}
