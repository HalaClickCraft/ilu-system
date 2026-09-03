package com.ilu.system.operator.service;

import com.ilu.system.operator.dto.*;
import com.ilu.system.operator.entity.*;
import com.ilu.system.operator.repository.*;
import com.ilu.system.structure.repository.ProjectRepository;
import com.ilu.system.structure.repository.ZoneRepository;
import com.ilu.system.structure.repository.WorkstationRepository;
import com.ilu.system.structure.entity.Workstation;
import com.ilu.system.recyclage.repository.RecyclagePlanningRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.List;
import com.ilu.system.evaluation.repository.EvaluationSessionRepository;
import com.ilu.system.evaluation.entity.EvaluationSession;
import java.util.stream.Collectors;

import com.ilu.system.absence.repository.AbsenceRepository;
import com.ilu.system.evaluation.repository.EvaluationAnswerRepository;
import com.ilu.system.notification.repository.NotificationRepository;

@Service
public class OperatorService {
    private final OperatorRepository operatorRepository;
    private final TeamRepository teamRepository;
    private final ProjectRepository projectRepository;
    private final ZoneRepository zoneRepository;
    private final WorkstationRepository workstationRepository;
    private final WorkstationFormationRepository workstationFormationRepository;
    private final FormationAssignmentRepository formationAssignmentRepository;
    private final RecyclagePlanningRepository recyclagePlanningRepository;
    private final EvaluationSessionRepository evaluationSessionRepository;
    private final EvaluationAnswerRepository evaluationAnswerRepository;
    private final DailyFormationTrackingRepository dailyFormationTrackingRepository;
    private final AbsenceRepository absenceRepository;
    private final ProjectTransferRequestRepository projectTransferRequestRepository;
    private final OperatorOnboardingRepository operatorOnboardingRepository;
    private final NotificationRepository notificationRepository;

    public OperatorService(OperatorRepository operatorRepository, TeamRepository teamRepository,
                           ProjectRepository projectRepository, ZoneRepository zoneRepository,
                           WorkstationRepository workstationRepository,
                           WorkstationFormationRepository workstationFormationRepository,
                           FormationAssignmentRepository formationAssignmentRepository,
                           RecyclagePlanningRepository recyclagePlanningRepository) {
        this(operatorRepository, teamRepository, projectRepository, zoneRepository, workstationRepository,
             workstationFormationRepository, formationAssignmentRepository, recyclagePlanningRepository,
             null, null, null, null, null, null, null);
    }

    @org.springframework.beans.factory.annotation.Autowired
    public OperatorService(OperatorRepository operatorRepository, TeamRepository teamRepository,
                           ProjectRepository projectRepository, ZoneRepository zoneRepository,
                           WorkstationRepository workstationRepository,
                           WorkstationFormationRepository workstationFormationRepository,
                           FormationAssignmentRepository formationAssignmentRepository,
                           RecyclagePlanningRepository recyclagePlanningRepository,
                           @org.springframework.beans.factory.annotation.Autowired(required = false) EvaluationSessionRepository evaluationSessionRepository,
                           @org.springframework.beans.factory.annotation.Autowired(required = false) EvaluationAnswerRepository evaluationAnswerRepository,
                           @org.springframework.beans.factory.annotation.Autowired(required = false) DailyFormationTrackingRepository dailyFormationTrackingRepository,
                           @org.springframework.beans.factory.annotation.Autowired(required = false) AbsenceRepository absenceRepository,
                           @org.springframework.beans.factory.annotation.Autowired(required = false) ProjectTransferRequestRepository projectTransferRequestRepository,
                           @org.springframework.beans.factory.annotation.Autowired(required = false) OperatorOnboardingRepository operatorOnboardingRepository,
                           @org.springframework.beans.factory.annotation.Autowired(required = false) NotificationRepository notificationRepository) {
        this.operatorRepository = operatorRepository;
        this.teamRepository = teamRepository;
        this.projectRepository = projectRepository;
        this.zoneRepository = zoneRepository;
        this.workstationRepository = workstationRepository;
        this.workstationFormationRepository = workstationFormationRepository;
        this.formationAssignmentRepository = formationAssignmentRepository;
        this.recyclagePlanningRepository = recyclagePlanningRepository;
        this.evaluationSessionRepository = evaluationSessionRepository;
        this.evaluationAnswerRepository = evaluationAnswerRepository;
        this.dailyFormationTrackingRepository = dailyFormationTrackingRepository;
        this.absenceRepository = absenceRepository;
        this.projectTransferRequestRepository = projectTransferRequestRepository;
        this.operatorOnboardingRepository = operatorOnboardingRepository;
        this.notificationRepository = notificationRepository;
    }

    @Transactional
    public Operator createOperator(CreateOperatorRequest request) {
        if (operatorRepository.existsByEmployeeId(request.getEmployeeId()))
            throw new RuntimeException("Un operateur avec le matricule '" + request.getEmployeeId() + "' existe deja");
        Operator op = new Operator();
        op.setEmployeeId(request.getEmployeeId());
        op.setLastName(request.getLastName());
        op.setFirstName(request.getFirstName());
        op.setRole(request.getRole());
        if (request.getHireDate() != null && !request.getHireDate().isBlank())
            op.setHireDate(LocalDate.parse(request.getHireDate()));
        if (request.getExitDate() != null && !request.getExitDate().isBlank())
            op.setExitDate(LocalDate.parse(request.getExitDate()));
        String typeValue = request.getOperatorType();
        if (typeValue == null || typeValue.isBlank()) {
            typeValue = "NOUVEAU_RECRU";
        }
        try {
            op.setOperatorType(Operator.OperatorType.valueOf(typeValue));
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Type operateur invalide: " + typeValue);
        }
        op.setActive(true);
        if (request.getTeamId() != null)
            op.setTeam(teamRepository.findById(request.getTeamId()).orElseThrow(() -> new RuntimeException("Equipe non trouvee")));
        if (request.getProjectId() != null)
            op.setProject(projectRepository.findById(request.getProjectId()).orElseThrow(() -> new RuntimeException("Projet non trouve")));
        if (request.getZoneId() != null)
            op.setZone(zoneRepository.findById(request.getZoneId()).orElseThrow(() -> new RuntimeException("Zone non trouvee")));
        Operator savedOp = operatorRepository.save(op);

        if (request.getWorkstationId() != null) {
            workstationRepository.findById(request.getWorkstationId()).ifPresent(ws -> {
                boolean exists = workstationFormationRepository.findByOperator_Id(savedOp.getId()).stream()
                        .anyMatch(f -> ws.getId().equals(f.getWorkstation().getId()) && "IN_PROGRESS".equals(f.getStatus()));
                if (!exists) {
                    WorkstationFormation formation = new WorkstationFormation();
                    formation.setOperator(savedOp);
                    formation.setWorkstation(ws);
                    formation.setStartDate(LocalDate.now());
                    formation.setStatus("IN_PROGRESS");
                    formation.setAchievedLevel("0");
                    formation.setTargetLevel("1");
                    formation.setQualityObjective(7);
                    workstationFormationRepository.save(formation);

                    FormationAssignment assignment = new FormationAssignment();
                    assignment.setOperator(savedOp);
                    assignment.setWorkstation(ws);
                    assignment.setIsPrimaryAssignment(true);
                    assignment.setStartDate(LocalDate.now());
                    assignment.setStatus("IN_PROGRESS");
                    formationAssignmentRepository.save(assignment);
                }
            });
        }
        return savedOp;
    }

    @Transactional
    public List<Operator> createOperatorsBatch(List<CreateOperatorRequest> requests) {
        return requests.stream().map(this::createOperator).collect(Collectors.toList());
    }

    public List<Operator> listAll() { return operatorRepository.findAll(); }
    public List<Operator> listActive() { return operatorRepository.findByActiveTrue(); }
    public Operator findByEmployeeId(String employeeId) { return operatorRepository.findByEmployeeId(employeeId).orElseThrow(() -> new RuntimeException("Operateur non trouve")); }
    public Operator findById(Long id) { return operatorRepository.findById(id).orElseThrow(() -> new RuntimeException("Operateur non trouve")); }

    @Transactional
    public Operator updateOperator(Long id, CreateOperatorRequest request) {
        Operator op = findById(id);
        if (request.getLastName() != null) op.setLastName(request.getLastName());
        if (request.getFirstName() != null) op.setFirstName(request.getFirstName());
        if (request.getRole() != null) op.setRole(request.getRole());
        if (request.getHireDate() != null && !request.getHireDate().isBlank())
            op.setHireDate(LocalDate.parse(request.getHireDate()));
        // FIX 4a: Handle exitDate save
        if (request.getExitDate() != null && !request.getExitDate().isBlank()) {
            op.setExitDate(LocalDate.parse(request.getExitDate()));
        } else if (request.getExitDate() != null && request.getExitDate().isBlank()) {
            op.setExitDate(null);
        }
        if (request.getOperatorType() != null && !request.getOperatorType().isBlank()) {
            try {
                op.setOperatorType(Operator.OperatorType.valueOf(request.getOperatorType()));
            } catch (IllegalArgumentException e) {
                throw new RuntimeException("Type operateur invalide: " + request.getOperatorType());
            }
        }
        if (request.getTeamId() != null)
            op.setTeam(teamRepository.findById(request.getTeamId()).orElseThrow(() -> new RuntimeException("Equipe non trouvee")));
        if (request.getProjectId() != null) {
            op.setProject(projectRepository.findById(request.getProjectId()).orElseThrow(() -> new RuntimeException("Projet non trouve")));
        }
        if (request.getZoneId() != null) {
            op.setZone(zoneRepository.findById(request.getZoneId()).orElseThrow(() -> new RuntimeException("Zone non trouvee")));
        }
        Operator savedOp = operatorRepository.save(op);

        if (request.getWorkstationId() != null) {
            workstationRepository.findById(request.getWorkstationId()).ifPresent(ws -> {
                boolean exists = workstationFormationRepository.findByOperator_Id(savedOp.getId()).stream()
                        .anyMatch(f -> ws.getId().equals(f.getWorkstation().getId()) && "IN_PROGRESS".equals(f.getStatus()));
                if (!exists) {
                    WorkstationFormation formation = new WorkstationFormation();
                    formation.setOperator(savedOp);
                    formation.setWorkstation(ws);
                    formation.setStartDate(LocalDate.now());
                    formation.setStatus("IN_PROGRESS");
                    formation.setAchievedLevel("0");
                    formation.setTargetLevel("1");
                    formation.setQualityObjective(7);
                    workstationFormationRepository.save(formation);

                    FormationAssignment assignment = new FormationAssignment();
                    assignment.setOperator(savedOp);
                    assignment.setWorkstation(ws);
                    assignment.setIsPrimaryAssignment(true);
                    assignment.setStartDate(LocalDate.now());
                    assignment.setStatus("IN_PROGRESS");
                    formationAssignmentRepository.save(assignment);
                }
            });
        }
        return savedOp;
    }

    @Transactional
    public void deactivateOperator(Long id) {
        Operator op = findById(id);
        op.setActive(false);
        op.setExitDate(LocalDate.now());
        operatorRepository.save(op);
        
        // Remove planned and in-progress recyclage planning records for this operator automatically.
        recyclagePlanningRepository.deleteByOperator_IdAndStatusIn(id, List.of(
            com.ilu.system.recyclage.entity.RecyclagePlanning.PlanningStatus.PLANIFIEE,
            com.ilu.system.recyclage.entity.RecyclagePlanning.PlanningStatus.EN_COURS
        ));

        // Clean up any uncompleted in-progress evaluation sessions for this deactivated operator.
        if (evaluationSessionRepository != null) {
            List<EvaluationSession> inProgressSessions = evaluationSessionRepository.findByOperatorIdOrderByCreatedAtDesc(id).stream()
                    .filter(s -> s.getStatus() == EvaluationSession.SessionStatus.IN_PROGRESS)
                    .collect(Collectors.toList());
            if (!inProgressSessions.isEmpty()) {
                evaluationSessionRepository.deleteAll(inProgressSessions);
            }
        }
    }

    @Transactional
    public void activateOperator(Long id) {
        Operator op = findById(id);
        op.setActive(true);
        op.setExitDate(null);
        operatorRepository.save(op);
    }

    private Integer parseIluLevel(String level) {
        if (level == null || level.isBlank()) return 0;
        String upper = level.trim().toUpperCase();
        if (upper.equals("I") || upper.equals("NIVEAU_1") || upper.equals("1")) return 1;
        if (upper.equals("L") || upper.equals("NIVEAU_2") || upper.equals("2")) return 2;
        if (upper.equals("U") || upper.equals("NIVEAU_3") || upper.equals("3")) return 3;
        try {
            return Integer.parseInt(upper);
        } catch (Exception e) {
            return 0;
        }
    }

    public List<FormationDetailsDto> getOperatorFormations(Long operatorId) {
        return workstationFormationRepository.findByOperator_Id(operatorId).stream().map(f -> {
            FormationDetailsDto dto = new FormationDetailsDto();
            dto.setId(f.getId());
            dto.setOperatorId(f.getOperator().getId());
            dto.setOperatorName(f.getOperator().getLastName() + " " + f.getOperator().getFirstName());
            dto.setWorkstationId(f.getWorkstation().getId());
            dto.setWorkstationName(f.getWorkstation().getName());
            dto.setStartDate(f.getStartDate());
            dto.setEndDate(f.getEndDate());
            dto.setStatus(f.getStatus());
            dto.setAchievedLevel(parseIluLevel(f.getAchievedLevel()));
            dto.setTargetLevel(parseIluLevel(f.getTargetLevel()));
            return dto;
        }).collect(Collectors.toList());
    }

    public List<FormationAssignment> getOperatorAssignments(Long operatorId) {
        return formationAssignmentRepository.findByOperatorId(operatorId);
    }

    @Transactional
    public void deleteOperatorPermanently(Long operatorId) {
        Operator op = findById(operatorId);

        // 1. Delete associated absences
        if (absenceRepository != null) {
            absenceRepository.deleteByOperator_Id(operatorId);
        }

        // 2. Delete associated project transfer requests
        if (projectTransferRequestRepository != null && op.getEmployeeId() != null) {
            projectTransferRequestRepository.deleteByEmployeeId(op.getEmployeeId());
        }

        // 3. Delete notifications relating to this operator
        if (notificationRepository != null) {
            notificationRepository.deleteByRelatedOperatorId(operatorId);
        }

        // 4. Delete recyclage plannings
        if (recyclagePlanningRepository != null) {
            recyclagePlanningRepository.deleteByOperator_Id(operatorId);
        }

        // 5. Delete evaluation answers and sessions
        if (evaluationSessionRepository != null) {
            List<EvaluationSession> sessions = evaluationSessionRepository.findByOperatorIdOrderByCreatedAtDesc(operatorId);
            for (EvaluationSession session : sessions) {
                if (evaluationAnswerRepository != null) {
                    evaluationAnswerRepository.deleteBySessionId(session.getId());
                }
                evaluationSessionRepository.delete(session);
            }
        }

        // 6. Delete onboarding records
        if (operatorOnboardingRepository != null) {
            operatorOnboardingRepository.deleteByOperatorId(operatorId);
        }

        // 7. Delete daily formation tracking and formations
        List<WorkstationFormation> formations = workstationFormationRepository.findByOperator_Id(operatorId);
        for (WorkstationFormation formation : formations) {
            if (dailyFormationTrackingRepository != null) {
                dailyFormationTrackingRepository.deleteByFormationId(formation.getId());
            }
        }
        if (formationAssignmentRepository != null) {
            formationAssignmentRepository.deleteByOperatorId(operatorId);
        }
        workstationFormationRepository.deleteByOperator_Id(operatorId);

        // 8. Delete the operator entity permanently
        operatorRepository.delete(op);
    }
}