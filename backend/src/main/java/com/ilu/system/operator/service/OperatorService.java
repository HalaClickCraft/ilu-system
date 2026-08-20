package com.ilu.system.operator.service;

import com.ilu.system.operator.dto.*;
import com.ilu.system.operator.entity.*;
import com.ilu.system.operator.repository.*;
import com.ilu.system.structure.repository.ProjectRepository;
import com.ilu.system.structure.repository.ZoneRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OperatorService {
    private final OperatorRepository operatorRepository;
    private final TeamRepository teamRepository;
    private final ProjectRepository projectRepository;
    private final ZoneRepository zoneRepository;
    private final WorkstationFormationRepository workstationFormationRepository;
    private final FormationAssignmentRepository formationAssignmentRepository;

    public OperatorService(OperatorRepository operatorRepository, TeamRepository teamRepository,
                           ProjectRepository projectRepository, ZoneRepository zoneRepository,
                           WorkstationFormationRepository workstationFormationRepository,
                           FormationAssignmentRepository formationAssignmentRepository) {
        this.operatorRepository = operatorRepository;
        this.teamRepository = teamRepository;
        this.projectRepository = projectRepository;
        this.zoneRepository = zoneRepository;
        this.workstationFormationRepository = workstationFormationRepository;
        this.formationAssignmentRepository = formationAssignmentRepository;
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
        if (request.getAbsenceReason() != null && !request.getAbsenceReason().isBlank())
            op.setAbsenceReason(request.getAbsenceReason());
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
        return operatorRepository.save(op);
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
        // Handle absenceReason
        if (request.getAbsenceReason() != null) {
            op.setAbsenceReason(request.getAbsenceReason().isBlank() ? null : request.getAbsenceReason());
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
        return operatorRepository.save(op);
    }

    @Transactional
    public void deactivateOperator(Long id) {
        Operator op = findById(id);
        op.setActive(false);
        op.setExitDate(LocalDate.now());
        operatorRepository.save(op);
    }

    @Transactional
    public void activateOperator(Long id) {
        Operator op = findById(id);
        op.setActive(true);
        op.setExitDate(null);
        op.setAbsenceReason(null);
        operatorRepository.save(op);
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
           try { dto.setAchievedLevel(Integer.parseInt(f.getAchievedLevel())); } catch (Exception e) { dto.setAchievedLevel(0); }
try { dto.setTargetLevel(Integer.parseInt(f.getTargetLevel())); } catch (Exception e) { dto.setTargetLevel(0); }
            return dto;
        }).collect(Collectors.toList());
    }

    public List<FormationAssignment> getOperatorAssignments(Long operatorId) {
        return formationAssignmentRepository.findByOperatorId(operatorId);
    }
}