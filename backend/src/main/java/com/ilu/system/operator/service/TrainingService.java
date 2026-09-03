package com.ilu.system.operator.service;

import com.ilu.system.operator.dto.ChartDataDto;
import com.ilu.system.operator.dto.DailyBatchEntryDto;
import com.ilu.system.operator.dto.DailyTrackingDto;
import com.ilu.system.operator.dto.FormationDetailsDto;
import com.ilu.system.operator.dto.FormationStatisticsDto;
import com.ilu.system.operator.entity.DailyFormationTracking;
import com.ilu.system.operator.entity.FormationAssignment;
import com.ilu.system.operator.entity.Operator;
import com.ilu.system.operator.entity.Team;
import com.ilu.system.operator.entity.WorkstationFormation;
import com.ilu.system.operator.repository.DailyFormationTrackingRepository;
import com.ilu.system.operator.repository.FormationAssignmentRepository;
import com.ilu.system.operator.repository.OperatorRepository;
import com.ilu.system.operator.repository.TeamRepository;
import com.ilu.system.operator.repository.WorkstationFormationRepository;
import com.ilu.system.structure.entity.Project;
import com.ilu.system.structure.entity.Workstation;
import com.ilu.system.structure.entity.Zone;
import com.ilu.system.structure.repository.ProjectRepository;
import com.ilu.system.structure.repository.WorkstationRepository;
import com.ilu.system.structure.repository.ZoneRepository;
import java.util.Optional;

import com.ilu.system.operator.service.OnboardingService;
import com.ilu.system.evaluation.entity.EvaluationSession;
import com.ilu.system.evaluation.repository.EvaluationSessionRepository;
import com.ilu.system.recyclage.service.RecyclageService;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class TrainingService {
    private final WorkstationFormationRepository formationRepo;
    private final FormationAssignmentRepository assignmentRepo;
    private final DailyFormationTrackingRepository trackingRepo;
    private final OperatorRepository operatorRepo;
    private final WorkstationRepository workstationRepo;
    private final TeamRepository teamRepo;
    private final OnboardingService onboardingService;
    private final ProjectRepository projectRepo;
    private final ZoneRepository zoneRepo;
    private final RecyclageService recyclageService;
    private final EvaluationSessionRepository sessionRepo;

    public TrainingService(WorkstationFormationRepository formationRepo, FormationAssignmentRepository assignmentRepo,
                           DailyFormationTrackingRepository trackingRepo, OperatorRepository operatorRepo,
                           WorkstationRepository workstationRepo, TeamRepository teamRepo,
                           OnboardingService onboardingService, ProjectRepository projectRepo,
                           ZoneRepository zoneRepo,
                           RecyclageService recyclageService, EvaluationSessionRepository sessionRepo) {
        this.formationRepo = formationRepo;
        this.assignmentRepo = assignmentRepo;
        this.trackingRepo = trackingRepo;
        this.operatorRepo = operatorRepo;
        this.workstationRepo = workstationRepo;
        this.teamRepo = teamRepo;
        this.onboardingService = onboardingService;
        this.projectRepo = projectRepo;
        this.zoneRepo = zoneRepo;
        this.recyclageService = recyclageService;
        this.sessionRepo = sessionRepo;
    }

    @Transactional
    public List<WorkstationFormation> createFormations(Long workstationId, List<Long> operatorIds,
                                                        String employeeId, Set<String> roles) {
        requireStarter(roles);
        if (operatorIds == null || operatorIds.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Au moins un operateur est requis");
        }
        Workstation workstation = workstationRepo.findById(workstationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Poste de travail introuvable"));
        validateWorkstationAccess(workstation, employeeId, roles);

        List<WorkstationFormation> created = new ArrayList<>();
        for (Long operatorId : operatorIds) {
            Operator operator = operatorRepo.findById(operatorId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Operateur introuvable: " + operatorId));
            if (!onboardingService.isOnboardingComplete(operatorId)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "L'operateur " + operator.getEmployeeId() + " doit terminer tous les modules theorique avant la formation pratique");
            }
            // Double echec (formations + evaluations confondues) sur ce poste: plus de formation possible
            if (countTotalFailures(operatorId, workstationId) >= 2) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Double echec pour l'operateur " + operator.getEmployeeId()
                                + " sur ce poste: nouvelle formation bloquee (cas RH)");
            }
            if (formationRepo.existsByOperator_IdAndWorkstation_IdAndStatus(operatorId, workstationId, "IN_PROGRESS")) {
                continue;
            }
            WorkstationFormation formation = new WorkstationFormation();
            formation.setOperator(operator);
            formation.setWorkstation(workstation);
            formation.setStartDate(LocalDate.now());
            formation.setStatus("IN_PROGRESS");
            formation.setAchievedLevel("0");
            formation.setTargetLevel(workstation.getTargetIluLevel() != null ? workstation.getTargetIluLevel() : "3");
            formation.setQualityObjective(workstation.getQualityObjective());
            created.add(formationRepo.save(formation));
        }
        return created;
    }

    public List<Map<String, Object>> getAvailableStructure(String employeeId, Set<String> roles) {
        requireStarter(roles);
        if (roles.contains("CHEF_EQUIPE")) {
            return projectRepo.findAll().stream()
                    .map(this::toStructureMap)
                    .collect(Collectors.toList());
        }
        return projectRepo.findAll().stream()
                .filter(project -> teamRepo.findByProjectId(project.getId()).stream()
                        .anyMatch(t -> employeeId.equals(t.getTeamLeaderEmployeeId()) || employeeId.equals(t.getAgentQualiteEmployeeId())))
                .map(this::toStructureMap)
                .collect(Collectors.toList());
    }

    private Map<String, Object> toStructureMap(Project project) {
        Map<String, Object> projectMap = new LinkedHashMap<>();
        projectMap.put("id", project.getId());
        projectMap.put("name", project.getName());
        List<Map<String, Object>> zones = zoneRepo.findByProjectId(project.getId()).stream().map(zone -> {
            Map<String, Object> zoneMap = new LinkedHashMap<>();
            zoneMap.put("id", zone.getId());
            zoneMap.put("name", zone.getName());
            List<Map<String, Object>> workstations = workstationRepo.findByZoneId(zone.getId()).stream().map(workstation -> {
                Map<String, Object> workstationMap = new LinkedHashMap<>();
                workstationMap.put("id", workstation.getId());
                workstationMap.put("name", workstation.getName());
                workstationMap.put("targetCadence", workstation.getTargetCadence());
                return workstationMap;
            }).collect(Collectors.toList());
            zoneMap.put("workstations", workstations);
            return zoneMap;
        }).collect(Collectors.toList());
        projectMap.put("zones", zones);
        return projectMap;
    }

    @Transactional
    public List<FormationDetailsDto> listAllFormations(String employeeId, Set<String> roles) {
        List<FormationAssignment> activeAssignments = assignmentRepo.findAll().stream()
                .filter(a -> "IN_PROGRESS".equals(a.getStatus()) && a.getOperator() != null && a.getWorkstation() != null)
                .collect(Collectors.toList());
        for (FormationAssignment a : activeAssignments) {
            boolean exists = formationRepo.findByOperator_Id(a.getOperator().getId()).stream()
                    .anyMatch(f -> a.getWorkstation().getId().equals(f.getWorkstation().getId()) && "IN_PROGRESS".equals(f.getStatus()));
            if (!exists) {
                WorkstationFormation formation = new WorkstationFormation();
                formation.setOperator(a.getOperator());
                formation.setWorkstation(a.getWorkstation());
                formation.setStartDate(a.getStartDate() != null ? a.getStartDate() : LocalDate.now());
                formation.setStatus("IN_PROGRESS");
                formation.setAchievedLevel("0");
                formation.setTargetLevel("1");
                formation.setQualityObjective(7);
                formationRepo.save(formation);
            }
        }

        return formationRepo.findAll().stream()
                .filter(formation -> canViewFormation(formation, employeeId, roles))
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public FormationDetailsDto getFormationDetail(Long formationId) {
        return toDto(getFormation(formationId));
    }

    private FormationDetailsDto toDto(WorkstationFormation formation) {
        FormationDetailsDto dto = new FormationDetailsDto();
        dto.setId(formation.getId());
        if (formation.getOperator() != null) {
            dto.setOperatorId(formation.getOperator().getId());
            String lastName = formation.getOperator().getLastName() != null ? formation.getOperator().getLastName() : "";
            String firstName = formation.getOperator().getFirstName() != null ? formation.getOperator().getFirstName() : "";
            dto.setOperatorName((lastName + " " + firstName).trim());
            dto.setOperatorEmployeeId(formation.getOperator().getEmployeeId());
        }
        if (formation.getWorkstation() != null) {
            dto.setWorkstationId(formation.getWorkstation().getId());
            dto.setWorkstationName(formation.getWorkstation().getName());
            dto.setTargetCadence(formation.getWorkstation().getTargetCadence());
        }
        dto.setStartDate(formation.getStartDate());
        dto.setEndDate(formation.getEndDate());
        dto.setStatus(formation.getStatus());
        dto.setAchievedLevel(parseInt(formation.getAchievedLevel()));
        dto.setTargetLevel(parseInt(formation.getTargetLevel()));
        dto.setQualityObjective(formation.getQualityObjective() != null ? formation.getQualityObjective() : 7);

        List<DailyFormationTracking> days = trackingRepo.findByFormationIdOrderByTrackingDateAsc(formation.getId());
        int cadenceTotal = days.stream().filter(day -> day.getActualCadence() != null).mapToInt(DailyFormationTracking::getActualCadence).sum();
        long cadenceDays = days.stream().filter(day -> day.getActualCadence() != null).count();
        int defectsTotal = days.stream().filter(day -> day.getDefects() != null).mapToInt(DailyFormationTracking::getDefects).sum();
        long defectDays = days.stream().filter(day -> day.getDefects() != null).count();
        dto.setAverageCadence(cadenceDays == 0 ? null : Math.round((double) cadenceTotal / cadenceDays * 100.0) / 100.0);
        dto.setTotalDefects(defectsTotal);
        dto.setPassedCadence(dto.getTargetCadence() != null && dto.getAverageCadence() != null
                && dto.getAverageCadence() >= dto.getTargetCadence());
        dto.setPassedQuality(defectsTotal < dto.getQualityObjective());
        dto.setCadenceDaysCount((int) cadenceDays);
        dto.setDefectsDaysCount((int) defectDays);
        dto.setDaysWithData((int) Math.max(cadenceDays, defectDays));
        return dto;
    }

    public List<DailyFormationTracking> getFormationTracking(Long formationId) {
        getFormation(formationId);
        return trackingRepo.findByFormationIdOrderByTrackingDateAsc(formationId);
    }

    @Transactional
    public DailyFormationTracking addDailyTracking(Long formationId, DailyTrackingDto dto,
                                                    String employeeId, Set<String> roles) {
        WorkstationFormation formation = getFormation(formationId);
        validateWorkstationAccess(formation.getWorkstation(), employeeId, roles);
        if (!"IN_PROGRESS".equals(formation.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La formation est cloturee et ne peut plus etre modifiee");
        }
        validateTrackingInput(dto, roles);
        int dayNumber = dto.getDayNumber();
        DailyFormationTracking tracking = trackingRepo.findByFormationIdAndDayNumber(formationId, dayNumber)
                .orElseGet(() -> {
                    DailyFormationTracking newTracking = new DailyFormationTracking();
                    newTracking.setFormation(formation);
                    newTracking.setDayNumber(dayNumber);
                    newTracking.setTrackingDate(dto.getTrackingDate() != null ? dto.getTrackingDate()
                            : formation.getStartDate().plusDays(dayNumber - 1L));
                    return newTracking;
                });

        boolean chef = roles.contains("CHEF_EQUIPE") || roles.contains("ADMIN") || roles.contains("RH") || roles.contains("SUPERVISEUR");
        boolean quality = roles.contains("AGENT_QUALITE") || roles.contains("RESP_QUALITE") || roles.contains("ADMIN") || roles.contains("RH") || roles.contains("SUPERVISEUR");

        if (chef && dto.getActualCadence() != null) {
            tracking.setActualCadence(dto.getActualCadence());
            tracking.setCadenceSubmittedBy(employeeId);
        }
        if (quality && dto.getDefects() != null) {
            tracking.setDefects(dto.getDefects());
            tracking.setDefectsSubmittedBy(employeeId);
        }
        DailyFormationTracking saved = trackingRepo.save(tracking);
        evaluateFormation(formation);
        return saved;
    }

    @Transactional
    public List<DailyFormationTracking> batchSaveDaily(Long formationId, List<DailyTrackingDto> days,
                                                        String employeeId, Set<String> roles) {
        if (days == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Aucune donnee de suivi fournie");
        }
        List<DailyFormationTracking> saved = new ArrayList<>();
        for (DailyTrackingDto dto : days) {
            saved.add(addDailyTracking(formationId, dto, employeeId, roles));
        }
        return saved;
    }

    @Transactional
    public List<DailyFormationTracking> saveDailyBatch(List<DailyBatchEntryDto> entries,
                                                        String employeeId, Set<String> roles) {
        if (entries == null || entries.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Aucune saisie quotidienne fournie");
        }
        List<DailyFormationTracking> saved = new ArrayList<>();
        for (DailyBatchEntryDto entry : entries) {
            if (entry.getFormationId() == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Formation manquante dans la saisie");
            }
            saved.add(addDailyTracking(entry.getFormationId(), entry.toDailyTrackingDto(), employeeId, roles));
        }
        return saved;
    }

    @Transactional
    public Map<String, Object> autoEvaluate(Long formationId, Set<String> roles) {
        requireTrackingContributor(roles);
        WorkstationFormation formation = getFormation(formationId);
        if (!"IN_PROGRESS".equals(formation.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La formation n'est pas en cours");
        }
        if (!hasAllRequiredMeasurements(formationId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Les 12 jours doivent contenir une cadence et un nombre de defauts avant l'evaluation");
        }
        evaluateFormation(formation);
        return evaluationResult(formation);
    }

    public Map<String, Object> getChartData(Long formationId) {
        WorkstationFormation formation = getFormation(formationId);
        Integer targetCadence = formation.getWorkstation().getTargetCadence();
        Map<Integer, DailyFormationTracking> dayByNumber = trackingRepo.findByFormationIdOrderByTrackingDateAsc(formationId)
                .stream().filter(day -> day.getDayNumber() != null)
                .collect(Collectors.toMap(DailyFormationTracking::getDayNumber, day -> day, (first, ignored) -> first));
        List<String> labels = new ArrayList<>();
        List<Integer> targets = new ArrayList<>();
        List<Integer> cadence = new ArrayList<>();
        List<Integer> defects = new ArrayList<>();
        for (int dayNumber = 1; dayNumber <= 12; dayNumber++) {
            DailyFormationTracking day = dayByNumber.get(dayNumber);
            labels.add("J" + dayNumber);
            targets.add(targetCadence != null ? targetCadence : 0);
            cadence.add(day != null ? day.getActualCadence() : null);
            defects.add(day != null ? day.getDefects() : null);
        }
        Map<String, Object> targetDataset = new HashMap<>();
        targetDataset.put("label", "Cadence Objectif");
        targetDataset.put("data", targets);
        targetDataset.put("borderColor", "#f59e0b");
        targetDataset.put("borderDash", new int[] {5, 5});
        Map<String, Object> cadenceDataset = new HashMap<>();
        cadenceDataset.put("label", "Cadence Realisee");
        cadenceDataset.put("data", cadence);
        cadenceDataset.put("borderColor", "#10b981");
        cadenceDataset.put("fill", true);
        cadenceDataset.put("tension", 0.4);
        Map<String, Object> defectsDataset = new HashMap<>();
        defectsDataset.put("label", "Defauts");
        defectsDataset.put("data", defects);
        defectsDataset.put("borderColor", "#ef4444");
        defectsDataset.put("backgroundColor", "rgba(239,68,68,0.15)");
        defectsDataset.put("type", "bar");
        defectsDataset.put("yAxisID", "y1");
        Map<String, Object> result = new HashMap<>();
        result.put("labels", labels);
        result.put("targetCadence", targetCadence);
        result.put("targetCadenceDataset", targetDataset);
        result.put("achievedCadenceDataset", cadenceDataset);
        result.put("defectsDataset", defectsDataset);
        return result;
    }

    @Transactional
    public void evaluateFormation(WorkstationFormation formation) {
        if (!"IN_PROGRESS".equals(formation.getStatus()) || !hasAllRequiredMeasurements(formation.getId())) {
            return;
        }
        List<DailyFormationTracking> days = trackingRepo.findByFormationIdOrderByTrackingDateAsc(formation.getId());
        double averageCadence = days.stream().mapToInt(DailyFormationTracking::getActualCadence).average().orElse(0);
        int totalDefects = days.stream().mapToInt(DailyFormationTracking::getDefects).sum();
        Integer targetCadence = formation.getWorkstation().getTargetCadence();
        int qualityObjective = formation.getQualityObjective() != null ? formation.getQualityObjective() : 7;
        boolean completed = targetCadence != null && averageCadence >= targetCadence && totalDefects < qualityObjective;
        formation.setStatus(completed ? "COMPLETED" : "FAILED");
        formation.setEndDate(LocalDate.now());
        formationRepo.save(formation);
        if (completed && formation.getOperator().getOperatorType() == Operator.OperatorType.NOUVEAU_RECRU) {
            recyclageService.generateNewHirePlanning(formation.getOperator().getId());
        }
        if (!completed) {
            createSecondChanceFormationAfterTrainingFailure(formation);
        }
    }

    private void createSecondChanceFormationAfterTrainingFailure(WorkstationFormation formation) {
        // Un echec = formations FAILED + sessions FAILED/BLOCKED confondues sur ce poste
        long failures = countTotalFailures(formation.getOperator().getId(), formation.getWorkstation().getId());
        boolean retryAlreadyActive = formationRepo.findByOperator_Id(formation.getOperator().getId()).stream()
                .anyMatch(candidate -> formation.getWorkstation().getId().equals(candidate.getWorkstation().getId())
                        && "IN_PROGRESS".equals(candidate.getStatus()));
        if (failures != 1 || retryAlreadyActive) return;

        WorkstationFormation retry = new WorkstationFormation();
        retry.setOperator(formation.getOperator());
        retry.setWorkstation(formation.getWorkstation());
        retry.setStartDate(LocalDate.now());
        retry.setStatus("IN_PROGRESS");
        retry.setAchievedLevel("0");
        retry.setTargetLevel(formation.getTargetLevel());
        retry.setQualityObjective(formation.getQualityObjective());
        formationRepo.save(retry);
    }

    @Transactional
    public void resetFormation(Long formationId, String employeeId, Set<String> roles) {
        WorkstationFormation formation = getFormation(formationId);
        requireStarter(roles);
        validateWorkstationAccess(formation.getWorkstation(), employeeId, roles);
        trackingRepo.deleteByFormationId(formationId);
        formation.setStatus("IN_PROGRESS");
        formation.setEndDate(null);
        formation.setAchievedLevel("0");
        formationRepo.save(formation);
    }

    @Transactional
    public FormationAssignment assignOperator(Long operatorId, Long workstationId, Boolean primary) {
        Operator operator = operatorRepo.findById(operatorId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Operateur introuvable"));
        Workstation workstation = workstationRepo.findById(workstationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Poste de travail introuvable"));
        FormationAssignment assignment = new FormationAssignment();
        assignment.setOperator(operator);
        assignment.setWorkstation(workstation);
        assignment.setIsPrimaryAssignment(primary != null && primary);
        assignment.setStartDate(LocalDate.now());
        assignment.setStatus("IN_PROGRESS");
        FormationAssignment saved = assignmentRepo.save(assignment);

        boolean exists = formationRepo.findByOperator_Id(operatorId).stream()
                .anyMatch(f -> workstationId.equals(f.getWorkstation().getId()) && "IN_PROGRESS".equals(f.getStatus()));
        if (!exists) {
            WorkstationFormation formation = new WorkstationFormation();
            formation.setOperator(operator);
            formation.setWorkstation(workstation);
            formation.setStartDate(LocalDate.now());
            formation.setStatus("IN_PROGRESS");
            formation.setAchievedLevel("0");
            formation.setTargetLevel("1");
            formation.setQualityObjective(7);
            formationRepo.save(formation);
        }
        return saved;
    }

    @Transactional
    public Map<String, Object> updateQualityObjective(Long formationId, Integer qualityObjective) {
        WorkstationFormation formation = getFormation(formationId);
        formation.setQualityObjective(qualityObjective);
        formationRepo.save(formation);
        return Map.of("formationId", formationId, "qualityObjective", qualityObjective);
    }

    @Transactional
    public Map<String, Object> updateWorkstationQualityObjective(Long workstationId, Integer qualityObjective) {
        Workstation workstation = workstationRepo.findById(workstationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Poste de travail introuvable"));
        workstation.setQualityObjective(qualityObjective);
        workstationRepo.save(workstation);
        return Map.of("workstationId", workstationId, "qualityObjective", qualityObjective);
    }

    public FormationStatisticsDto getStatistics(String employeeId, Set<String> roles) {
        boolean isRestricted = !roles.contains("ADMIN") && !roles.contains("RH") && !roles.contains("CHEF_EQUIPE");
        final List<Long> myProjectIds = isRestricted
                ? teamRepo.findAll().stream()
                        .filter(t -> employeeId.equals(t.getTeamLeaderEmployeeId()) || employeeId.equals(t.getAgentQualiteEmployeeId()))
                        .filter(t -> t.getProject() != null)
                        .map(t -> t.getProject().getId())
                        .collect(Collectors.toList())
                : new java.util.ArrayList<>();

        FormationStatisticsDto stats = new FormationStatisticsDto();

        List<com.ilu.system.operator.entity.Operator> operators = operatorRepo.findAll();
        if (isRestricted) {
            final List<Long> pIds = myProjectIds;
            operators = operators.stream()
                    .filter(op -> op.getProject() != null && pIds.contains(op.getProject().getId()))
                    .collect(Collectors.toList());
        }
        stats.setTotalOperators((long) operators.size());

        List<WorkstationFormation> formations = formationRepo.findAll();
        if (isRestricted) {
            final List<Long> pIds = myProjectIds;
            formations = formations.stream()
                    .filter(f -> f.getOperator() != null && f.getOperator().getProject() != null && pIds.contains(f.getOperator().getProject().getId()))
                    .collect(Collectors.toList());
        }

        long inProgress = formations.stream().filter(f -> "IN_PROGRESS".equals(f.getStatus())).count();
        long completed = formations.stream().filter(f -> "COMPLETED".equals(f.getStatus())).count();
        long failed = formations.stream().filter(f -> "FAILED".equals(f.getStatus())).count();

        stats.setOperatorsInTraining(inProgress);
        stats.setOperatorsCertified(completed);

        java.util.Set<Long> withFormationIds = formations.stream()
                .map(f -> f.getOperator().getId())
                .collect(Collectors.toSet());
        long notStarted = operators.stream()
                .filter(op -> !withFormationIds.contains(op.getId()))
                .count();
        stats.setOperatorsNotStarted(notStarted);

        long workstationsCount = workstationRepo.findAll().stream()
                .filter(w -> !isRestricted || (w.getZone() != null && w.getZone().getProject() != null && myProjectIds.contains(w.getZone().getProject().getId())))
                .count();
        stats.setTotalWorkstations(workstationsCount);
        stats.setTotalTeams(teamRepo.count());

        stats.setStatusDistribution(List.of(
                new ChartDataDto("En Cours", inProgress),
                new ChartDataDto("Terminees", completed),
                new ChartDataDto("Echouees", failed)));
        return stats;
    }

    private WorkstationFormation getFormation(Long formationId) {
        return formationRepo.findById(formationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Formation introuvable"));
    }

    private void validateWorkstationAccess(Workstation workstation, String employeeId, Set<String> roles) {
        if (roles.contains("ADMIN") || roles.contains("SUPERVISEUR") || roles.contains("RH") ||
            roles.contains("AGENT_QUALITE") || roles.contains("RESP_QUALITE") || roles.contains("RESP_HSE") ||
            roles.contains("CHEF_EQUIPE")) {
            return;
        }

        Zone zone = workstation.getZone();
        if (zone == null || zone.getProject() == null) {
            return;
        }
        Long projId = zone.getProject().getId();
        boolean isAssociated = teamRepo.findAll().stream()
                .filter(t -> (employeeId != null && (employeeId.equalsIgnoreCase(t.getTeamLeaderEmployeeId()) || employeeId.equalsIgnoreCase(t.getAgentQualiteEmployeeId()))))
                .anyMatch(t -> (t.getProject() != null && t.getProject().getId().equals(projId)) ||
                               (t.getProjects() != null && t.getProjects().stream().anyMatch(p -> p.getId().equals(projId))));
        if (!isAssociated) {
            boolean hasAnyTeam = teamRepo.findAll().stream()
                .anyMatch(t -> employeeId != null && employeeId.equalsIgnoreCase(t.getTeamLeaderEmployeeId()));
            if (!hasAnyTeam) {
                return;
            }
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Le chef d'equipe n'est pas associe a ce projet");
        }
    }

    private void validateTrackingInput(DailyTrackingDto dto, Set<String> roles) {
        requireTrackingContributor(roles);
        if (dto == null || dto.getDayNumber() == null || dto.getDayNumber() < 1 || dto.getDayNumber() > 12) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Le jour de suivi doit etre compris entre 1 et 12");
        }
        if (dto.getActualCadence() != null && dto.getActualCadence() < 0
                || dto.getDefects() != null && dto.getDefects() < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La cadence et les defauts ne peuvent pas etre negatifs");
        }
    }

    private boolean hasAllRequiredMeasurements(Long formationId) {
        Map<Integer, DailyFormationTracking> days = trackingRepo.findByFormationIdOrderByTrackingDateAsc(formationId).stream()
                .filter(day -> day.getDayNumber() != null && day.getDayNumber() >= 1 && day.getDayNumber() <= 12)
                .collect(Collectors.toMap(DailyFormationTracking::getDayNumber, day -> day, (first, ignored) -> first));
        return days.size() == 12 && days.values().stream()
                .allMatch(day -> day.getActualCadence() != null && day.getDefects() != null);
    }

    private Map<String, Object> evaluationResult(WorkstationFormation formation) {
        List<DailyFormationTracking> days = trackingRepo.findByFormationIdOrderByTrackingDateAsc(formation.getId());
        double averageCadence = days.stream().mapToInt(DailyFormationTracking::getActualCadence).average().orElse(0);
        int totalDefects = days.stream().mapToInt(DailyFormationTracking::getDefects).sum();
        Integer targetCadence = formation.getWorkstation().getTargetCadence();
        int qualityObjective = formation.getQualityObjective() != null ? formation.getQualityObjective() : 7;
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("formationId", formation.getId());
        result.put("averageCadence", Math.round(averageCadence * 10.0) / 10.0);
        result.put("totalDefects", totalDefects);
        result.put("targetCadence", targetCadence);
        result.put("qualityObjective", qualityObjective);
        result.put("passedCadence", targetCadence != null && averageCadence >= targetCadence);
        result.put("passedQuality", totalDefects < qualityObjective);
        result.put("passed", "COMPLETED".equals(formation.getStatus()));
        result.put("newStatus", formation.getStatus());
        result.put("daysWithData", 12);
        return result;
    }

    private void requireStarter(Set<String> roles) {
        if (!roles.contains("CHEF_EQUIPE") && !roles.contains("AGENT_QUALITE") && !roles.contains("ADMIN")) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Seuls le chef d'equipe et l'agent qualite peuvent demarrer une formation");
        }
    }

    private void requireTrackingContributor(Set<String> roles) {
        if (!roles.contains("CHEF_EQUIPE") && !roles.contains("AGENT_QUALITE") && !roles.contains("ADMIN") && !roles.contains("RH")) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Role non autorise pour le suivi pratique");
        }
    }

    /**
     * Total des echecs d'un operateur sur un poste depuis sa derniere reussite:
     * formations 12j FAILED + sessions FAILED ou BLOCKED (partie commune incluse).
     * Une reussite remet le compteur a zero: seuls deux echecs consecutifs
     * declenchent le cas RH.
     */
    private long countTotalFailures(Long operatorId, Long workstationId) {
        if (workstationId == null) return 0;
        List<EvaluationSession> sessions = sessionRepo.findByOperatorIdOrderByCreatedAtDesc(operatorId);

        LocalDateTime since = sessions.stream()
                .filter(s -> s.getTemplate() != null && s.getTemplate().getWorkstation() != null
                        && workstationId.equals(s.getTemplate().getWorkstation().getId())
                        && s.getStatus() == EvaluationSession.SessionStatus.PASSED
                        && s.getCreatedAt() != null)
                .findFirst()
                .map(EvaluationSession::getCreatedAt)
                .orElse(LocalDateTime.MIN);

        long formationFailures = formationRepo.findByOperator_Id(operatorId).stream()
                .filter(f -> workstationId.equals(f.getWorkstation().getId()) && "FAILED".equals(f.getStatus()))
                .filter(f -> f.getEndDate() == null || !f.getEndDate().atStartOfDay().isBefore(since))
                .count();
        long sessionFailures = sessions.stream()
                .filter(s -> s.getTemplate() != null && s.getTemplate().getWorkstation() != null)
                .filter(s -> workstationId.equals(s.getTemplate().getWorkstation().getId()))
                .filter(s -> s.getStatus() == EvaluationSession.SessionStatus.FAILED
                        || s.getStatus() == EvaluationSession.SessionStatus.BLOCKED)
                .filter(s -> s.getCreatedAt() != null && !s.getCreatedAt().isBefore(since))
                .count();
        return formationFailures + sessionFailures;
    }

    private int parseInt(String value) {
        try {
            return Integer.parseInt(value);
        } catch (Exception ignored) {
            return 0;
        }
    }

    private boolean canViewFormation(WorkstationFormation formation, String employeeId, Set<String> roles) {
        if (formation == null) return false;
        if (roles.contains("ADMIN") || roles.contains("RESP_QUALITE") || roles.contains("SUPERVISEUR") || roles.contains("RH") || roles.contains("AGENT_QUALITE")) {
            return true;
        }
        if (roles.contains("RESP_HSE")) {
            return false;
        }
        if (roles.contains("CHEF_EQUIPE")) {
            if (formation.getOperator() == null) return true;
            if (formation.getOperator().getTeam() != null && employeeId != null) {
                String tlId = formation.getOperator().getTeam().getTeamLeaderEmployeeId();
                if (tlId != null && (tlId.equalsIgnoreCase(employeeId) || tlId.replace("-", "").equalsIgnoreCase(employeeId.replace("-", "")))) {
                    return true;
                }
            }
            return true;
        }
        return true;
    }
}