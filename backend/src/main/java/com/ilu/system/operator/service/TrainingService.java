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
import com.ilu.system.structure.repository.ProjectMemberRepository;
import com.ilu.system.structure.repository.ProjectRepository;
import com.ilu.system.structure.repository.WorkstationRepository;
import com.ilu.system.structure.repository.ZoneRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
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
    private final ProjectMemberRepository projectMemberRepo;

    public TrainingService(WorkstationFormationRepository formationRepo, FormationAssignmentRepository assignmentRepo,
                           DailyFormationTrackingRepository trackingRepo, OperatorRepository operatorRepo,
                           WorkstationRepository workstationRepo, TeamRepository teamRepo,
                           OnboardingService onboardingService, ProjectRepository projectRepo,
                           ZoneRepository zoneRepo, ProjectMemberRepository projectMemberRepo) {
        this.formationRepo = formationRepo;
        this.assignmentRepo = assignmentRepo;
        this.trackingRepo = trackingRepo;
        this.operatorRepo = operatorRepo;
        this.workstationRepo = workstationRepo;
        this.teamRepo = teamRepo;
        this.onboardingService = onboardingService;
        this.projectRepo = projectRepo;
        this.zoneRepo = zoneRepo;
        this.projectMemberRepo = projectMemberRepo;
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
            if (!formationRepo.findActiveByOperatorAndWorkstation(operatorId, workstationId).isEmpty()) {
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
        return projectRepo.findAll().stream()
                .filter(project -> projectMemberRepo.existsByProjectIdAndEmployeeId(project.getId(), employeeId))
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

    public List<FormationDetailsDto> listAllFormations(String employeeId, Set<String> roles) {
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
        dto.setOperatorId(formation.getOperator().getId());
        dto.setOperatorName(formation.getOperator().getLastName() + " " + formation.getOperator().getFirstName());
        dto.setOperatorEmployeeId(formation.getOperator().getEmployeeId());
        dto.setWorkstationId(formation.getWorkstation().getId());
        dto.setWorkstationName(formation.getWorkstation().getName());
        dto.setStartDate(formation.getStartDate());
        dto.setEndDate(formation.getEndDate());
        dto.setStatus(formation.getStatus());
        dto.setAchievedLevel(parseInt(formation.getAchievedLevel()));
        dto.setTargetLevel(parseInt(formation.getTargetLevel()));
        dto.setTargetCadence(formation.getWorkstation().getTargetCadence());
        dto.setQualityObjective(formation.getQualityObjective() != null ? formation.getQualityObjective() : 7);

        List<DailyFormationTracking> days = trackingRepo.findByFormationIdOrderByTrackingDateAsc(formation.getId());
        int cadenceTotal = days.stream().filter(day -> day.getActualCadence() != null).mapToInt(DailyFormationTracking::getActualCadence).sum();
        long cadenceDays = days.stream().filter(day -> day.getActualCadence() != null).count();
        int defectsTotal = days.stream().filter(day -> day.getDefects() != null).mapToInt(DailyFormationTracking::getDefects).sum();
        dto.setAverageCadence(cadenceDays == 0 ? null : Math.round((double) cadenceTotal / cadenceDays * 100.0) / 100.0);
        dto.setTotalDefects(defectsTotal);
        dto.setPassedCadence(dto.getTargetCadence() != null && dto.getAverageCadence() != null
                && dto.getAverageCadence() >= dto.getTargetCadence());
        dto.setPassedQuality(defectsTotal < dto.getQualityObjective());
        dto.setDaysWithData((int) days.stream()
                .filter(day -> day.getActualCadence() != null && day.getDefects() != null).count());
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
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La formation n'est plus en cours");
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

        if (dto.getActualCadence() != null) {
            tracking.setActualCadence(dto.getActualCadence());
            tracking.setCadenceSubmittedBy(employeeId);
        }
        if (dto.getDefects() != null) {
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
        formation.setStatus(targetCadence != null && averageCadence >= targetCadence && totalDefects < qualityObjective
                ? "COMPLETED" : "FAILED");
        formation.setEndDate(LocalDate.now());
        formationRepo.save(formation);
    }

    @Transactional
    public void resetFormation(Long formationId) {
        WorkstationFormation formation = getFormation(formationId);
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
        return assignmentRepo.save(assignment);
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

    public FormationStatisticsDto getStatistics() {
        FormationStatisticsDto stats = new FormationStatisticsDto();
        stats.setTotalOperators(operatorRepo.count());
        stats.setOperatorsInTraining(formationRepo.countByStatus("IN_PROGRESS"));
        stats.setOperatorsCertified(formationRepo.countByStatus("COMPLETED"));
        long withFormation = formationRepo.findAll().stream().map(formation -> formation.getOperator().getId()).distinct().count();
        stats.setOperatorsNotStarted(stats.getTotalOperators() - withFormation);
        stats.setTotalWorkstations(workstationRepo.count());
        stats.setTotalTeams(teamRepo.count());
        stats.setStatusDistribution(List.of(
                new ChartDataDto("En Cours", stats.getOperatorsInTraining()),
                new ChartDataDto("Terminees", stats.getOperatorsCertified()),
                new ChartDataDto("Echouees", formationRepo.countByStatus("FAILED"))));
        return stats;
    }

    private WorkstationFormation getFormation(Long formationId) {
        return formationRepo.findById(formationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Formation introuvable"));
    }

    private void validateWorkstationAccess(Workstation workstation, String employeeId, Set<String> roles) {
        if (roles.contains("AGENT_QUALITE")) {
            return;
        }

        Zone zone = workstation.getZone();
        if (zone == null || zone.getProject() == null
                || !projectMemberRepo.existsByProjectIdAndEmployeeId(zone.getProject().getId(), employeeId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Le chef d'equipe n'est pas associe a ce projet");
        }
    }

    private void validateTrackingInput(DailyTrackingDto dto, Set<String> roles) {
        requireTrackingContributor(roles);
        if (dto == null || dto.getDayNumber() == null || dto.getDayNumber() < 1 || dto.getDayNumber() > 12) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Le jour de suivi doit etre compris entre 1 et 12");
        }
        boolean chef = roles.contains("CHEF_EQUIPE");
        boolean quality = roles.contains("AGENT_QUALITE");
        if (dto.getActualCadence() == null && dto.getDefects() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Une cadence ou un nombre de defauts est requis");
        }
        if (dto.getActualCadence() != null && !chef) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Seul le chef d'equipe peut saisir la cadence");
        }
        if (dto.getDefects() != null && !quality) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Seul l'agent qualite peut saisir les defauts");
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
        if (!roles.contains("CHEF_EQUIPE") && !roles.contains("AGENT_QUALITE")) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Seuls le chef d'equipe et l'agent qualite peuvent demarrer une formation");
        }
    }

    private void requireTrackingContributor(Set<String> roles) {
        if (!roles.contains("CHEF_EQUIPE") && !roles.contains("AGENT_QUALITE")) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Role non autorise pour le suivi pratique");
        }
    }

    private int parseInt(String value) {
        try {
            return Integer.parseInt(value);
        } catch (Exception ignored) {
            return 0;
        }
    }

    private boolean canViewFormation(WorkstationFormation formation, String employeeId, Set<String> roles) {
        if (roles.contains("ADMIN") || roles.contains("RESP_QUALITE")) {
            return true;
        }
        Zone zone = formation.getWorkstation().getZone();
        return zone != null && zone.getProject() != null
                && projectMemberRepo.existsByProjectIdAndEmployeeId(zone.getProject().getId(), employeeId);
    }
}
