package com.ilu.system.operator.service;
import com.ilu.system.operator.dto.*;
import com.ilu.system.operator.entity.*;
import com.ilu.system.operator.repository.*;
import com.ilu.system.structure.entity.Workstation;
import com.ilu.system.structure.repository.WorkstationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
@Service
public class TrainingService {
    private final WorkstationFormationRepository formationRepo;
    private final FormationAssignmentRepository assignmentRepo;
    private final DailyFormationTrackingRepository trackingRepo;
    private final OperatorRepository operatorRepo;
    private final WorkstationRepository workstationRepo;
    private final TeamRepository teamRepo;
    public TrainingService(WorkstationFormationRepository formationRepo, FormationAssignmentRepository assignmentRepo, DailyFormationTrackingRepository trackingRepo, OperatorRepository operatorRepo, WorkstationRepository workstationRepo, TeamRepository teamRepo) {
        this.formationRepo = formationRepo; this.assignmentRepo = assignmentRepo; this.trackingRepo = trackingRepo;
        this.operatorRepo = operatorRepo; this.workstationRepo = workstationRepo; this.teamRepo = teamRepo;
    }
    @Transactional
    public WorkstationFormation createFormation(Long operatorId, Long workstationId, Integer targetLevel) {
        Operator operator = operatorRepo.findById(operatorId).orElseThrow(() -> new RuntimeException("Operator not found"));
        Workstation ws = workstationRepo.findById(workstationId).orElseThrow(() -> new RuntimeException("Workstation not found"));
        List<WorkstationFormation> existing = formationRepo.findActiveByOperatorAndWorkstation(operatorId, workstationId);
        if (!existing.isEmpty()) throw new RuntimeException("Operator already has an active formation for this workstation");
        WorkstationFormation f = new WorkstationFormation();
        f.setOperator(operator); f.setWorkstation(ws); f.setStartDate(LocalDate.now());
        f.setStatus("IN_PROGRESS"); f.setAchievedLevel(0);
        f.setTargetLevel(targetLevel != null ? targetLevel : ws.getTargetIluLevel());
        return formationRepo.save(f);
    }
    public List<FormationDetailsDto> listAllFormations() {
        return formationRepo.findAll().stream().map(f -> {
            FormationDetailsDto dto = new FormationDetailsDto();
            dto.setId(f.getId()); dto.setOperatorId(f.getOperator().getId());
            dto.setOperatorName(f.getOperator().getLastName() + " " + f.getOperator().getFirstName());
            dto.setWorkstationId(f.getWorkstation().getId()); dto.setWorkstationName(f.getWorkstation().getName());
            dto.setStartDate(f.getStartDate()); dto.setEndDate(f.getEndDate());
            dto.setStatus(f.getStatus()); dto.setAchievedLevel(f.getAchievedLevel()); dto.setTargetLevel(f.getTargetLevel());
            return dto;
        }).collect(Collectors.toList());
    }
    public List<DailyFormationTracking> getFormationTracking(Long formationId) {
        return trackingRepo.findByFormationIdOrderByTrackingDateAsc(formationId);
    }
    @Transactional
    public DailyFormationTracking addDailyTracking(Long formationId, DailyTrackingDto dto) {
        WorkstationFormation f = formationRepo.findById(formationId).orElseThrow(() -> new RuntimeException("Formation not found"));
        DailyFormationTracking t = new DailyFormationTracking();
        t.setFormation(f); t.setTrackingDate(dto.getTrackingDate()); t.setDailyLevel(dto.getDailyLevel());
        t.setObjectif(dto.getObjectif());
        t.setComment(dto.getComment()); t.setSupervisor(dto.getSupervisor());
        if (dto.getDailyLevel() != null && dto.getDailyLevel() > f.getAchievedLevel()) {
            f.setAchievedLevel(dto.getDailyLevel()); formationRepo.save(f);
        }
        return trackingRepo.save(t);
    }
    @Transactional
    public DailyFormationTracking addCadence(Long formationId, DailyTrackingDto dto) {
        WorkstationFormation f = formationRepo.findById(formationId).orElseThrow(() -> new RuntimeException("Formation not found"));
        // Check if tracking already exists for this date
        List<DailyFormationTracking> existing = trackingRepo.findByFormationIdAndTrackingDate(formationId, dto.getTrackingDate());
        DailyFormationTracking t;
        if (!existing.isEmpty()) {
            t = existing.get(0);
        } else {
            t = new DailyFormationTracking();
            t.setFormation(f); t.setTrackingDate(dto.getTrackingDate());
        }
        t.setCadence(dto.getCadence());
        t.setObjectif(dto.getObjectif());
        return trackingRepo.save(t);
    }
    @Transactional
    public DailyFormationTracking addDefauts(Long formationId, DailyTrackingDto dto) {
        WorkstationFormation f = formationRepo.findById(formationId).orElseThrow(() -> new RuntimeException("Formation not found"));
        List<DailyFormationTracking> existing = trackingRepo.findByFormationIdAndTrackingDate(formationId, dto.getTrackingDate());
        DailyFormationTracking t;
        if (!existing.isEmpty()) {
            t = existing.get(0);
        } else {
            t = new DailyFormationTracking();
            t.setFormation(f); t.setTrackingDate(dto.getTrackingDate());
        }
        t.setDefauts(dto.getDefauts());
        return trackingRepo.save(t);
    }
    @Transactional
    public void completeFormation(Long formationId) {
        WorkstationFormation f = formationRepo.findById(formationId).orElseThrow(() -> new RuntimeException("Formation not found"));
        f.setStatus("COMPLETED"); f.setEndDate(LocalDate.now()); formationRepo.save(f);
    }
    @Transactional
    public FormationAssignment assignOperator(Long operatorId, Long workstationId, Boolean isPrimary) {
        Operator operator = operatorRepo.findById(operatorId).orElseThrow(() -> new RuntimeException("Operator not found"));
        Workstation ws = workstationRepo.findById(workstationId).orElseThrow(() -> new RuntimeException("Workstation not found"));
        FormationAssignment a = new FormationAssignment();
        a.setOperator(operator); a.setWorkstation(ws); a.setIsPrimaryAssignment(isPrimary != null ? isPrimary : false);
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
        dist.add(new ChartDataDto("In Progress", stats.getOperatorsInTraining()));
        dist.add(new ChartDataDto("Completed", stats.getOperatorsCertified()));
        dist.add(new ChartDataDto("Planned", formationRepo.countByStatus("PLANNED")));
        stats.setStatusDistribution(dist);
        return stats;
    }
}