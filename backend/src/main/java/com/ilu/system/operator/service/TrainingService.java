package com.ilu.system.operator.service;
import com.ilu.system.operator.dto.*;
import com.ilu.system.operator.entity.*;
import com.ilu.system.operator.repository.*;
import com.ilu.system.structure.entity.Workstation;
import com.ilu.system.structure.repository.WorkstationRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
@Service
public class TrainingService {
    private final WorkstationFormationRepository formationRepo;
    private final FormationAssignmentRepository assignmentRepo;
    private final DailyFormationTrackingRepository trackingRepo;
    private final OperatorRepository operatorRepo;
    private final WorkstationRepository workstationRepo;
    private final TeamRepository teamRepo;
    public TrainingService(WorkstationFormationRepository formationRepo, FormationAssignmentRepository assignmentRepo,
                           DailyFormationTrackingRepository trackingRepo, OperatorRepository operatorRepo,
                           WorkstationRepository workstationRepo, TeamRepository teamRepo) {
        this.formationRepo = formationRepo; this.assignmentRepo = assignmentRepo;
        this.trackingRepo = trackingRepo; this.operatorRepo = operatorRepo;
        this.workstationRepo = workstationRepo; this.teamRepo = teamRepo;
    }

    @Transactional
    public List<WorkstationFormation> createFormations(Long workstationId, List<Long> operatorIds) {
        Workstation ws = workstationRepo.findById(workstationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Workstation non trouve"));
        List<WorkstationFormation> created = new ArrayList<>();
        for (Long opId : operatorIds) {
            List<WorkstationFormation> existing = formationRepo.findActiveByOperatorAndWorkstation(opId, workstationId);
            if (!existing.isEmpty()) continue;
            Operator operator = operatorRepo.findById(opId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Operateur non trouve: " + opId));
            WorkstationFormation f = new WorkstationFormation();
            f.setOperator(operator); f.setWorkstation(ws); f.setStartDate(LocalDate.now());
            f.setStatus("IN_PROGRESS"); f.setAchievedLevel(0);
            f.setTargetLevel(ws.getTargetIluLevel() != null ? ws.getTargetIluLevel() : 3);
            created.add(formationRepo.save(f));
        }
        return created;
    }

    public List<FormationDetailsDto> listAllFormations() {
        return formationRepo.findAll().stream().map(this::toDto).collect(Collectors.toList());
    }

    public FormationDetailsDto getFormationDetail(Long formationId) {
        WorkstationFormation f = formationRepo.findById(formationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Formation non trouve"));
        return toDto(f);
    }

    private FormationDetailsDto toDto(WorkstationFormation f) {
        FormationDetailsDto dto = new FormationDetailsDto();
        dto.setId(f.getId());
        dto.setOperatorId(f.getOperator().getId());
        dto.setOperatorName(f.getOperator().getLastName() + " " + f.getOperator().getFirstName());
        dto.setOperatorEmployeeId(f.getOperator().getEmployeeId());
        dto.setWorkstationId(f.getWorkstation().getId());
        dto.setWorkstationName(f.getWorkstation().getName());
        dto.setStartDate(f.getStartDate()); dto.setEndDate(f.getEndDate());
        dto.setStatus(f.getStatus());
        dto.setAchievedLevel(f.getAchievedLevel()); dto.setTargetLevel(f.getTargetLevel());
        dto.setTargetCadence(f.getWorkstation().getTargetCadence());
        dto.setQualityObjective(f.getWorkstation().getQualityObjective() != null ? f.getWorkstation().getQualityObjective() : 7);
        List<DailyFormationTracking> days = trackingRepo.findByFormationIdOrderByTrackingDateAsc(f.getId());
        if (!days.isEmpty()) {
            int totalCadence = 0; int totalDefects = 0; int count = 0;
            for (DailyFormationTracking d : days) {
                if (d.getActualCadence() != null) { totalCadence += d.getActualCadence(); count++; }
                if (d.getDefects() != null) totalDefects += d.getDefects();
            }
            dto.setAverageCadence(count > 0 ? Math.round(((double) totalCadence / count) * 100.0) / 100.0 : null);
            dto.setTotalDefects(totalDefects);
            Integer targetCad = dto.getTargetCadence();
            dto.setPassedCadence(targetCad != null && dto.getAverageCadence() != null && dto.getAverageCadence() >= targetCad);
            dto.setPassedQuality(totalDefects < dto.getQualityObjective());
            dto.setDaysWithData(count);
        } else {
            dto.setDaysWithData(0);
        }
        return dto;
    }

    public List<DailyFormationTracking> getFormationTracking(Long formationId) {
        return trackingRepo.findByFormationIdOrderByTrackingDateAsc(formationId);
    }

    /**
     * Add daily tracking. Supports upsert by dayNumber.
     */
    @Transactional
    public DailyFormationTracking addDailyTracking(Long formationId, DailyTrackingDto dto) {
        WorkstationFormation f = formationRepo.findById(formationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Formation non trouve"));

        // Check if entry exists for this day
        List<DailyFormationTracking> existing = trackingRepo.findByFormationIdOrderByTrackingDateAsc(formationId);
        DailyFormationTracking existingDay = existing.stream()
                .filter(d -> d.getDayNumber() != null && d.getDayNumber().equals(dto.getDayNumber()))
                .findFirst().orElse(null);

        DailyFormationTracking t;
        if (existingDay != null) {
            // Update existing
            t = existingDay;
            if (dto.getActualCadence() != null) t.setActualCadence(dto.getActualCadence());
            if (dto.getDefects() != null) t.setDefects(dto.getDefects());
            if (dto.getComment() != null) t.setComment(dto.getComment());
        } else {
            // Create new
            int nextDay = dto.getDayNumber() != null ? dto.getDayNumber() : (existing.size() + 1);
            t = new DailyFormationTracking();
            t.setFormation(f);
            t.setTrackingDate(dto.getTrackingDate() != null ? dto.getTrackingDate() : LocalDate.now());
            t.setDayNumber(nextDay);
            t.setActualCadence(dto.getActualCadence());
            t.setDefects(dto.getDefects());
            t.setDailyLevel(dto.getDailyLevel());
            t.setComment(dto.getComment());
            t.setSupervisor(dto.getSupervisor());
        }
        t = trackingRepo.save(t);

        // Auto-evaluate when 12 days are entered
        long count = trackingRepo.findByFormationIdOrderByTrackingDateAsc(formationId).stream()
                .filter(d -> d.getDayNumber() != null).count();
        if (count >= 12) {
            evaluateFormation(f);
        }
        return t;
    }

    /**
     * Batch save/update all 12 days at once. Each entry is upserted by dayNumber.
     */
    @Transactional
    public List<DailyFormationTracking> batchSaveDaily(Long formationId, List<DailyTrackingDto> days) {
        WorkstationFormation f = formationRepo.findById(formationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Formation non trouve"));
        List<DailyFormationTracking> results = new ArrayList<>();
        for (DailyTrackingDto dto : days) {
            if (dto.getDayNumber() == null || dto.getDayNumber() < 1 || dto.getDayNumber() > 12) continue;
            results.add(addDailyTracking(formationId, dto));
        }
        return results;
    }

    /**
     * Auto-evaluate: compares average cadence vs targetCadence, total defects vs qualityObjective.
     * Sets status to COMPLETED (pass) or FAILED (fail).
     * Returns evaluation result map with all details.
     */
    @Transactional
    public Map<String, Object> autoEvaluate(Long formationId) {
        WorkstationFormation f = formationRepo.findById(formationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Formation non trouve"));
        if (!"IN_PROGRESS".equals(f.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Formation is not in progress. Current: " + f.getStatus());
        }
        List<DailyFormationTracking> days = trackingRepo.findByFormationIdOrderByTrackingDateAsc(formationId);
        int totalCadence = 0; int totalDefects = 0; int count = 0;
        for (DailyFormationTracking d : days) {
            if (d.getActualCadence() != null) { totalCadence += d.getActualCadence(); count++; }
            if (d.getDefects() != null) totalDefects += d.getDefects();
        }
        double avgCadence = count > 0 ? (double) totalCadence / count : 0;
        Integer targetCad = f.getWorkstation().getTargetCadence();
        int qualityObj = f.getWorkstation().getQualityObjective() != null ? f.getWorkstation().getQualityObjective() : 7;
        boolean passedCadence = targetCad != null && avgCadence >= targetCad;
        boolean passedQuality = totalDefects < qualityObj;
        boolean passed = passedCadence && passedQuality;

        f.setStatus(passed ? "COMPLETED" : "FAILED");
        f.setEndDate(LocalDate.now());
        formationRepo.save(f);

        Map<String, Object> result = new HashMap<>();
        result.put("formationId", formationId);
        result.put("operatorName", f.getOperator().getLastName() + " " + f.getOperator().getFirstName());
        result.put("workstationName", f.getWorkstation().getName());
        result.put("targetCadence", targetCad);
        result.put("averageCadence", Math.round(avgCadence * 10.0) / 10.0);
        result.put("totalDefects", totalDefects);
        result.put("qualityObjective", qualityObj);
        result.put("passedCadence", passedCadence);
        result.put("passedQuality", passedQuality);
        result.put("passed", passed);
        result.put("newStatus", f.getStatus());
        result.put("daysWithData", count);
        return result;
    }

    /**
     * Get chart data: cadence per day + target line + defects.
     */
    public Map<String, Object> getChartData(Long formationId) {
        WorkstationFormation f = formationRepo.findById(formationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Formation non trouve"));
        Integer targetCad = f.getWorkstation().getTargetCadence();
        List<DailyFormationTracking> days = trackingRepo.findByFormationIdOrderByTrackingDateAsc(formationId);

        List<String> labels = new ArrayList<>();
        List<Integer> targetData = new ArrayList<>();
        List<Integer> cadenceData = new ArrayList<>();
        List<Integer> defectsData = new ArrayList<>();

        for (int i = 1; i <= 12; i++) {
            labels.add("J" + i);
            targetData.add(targetCad);
            final int dayNum = i;
            DailyFormationTracking d = days.stream().filter(dd -> dd.getDayNumber() != null && dd.getDayNumber() == dayNum).findFirst().orElse(null);
            cadenceData.add(d != null ? d.getActualCadence() : null);
            defectsData.add(d != null && d.getDefects() != null ? d.getDefects() : 0);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("labels", labels);
        result.put("targetCadence", targetCad);

        Map<String, Object> targetDataset = new HashMap<>();
        targetDataset.put("label", "Cadence Objectif");
        targetDataset.put("data", targetData);
        targetDataset.put("borderColor", "#f59e0b");
        targetDataset.put("backgroundColor", "rgba(245,158,11,0.1)");
        targetDataset.put("borderWidth", 2);
        targetDataset.put("borderDash", new int[]{5, 5});

        Map<String, Object> cadenceDataset = new HashMap<>();
        cadenceDataset.put("label", "Cadence Realisee");
        cadenceDataset.put("data", cadenceData);
        cadenceDataset.put("borderColor", "#10b981");
        cadenceDataset.put("backgroundColor", "rgba(16,185,129,0.1)");
        cadenceDataset.put("borderWidth", 3);
        cadenceDataset.put("fill", true);
        cadenceDataset.put("tension", 0.4);

        Map<String, Object> defectsDataset = new HashMap<>();
        defectsDataset.put("label", "Defauts");
        defectsDataset.put("data", defectsData);
        defectsDataset.put("borderColor", "#ef4444");
        defectsDataset.put("backgroundColor", "rgba(239,68,68,0.15)");
        defectsDataset.put("type", "bar");
        defectsDataset.put("yAxisID", "y1");

        result.put("targetCadenceDataset", targetDataset);
        result.put("achievedCadenceDataset", cadenceDataset);
        result.put("defectsDataset", defectsDataset);
        return result;
    }

    @Transactional
    public void evaluateFormation(WorkstationFormation f) {
        List<DailyFormationTracking> days = trackingRepo.findByFormationIdOrderByTrackingDateAsc(f.getId());
        if (days.size() < 12) return;
        int totalCadence = 0; int totalDefects = 0; int count = 0;
        for (DailyFormationTracking d : days) {
            if (d.getActualCadence() != null) { totalCadence += d.getActualCadence(); count++; }
            if (d.getDefects() != null) totalDefects += d.getDefects();
        }
        double avgCadence = count > 0 ? (double) totalCadence / count : 0;
        Integer targetCad = f.getWorkstation().getTargetCadence();
        int qualityObj = f.getWorkstation().getQualityObjective() != null ? f.getWorkstation().getQualityObjective() : 7;
        boolean passedCadence = targetCad != null && avgCadence >= targetCad;
        boolean passedQuality = totalDefects < qualityObj;
        if (passedCadence && passedQuality) {
            f.setStatus("COMPLETED"); f.setEndDate(LocalDate.now());
        } else {
            f.setStatus("FAILED"); f.setEndDate(LocalDate.now());
        }
        formationRepo.save(f);
    }

    @Transactional
    public void resetFormation(Long formationId) {
        WorkstationFormation f = formationRepo.findById(formationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Formation non trouve"));
        trackingRepo.deleteByFormationId(formationId);
        f.setStatus("IN_PROGRESS"); f.setEndDate(null); f.setAchievedLevel(0);
        formationRepo.save(f);
    }

    @Transactional
    public FormationAssignment assignOperator(Long operatorId, Long workstationId, Boolean isPrimary) {
        Operator operator = operatorRepo.findById(operatorId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Operateur non trouve"));
        Workstation ws = workstationRepo.findById(workstationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Workstation non trouve"));
        FormationAssignment a = new FormationAssignment();
        a.setOperator(operator); a.setWorkstation(ws);
        a.setIsPrimaryAssignment(isPrimary != null ? isPrimary : false);
        a.setStartDate(LocalDate.now()); a.setStatus("IN_PROGRESS");
        return assignmentRepo.save(a);
    }

    public FormationStatisticsDto getStatistics() {
        FormationStatisticsDto stats = new FormationStatisticsDto();
        stats.setTotalOperators(operatorRepo.count());
        stats.setOperatorsInTraining(formationRepo.countByStatus("IN_PROGRESS"));
        stats.setOperatorsCertified(formationRepo.countByStatus("COMPLETED"));
        long withFormation = formationRepo.findAll().stream().map(f -> f.getOperator().getId()).distinct().count();
        stats.setOperatorsNotStarted(stats.getTotalOperators() - withFormation);
        stats.setTotalWorkstations(workstationRepo.count());
        stats.setTotalTeams(teamRepo.count());
        List<ChartDataDto> dist = new ArrayList<>();
        dist.add(new ChartDataDto("En Cours", stats.getOperatorsInTraining()));
        dist.add(new ChartDataDto("Terminees", stats.getOperatorsCertified()));
        dist.add(new ChartDataDto("Echouees", formationRepo.countByStatus("FAILED")));
        stats.setStatusDistribution(dist);
        return stats;
    }
}
