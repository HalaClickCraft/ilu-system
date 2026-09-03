package com.ilu.system.recyclage.service;

import com.ilu.system.evaluation.repository.EvaluationSessionRepository;
import com.ilu.system.evaluation.entity.EvaluationTemplate;
import com.ilu.system.evaluation.repository.EvaluationTemplateRepository;
import com.ilu.system.notification.service.NotificationService;
import com.ilu.system.operator.entity.Operator;
import com.ilu.system.operator.entity.WorkstationFormation;
import com.ilu.system.operator.repository.OperatorRepository;
import com.ilu.system.operator.repository.WorkstationFormationRepository;
import com.ilu.system.recyclage.entity.RecyclagePlanning;
import com.ilu.system.recyclage.repository.RecyclagePlanningRepository;
import com.ilu.system.structure.entity.Workstation;
import com.ilu.system.structure.repository.WorkstationRepository;
import org.springframework.stereotype.Service;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.ilu.system.recyclage.entity.RecyclagePlanning.PlanningSource;
import static com.ilu.system.recyclage.entity.RecyclagePlanning.PlanningStatus;
import static com.ilu.system.recyclage.entity.RecyclagePlanning.PlanningType;

@Service
public class RecyclageService {

    private final RecyclagePlanningRepository recyclagePlanningRepository;
    private final OperatorRepository operatorRepository;
    private final WorkstationFormationRepository workstationFormationRepository;
    private final WorkstationRepository workstationRepository;
    private final EvaluationSessionRepository evaluationSessionRepository;
    private final EvaluationTemplateRepository evaluationTemplateRepository;
    private final NotificationService notificationService;
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public RecyclageService(RecyclagePlanningRepository recyclagePlanningRepository,
                            OperatorRepository operatorRepository,
                            WorkstationFormationRepository workstationFormationRepository,
                            WorkstationRepository workstationRepository,
                            EvaluationSessionRepository evaluationSessionRepository,
                            EvaluationTemplateRepository evaluationTemplateRepository,
                            NotificationService notificationService) {
        this.recyclagePlanningRepository = recyclagePlanningRepository;
        this.operatorRepository = operatorRepository;
        this.workstationFormationRepository = workstationFormationRepository;
        this.workstationRepository = workstationRepository;
        this.evaluationSessionRepository = evaluationSessionRepository;
        this.evaluationTemplateRepository = evaluationTemplateRepository;
        this.notificationService = notificationService;
    }

    /** Returns the correct question flow for a recyclage or an initial planning. */
    @Transactional
    public Map<String, Object> startEvaluation(Long planningId) {
        RecyclagePlanning planning = recyclagePlanningRepository.findById(planningId)
                .orElseThrow(() -> new RuntimeException("Planning not found with id: " + planningId));

        if (planning.getStatus() == PlanningStatus.TERMINEE || planning.getStatus() == PlanningStatus.ANNULEE) {
            throw new IllegalArgumentException("Ce planning est deja termine ou annule");
        }

        EvaluationTemplate template = evaluationTemplateRepository
                .findValidatedProductionForWorkstation(planning.getWorkstation().getId())
                .stream()
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Aucun template de questions valide pour le poste '" + planning.getWorkstation().getName() + "'"));

        boolean newHireInitial = planning.getType() == PlanningType.INITIALE_NOUVELLE_RECRUE
                || planning.getType() == PlanningType.INITIALE;
        EvaluationTemplate firstTemplate = template;
        Long nextTemplateId = null;
        String mode = (planning.getType() == PlanningType.RECYCLAGE 
                || planning.getType() == PlanningType.RECYCLAGE_NOUVELLE_RECRUE
                || planning.getType() == PlanningType.EVALUATION_ANNUELLE_MOIS_7) ? "RECYCLAGE"
                : planning.getType() == PlanningType.EVALUATION_ANNUELLE_MOIS_1 ? "ANNUELLE"
                : "NOUVELLE_RECRUE";

        // A new recruit must complete the generic questions before the production questions.
        if (newHireInitial && !evaluationSessionRepository.hasPassedGeneric(planning.getOperator().getId())) {
            firstTemplate = evaluationTemplateRepository
                    .findByTypeAndStatus(EvaluationTemplate.TemplateType.GENERIC_COMMON,
                            EvaluationTemplate.TemplateStatus.VALIDATED)
                    .stream()
                    .filter(candidate -> candidate.getWorkstation() == null)
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Aucun template generique valide n'est disponible"));
            nextTemplateId = template.getId();
        }

        planning.setStatus(PlanningStatus.EN_COURS);
        recyclagePlanningRepository.save(planning);

        // FIX 3: no one else was told when a recyclage started - other roles only found out
        // on their next refresh of the planning list. Notify chefs d'équipe/HR right away.
        if (planning.getType() == PlanningType.RECYCLAGE 
                || planning.getType() == PlanningType.RECYCLAGE_NOUVELLE_RECRUE
                || planning.getType() == PlanningType.EVALUATION_ANNUELLE_MOIS_7) {
            String operatorName = planning.getOperator().getLastName() + " " + planning.getOperator().getFirstName();
            notificationService.createRecyclageStartedNotification(
                    planning.getId(), planning.getOperator().getId(), operatorName, planning.getWorkstation().getName());
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("planningId", planning.getId());
        result.put("operatorId", planning.getOperator().getId());
        result.put("templateId", firstTemplate.getId());
        result.put("templateName", firstTemplate.getName());
        result.put("nextTemplateId", nextTemplateId);
        result.put("mode", mode);
        result.put("workstationName", planning.getWorkstation().getName());
        return result;
    }

    @Transactional
    public Map<String, Object> generateCurrentSemesterEvaluations() {
        int created = 0;
        int skipped = 0;

        LocalDate today = LocalDate.now();
        int currentYear = today.getYear();
        boolean isS1 = today.getMonthValue() < 7;

        List<Operator> operators = operatorRepository.findByActiveTrue();
        LocalDate targetDate = isS1 ? LocalDate.of(currentYear, 1, 26) : LocalDate.of(currentYear, 7, 26);
        PlanningType targetType = isS1 ? PlanningType.EVALUATION_ANNUELLE_MOIS_1 : PlanningType.RECYCLAGE;

        for (Operator operator : operators) {
            final Long operatorId = operator.getId();
            List<WorkstationFormation> formations = workstationFormationRepository.findByOperator_IdAndStatus(operatorId, "COMPLETED");

            for (WorkstationFormation formation : formations) {
                final Workstation ws = formation.getWorkstation();
                if (ws == null || ws.getZone() == null || ws.getZone().getProject() == null) {
                    skipped++;
                    continue;
                }

                final Long wsId = ws.getId();
                final Long projectId = ws.getZone().getProject().getId();

                boolean exists = recyclagePlanningRepository.existsByOperator_IdAndWorkstation_IdAndScheduledDateAndType(
                        operatorId, wsId, targetDate, targetType);
                if (!exists) {
                    RecyclagePlanning planning = new RecyclagePlanning();
                    planning.setOperator(operator);
                    planning.setWorkstation(ws);
                    planning.setType(targetType);
                    planning.setScheduledDate(targetDate);
                    planning.setStatus(PlanningStatus.PLANIFIEE);
                    planning.setSource(PlanningSource.ANNUELLE);
                    planning.setProjectId(projectId);
                    recyclagePlanningRepository.save(planning);
                    created++;
                } else {
                    skipped++;
                }
            }
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("created", created);
        result.put("skipped", skipped);
        result.put("year", currentYear);
        result.put("semester", isS1 ? "S1" : "S2");
        return result;
    }

    @Transactional
    public Map<String, Object> generateAnnualEvaluations(int year) {
        int created = 0;
        int skipped = 0;

        List<Operator> operators = operatorRepository.findByActiveTrue();
        LocalDate januaryDate = LocalDate.of(year, 1, 26);
        LocalDate julyDate = LocalDate.of(year, 7, 26);

        for (Operator operator : operators) {
            final Long operatorId = operator.getId();
            List<WorkstationFormation> formations = workstationFormationRepository.findByOperator_IdAndStatus(operatorId, "COMPLETED");

            for (WorkstationFormation formation : formations) {
                final Workstation ws = formation.getWorkstation();
                if (ws == null || ws.getZone() == null || ws.getZone().getProject() == null) {
                    skipped++;
                    continue;
                }

                final Long wsId = ws.getId();
                final Long projectId = ws.getZone().getProject().getId();

                // Month 1 annual evaluation for operators already in post.
                boolean janExists = recyclagePlanningRepository.existsByOperator_IdAndWorkstation_IdAndScheduledDateAndType(
                        operatorId, wsId, januaryDate, PlanningType.EVALUATION_ANNUELLE_MOIS_1);
                if (!janExists) {
                    RecyclagePlanning janPlanning = new RecyclagePlanning();
                    janPlanning.setOperator(operator);
                    janPlanning.setWorkstation(ws);
                    janPlanning.setType(PlanningType.EVALUATION_ANNUELLE_MOIS_1);
                    janPlanning.setScheduledDate(januaryDate);
                    janPlanning.setStatus(PlanningStatus.PLANIFIEE);
                    janPlanning.setSource(PlanningSource.ANNUELLE);
                    janPlanning.setProjectId(projectId);
                    recyclagePlanningRepository.save(janPlanning);
                    created++;
                } else {
                    skipped++;
                }

                // July RECYCLAGE
                boolean julExists = recyclagePlanningRepository.existsByOperator_IdAndWorkstation_IdAndScheduledDateAndType(
                        operatorId, wsId, julyDate, PlanningType.RECYCLAGE);
                if (!julExists) {
                    RecyclagePlanning julPlanning = new RecyclagePlanning();
                    julPlanning.setOperator(operator);
                    julPlanning.setWorkstation(ws);
                    julPlanning.setType(PlanningType.RECYCLAGE);
                    julPlanning.setScheduledDate(julyDate);
                    julPlanning.setStatus(PlanningStatus.PLANIFIEE);
                    julPlanning.setSource(PlanningSource.ANNUELLE);
                    julPlanning.setProjectId(projectId);
                    recyclagePlanningRepository.save(julPlanning);
                    created++;
                } else {
                    skipped++;
                }
            }
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("created", created);
        result.put("skipped", skipped);
        result.put("year", year);
        return result;
    }

    @Transactional
    public void cleanupFutureUnstartedAnnualPlannings() {
        LocalDate currentYearEnd = LocalDate.of(LocalDate.now().getYear(), 12, 31);
        List<RecyclagePlanning> futurePlannings = recyclagePlanningRepository.findAll().stream()
                .filter(p -> p.getStatus() == PlanningStatus.PLANIFIEE 
                          && p.getEvaluationSessionId() == null
                          && p.getSource() == PlanningSource.ANNUELLE
                          && p.getScheduledDate() != null
                          && p.getScheduledDate().isAfter(currentYearEnd))
                .toList();
        if (!futurePlannings.isEmpty()) {
            recyclagePlanningRepository.deleteAll(futurePlannings);
        }
    }

    /** Ensures only current semester evaluations exist automatically without prematurely creating future years. */
    @Scheduled(cron = "0 5 0 * * *")
    public void generateCurrentYearAnnualEvaluations() {
        cleanupFutureUnstartedAnnualPlannings();
        generateCurrentSemesterEvaluations();
    }

    @EventListener(ApplicationReadyEvent.class)
    public void generateCurrentYearAnnualEvaluationsOnStartup() {
        try {
            cleanupFutureUnstartedAnnualPlannings();
            generateCurrentSemesterEvaluations();
        } catch (Exception e) {
            System.err.println("Failed to generate annual evaluations on startup: " + e.getMessage());
        }
    }

    @Transactional
    public Map<String, Object> generateNewHirePlanning(Long operatorId) {
        Operator operator = operatorRepository.findById(operatorId)
                .orElseThrow(() -> new RuntimeException("Operator not found with id: " + operatorId));

        List<WorkstationFormation> formations = workstationFormationRepository.findByOperator_IdAndStatus(operatorId, "COMPLETED");
        if (formations.isEmpty()) {
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("created", 0);
            result.put("message", "No completed formations found for this operator");
            return result;
        }

        int created = 0;
        LocalDate earliestEndDate = null;

        for (WorkstationFormation formation : formations) {
            final Workstation ws = formation.getWorkstation();
            if (ws == null) {
                continue;
            }

            final Long wsId = ws.getId();
            final Long projectId = (ws.getZone() != null && ws.getZone().getProject() != null)
                    ? ws.getZone().getProject().getId() : null;

            // Initial evaluation after the new recruit's completed training.
            boolean initExists = formation.getEndDate() != null
                    && recyclagePlanningRepository.existsByOperator_IdAndWorkstation_IdAndScheduledDateAndType(
                    operatorId, wsId, formation.getEndDate(), PlanningType.INITIALE_NOUVELLE_RECRUE);
            if (!initExists) {
                RecyclagePlanning initPlanning = new RecyclagePlanning();
                initPlanning.setOperator(operator);
                initPlanning.setWorkstation(ws);
                initPlanning.setType(PlanningType.INITIALE_NOUVELLE_RECRUE);
                initPlanning.setScheduledDate(formation.getEndDate());
                initPlanning.setStatus(PlanningStatus.PLANIFIEE);
                initPlanning.setSource(PlanningSource.NOUVELLE_RECRUE);
                initPlanning.setProjectId(projectId);
                recyclagePlanningRepository.save(initPlanning);
                created++;
            }

            // Track earliest end date for recyclage
            if (formation.getEndDate() != null) {
                if (earliestEndDate == null || formation.getEndDate().isBefore(earliestEndDate)) {
                    earliestEndDate = formation.getEndDate();
                }
            }
        }

        // Create RECYCLAGE planning for ALL workstations at earliest + 6 months
        LocalDate recyclageDate = null;
        if (earliestEndDate != null) {
            recyclageDate = earliestEndDate.plusMonths(6);

            for (WorkstationFormation formation : formations) {
                final Workstation ws = formation.getWorkstation();
                if (ws == null) {
                    continue;
                }
                final Long wsId = ws.getId();
                final Long projectId = (ws.getZone() != null && ws.getZone().getProject() != null)
                        ? ws.getZone().getProject().getId() : null;

                boolean recyExists = recyclagePlanningRepository.existsByOperator_IdAndWorkstation_IdAndScheduledDateAndType(
                        operatorId, wsId, recyclageDate, PlanningType.RECYCLAGE);
                if (!recyExists) {
                    RecyclagePlanning recyPlanning = new RecyclagePlanning();
                    recyPlanning.setOperator(operator);
                    recyPlanning.setWorkstation(ws);
                    recyPlanning.setType(PlanningType.RECYCLAGE);
                    recyPlanning.setScheduledDate(recyclageDate);
                    recyPlanning.setStatus(PlanningStatus.PLANIFIEE);
                    recyPlanning.setSource(PlanningSource.NOUVELLE_RECRUE);
                    recyPlanning.setProjectId(projectId);
                    recyclagePlanningRepository.save(recyPlanning);
                    created++;
                }
            }
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("created", created);
        result.put("recyclageDate", recyclageDate != null ? recyclageDate.format(dateFormatter) : null);
        result.put("operatorId", operatorId);
        return result;
    }

    @Transactional
    public Map<String, Object> generateReturnFromAbsence(Long operatorId, LocalDate returnDate) {
        List<WorkstationFormation> formations = workstationFormationRepository.findByOperator_IdAndStatus(operatorId, "COMPLETED");
        Operator operator = operatorRepository.findById(operatorId)
                .orElseThrow(() -> new RuntimeException("Operator not found with id: " + operatorId));

        int created = 0;
        LocalDate scheduledDate = returnDate != null ? returnDate : LocalDate.now();

        for (WorkstationFormation formation : formations) {
            final Workstation ws = formation.getWorkstation();
            if (ws == null) {
                continue;
            }

            final Long wsId = ws.getId();
            final Long projectId = (ws.getZone() != null && ws.getZone().getProject() != null)
                    ? ws.getZone().getProject().getId() : null;

            boolean exists = recyclagePlanningRepository.existsByOperator_IdAndWorkstation_IdAndScheduledDateAndType(
                    operatorId, wsId, scheduledDate, PlanningType.RECYCLAGE);
            if (!exists) {
                RecyclagePlanning planning = new RecyclagePlanning();
                planning.setOperator(operator);
                planning.setWorkstation(ws);
                planning.setType(PlanningType.RECYCLAGE);
                planning.setScheduledDate(scheduledDate);
                planning.setStatus(PlanningStatus.PLANIFIEE);
                planning.setSource(PlanningSource.REPRISE_ABSENCE);
                planning.setProjectId(projectId);
                recyclagePlanningRepository.save(planning);
                created++;
            }
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("created", created);
        result.put("operatorId", operatorId);
        return result;
    }

    /** Lets a team leader activate a recycling evaluation for one qualified operator. */
    @Transactional
    public Map<String, Object> createManualRecyclage(Long operatorId, Long workstationId, LocalDate scheduledDate) {
        Operator operator = operatorRepository.findById(operatorId)
                .orElseThrow(() -> new IllegalArgumentException("Operateur introuvable"));
        if (!Boolean.TRUE.equals(operator.getActive())) {
            throw new IllegalArgumentException("L'operateur doit etre actif pour planifier un recyclage");
        }
        Workstation workstation = workstationRepository.findById(workstationId)
                .orElseThrow(() -> new IllegalArgumentException("Poste de travail introuvable"));
        boolean qualifiedForWorkstation = workstationFormationRepository
                .findByOperator_IdAndStatus(operatorId, "COMPLETED").stream()
                .anyMatch(formation -> workstationId.equals(formation.getWorkstation().getId()));
        if (!qualifiedForWorkstation) {
            throw new IllegalArgumentException("L'operateur n'est pas forme sur ce poste");
        }
        LocalDate date = scheduledDate != null ? scheduledDate : LocalDate.now();
        boolean exists = recyclagePlanningRepository.existsByOperator_IdAndWorkstation_IdAndScheduledDateAndType(
                operatorId, workstationId, date, PlanningType.RECYCLAGE);
        if (exists) {
            throw new IllegalArgumentException("Un recyclage est deja planifie pour cet operateur, ce poste et cette date");
        }
        RecyclagePlanning planning = new RecyclagePlanning();
        planning.setOperator(operator);
        planning.setWorkstation(workstation);
        planning.setType(PlanningType.RECYCLAGE);
        planning.setScheduledDate(date);
        planning.setStatus(PlanningStatus.PLANIFIEE);
        planning.setSource(PlanningSource.CHEF_EQUIPE);
        planning.setProjectId(workstation.getZone() != null && workstation.getZone().getProject() != null
                ? workstation.getZone().getProject().getId() : null);
        recyclagePlanningRepository.save(planning);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", planning.getId());
        result.put("message", "Recyclage active avec succes");
        return result;
    }

    public Long getPlanningProjectId(Long planningId) {
        RecyclagePlanning planning = recyclagePlanningRepository.findById(planningId)
                .orElseThrow(() -> new IllegalArgumentException("Planning introuvable"));
        if (planning.getProjectId() != null) {
            return planning.getProjectId();
        }
        if (planning.getWorkstation() != null && planning.getWorkstation().getZone() != null 
                && planning.getWorkstation().getZone().getProject() != null) {
            return planning.getWorkstation().getZone().getProject().getId();
        }
        return null;
    }

    public Long getWorkstationProjectId(Long workstationId) {
        Workstation workstation = workstationRepository.findById(workstationId)
                .orElseThrow(() -> new IllegalArgumentException("Poste de travail introuvable"));
        return workstation.getZone() != null && workstation.getZone().getProject() != null
                ? workstation.getZone().getProject().getId() : null;
    }

    public List<Map<String, Object>> getPlanningByMonth(int month, int year, Long projectId) {
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate firstDay = yearMonth.atDay(1);
        LocalDate lastDay = yearMonth.atEndOfMonth();

        List<RecyclagePlanning> plannings = recyclagePlanningRepository.findByScheduledDateBetween(firstDay, lastDay);

        List<Map<String, Object>> result = new ArrayList<>();
        for (RecyclagePlanning planning : plannings) {
            if (planning.getOperator() != null && !Boolean.TRUE.equals(planning.getOperator().getActive())) {
                continue;
            }
            if (projectId != null && !projectId.equals(planning.getProjectId())) {
                continue;
            }
            final Long opId = planning.getOperator().getId();
            final Long wsId = planning.getWorkstation().getId();
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", planning.getId());
            map.put("operatorName", planning.getOperator().getLastName() + " " + planning.getOperator().getFirstName());
            map.put("operatorId", opId);
            map.put("workstationName", planning.getWorkstation().getName());
            map.put("workstationId", wsId);
            map.put("type", planning.getType().name());
            map.put("scheduledDate", planning.getScheduledDate().format(dateFormatter));
            map.put("status", planning.getStatus().name());
            map.put("source", planning.getSource().name());
            map.put("niveauObtenu", planning.getNiveauObtenu());
            result.add(map);
        }
        return result;
    }

    public List<Map<String, Object>> getAllPlanning(Long projectId, String statusFilter, Long operatorId,
                                                     String typeFilter, String search) {
        List<RecyclagePlanning> plannings = recyclagePlanningRepository.findAll();

        List<Map<String, Object>> result = new ArrayList<>();
        for (RecyclagePlanning planning : plannings) {
            if (planning.getOperator() != null && !Boolean.TRUE.equals(planning.getOperator().getActive())) {
                continue;
            }
            if (projectId != null && !projectId.equals(planning.getProjectId())) {
                continue;
            }
            if (statusFilter != null && !statusFilter.isEmpty()) {
                try {
                    PlanningStatus filterStatus = PlanningStatus.valueOf(statusFilter);
                    if (planning.getStatus() != filterStatus) {
                        continue;
                    }
                } catch (IllegalArgumentException e) {
                    
                }
            }
            if (operatorId != null && !operatorId.equals(planning.getOperator().getId())) {
                continue;
            }
            if (typeFilter != null && !typeFilter.isBlank()) {
                try {
                    if (planning.getType() != PlanningType.valueOf(typeFilter)) continue;
                } catch (IllegalArgumentException e) {
                    continue;
                }
            }
            if (search != null && !search.isBlank()) {
                String operatorName = (planning.getOperator().getLastName() + " "
                        + planning.getOperator().getFirstName()).toLowerCase();
                String employeeId = planning.getOperator().getEmployeeId();
                String query = search.trim().toLowerCase();
                if (!operatorName.contains(query) && (employeeId == null || !employeeId.toLowerCase().contains(query))) continue;
            }

            final Long opId = planning.getOperator().getId();
            final Long wsId = planning.getWorkstation().getId();
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", planning.getId());
            map.put("operatorName", planning.getOperator().getLastName() + " " + planning.getOperator().getFirstName());
            map.put("operatorId", opId);
            map.put("workstationName", planning.getWorkstation().getName());
            map.put("workstationId", wsId);
            map.put("type", planning.getType().name());
            map.put("scheduledDate", planning.getScheduledDate().format(dateFormatter));
            map.put("status", planning.getStatus().name());
            map.put("source", planning.getSource().name());
            map.put("niveauObtenu", planning.getNiveauObtenu());
            map.put("evaluationSessionId", planning.getEvaluationSessionId());
            map.put("createdAt", planning.getCreatedAt() != null ? planning.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) : null);
            map.put("completedAt", planning.getCompletedAt() != null ? planning.getCompletedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) : null);
            map.put("projectId", planning.getProjectId());
            result.add(map);
        }
        return result;
    }

    @Transactional
    public Map<String, Object> completePlanning(Long planningId, String niveauObtenu, Long evaluationSessionId) {
        RecyclagePlanning planning = recyclagePlanningRepository.findById(planningId)
                .orElseThrow(() -> new RuntimeException("Planning not found with id: " + planningId));

        planning.setStatus(PlanningStatus.TERMINEE);
        planning.setCompletedAt(LocalDateTime.now());
        planning.setNiveauObtenu(niveauObtenu);
        planning.setEvaluationSessionId(evaluationSessionId);
        recyclagePlanningRepository.save(planning);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("message", "Planning completed successfully");
        result.put("planningId", planningId);
        result.put("niveauObtenu", niveauObtenu);
        return result;
    }

    @Transactional
    public Map<String, Object> cancelPlanning(Long planningId) {
        RecyclagePlanning planning = recyclagePlanningRepository.findById(planningId)
                .orElseThrow(() -> new RuntimeException("Planning not found with id: " + planningId));

        planning.setStatus(PlanningStatus.ANNULEE);
        recyclagePlanningRepository.save(planning);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("message", "Planning cancelled successfully");
        result.put("planningId", planningId);
        return result;
    }

    public List<Map<String, Object>> getUpcomingRecyclages(int daysAhead, Long projectId) {
        LocalDate today = LocalDate.now();
        LocalDate futureDate = today.plusDays(daysAhead);

        List<RecyclagePlanning> plannings = recyclagePlanningRepository.findByScheduledDateBetween(today, futureDate);

        List<Map<String, Object>> result = new ArrayList<>();
        for (RecyclagePlanning planning : plannings) {
            if (planning.getStatus() != PlanningStatus.PLANIFIEE) {
                continue;
            }
            if (planning.getOperator() != null && !Boolean.TRUE.equals(planning.getOperator().getActive())) {
                continue;
            }
            if (projectId != null && !projectId.equals(planning.getProjectId())) {
                continue;
            }

            final Long opId = planning.getOperator().getId();
            final Long wsId = planning.getWorkstation().getId();
            final long daysRemaining = ChronoUnit.DAYS.between(today, planning.getScheduledDate());

            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", planning.getId());
            map.put("operatorName", planning.getOperator().getLastName() + " " + planning.getOperator().getFirstName());
            map.put("operatorId", opId);
            map.put("workstationName", planning.getWorkstation().getName());
            map.put("workstationId", wsId);
            map.put("type", planning.getType().name());
            map.put("scheduledDate", planning.getScheduledDate().format(dateFormatter));
            map.put("daysRemaining", daysRemaining);
            map.put("source", planning.getSource().name());
            map.put("projectId", planning.getProjectId());
            result.add(map);
        }
        return result;
    }
}