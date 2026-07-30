package com.ilu.system.operator.service;
import com.ilu.system.auth.entity.User;
import com.ilu.system.auth.repository.UserRepository;
import com.ilu.system.operator.dto.*;
import com.ilu.system.operator.entity.*;
import com.ilu.system.operator.repository.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
@Service
public class OperatorService {
    private final OperatorRepository operatorRepository;
    private final TeamRepository teamRepository;
    private final WorkstationFormationRepository workstationFormationRepository;
    private final FormationAssignmentRepository formationAssignmentRepository;
    private final UserRepository userRepository;

    public OperatorService(OperatorRepository operatorRepository, TeamRepository teamRepository, WorkstationFormationRepository workstationFormationRepository, FormationAssignmentRepository formationAssignmentRepository, UserRepository userRepository) {
        this.operatorRepository = operatorRepository;
        this.teamRepository = teamRepository;
        this.workstationFormationRepository = workstationFormationRepository;
        this.formationAssignmentRepository = formationAssignmentRepository;
        this.userRepository = userRepository;
    }

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) return null;
        return userRepository.findByEmployeeId(auth.getName()).orElse(null);
    }

    private boolean hasRole(String role) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return false;
        return auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_" + role));
    }

    @Transactional
    public Operator createOperator(CreateOperatorRequest request) {
        if (operatorRepository.existsByEmployeeId(request.getEmployeeId())) throw new RuntimeException("Operator with employee ID '" + request.getEmployeeId() + "' already exists");

        // Chef d'Equipe can only add to his own team
        if (hasRole("CHEF_EQUIPE") && !hasRole("ADMIN") && !hasRole("RH") && !hasRole("SUPERVISEUR")) {
            User currentUser = getCurrentUser();
            if (currentUser == null) throw new RuntimeException("User not authenticated");
            Team chefTeam = teamRepository.findByTeamLeaderEmployeeId(currentUser.getEmployeeId()).orElse(null);
            if (chefTeam == null) throw new RuntimeException("Aucune equipe associee a votre compte");
            if (request.getTeamId() != null && !chefTeam.getId().equals(request.getTeamId())) {
                throw new RuntimeException("Vous ne pouvez ajouter des operateurs qu'a votre propre equipe");
            }
            // Force team to chef's team if not specified
            request.setTeamId(chefTeam.getId());
        }

        Operator op = new Operator();
        op.setEmployeeId(request.getEmployeeId());
        op.setLastName(request.getLastName());
        op.setFirstName(request.getFirstName());
        op.setRole(request.getRole());
        if (request.getHireDate() != null) op.setHireDate(LocalDate.parse(request.getHireDate()));
        op.setActive(true);
        if (request.getTeamId() != null) op.setTeam(teamRepository.findById(request.getTeamId()).orElseThrow(() -> new RuntimeException("Team not found")));
        return operatorRepository.save(op);
    }

    public List<Operator> listAll() {
        // Chef d'Equipe sees only his team
        if (hasRole("CHEF_EQUIPE") && !hasRole("ADMIN") && !hasRole("RH") && !hasRole("SUPERVISEUR")) {
            User currentUser = getCurrentUser();
            if (currentUser != null) {
                Team chefTeam = teamRepository.findByTeamLeaderEmployeeId(currentUser.getEmployeeId()).orElse(null);
                if (chefTeam != null) return operatorRepository.findActiveByTeamId(chefTeam.getId());
            }
        }
        return operatorRepository.findAll();
    }

    public List<Operator> listActive() { return operatorRepository.findByActiveTrue(); }
    public Operator findByEmployeeId(String employeeId) { return operatorRepository.findByEmployeeId(employeeId).orElseThrow(() -> new RuntimeException("Operator not found")); }
    public Operator findById(Long id) { return operatorRepository.findById(id).orElseThrow(() -> new RuntimeException("Operator not found")); }
    @Transactional
    public Operator updateOperator(Long id, CreateOperatorRequest request) {
        Operator op = findById(id);
        if (request.getLastName() != null) op.setLastName(request.getLastName());
        if (request.getFirstName() != null) op.setFirstName(request.getFirstName());
        if (request.getRole() != null) op.setRole(request.getRole());
        if (request.getHireDate() != null) op.setHireDate(LocalDate.parse(request.getHireDate()));
        if (request.getTeamId() != null) op.setTeam(teamRepository.findById(request.getTeamId()).orElseThrow(() -> new RuntimeException("Team not found")));
        return operatorRepository.save(op);
    }
    @Transactional
    public void deactivateOperator(Long id) { Operator op = findById(id); op.setActive(false); op.setExitDate(LocalDate.now()); operatorRepository.save(op); }
    @Transactional
    public void activateOperator(Long id) { Operator op = findById(id); op.setActive(true); op.setExitDate(null); op.setAbsenceReason(null); operatorRepository.save(op); }
    public List<FormationDetailsDto> getOperatorFormations(Long operatorId) {
        return workstationFormationRepository.findByOperatorId(operatorId).stream().map(f -> {
            FormationDetailsDto dto = new FormationDetailsDto();
            dto.setId(f.getId()); dto.setOperatorId(f.getOperator().getId());
            dto.setOperatorName(f.getOperator().getLastName() + " " + f.getOperator().getFirstName());
            dto.setWorkstationId(f.getWorkstation().getId()); dto.setWorkstationName(f.getWorkstation().getName());
            dto.setStartDate(f.getStartDate()); dto.setEndDate(f.getEndDate());
            dto.setStatus(f.getStatus()); dto.setAchievedLevel(f.getAchievedLevel()); dto.setTargetLevel(f.getTargetLevel());
            return dto;
        }).collect(Collectors.toList());
    }
    public List<FormationAssignment> getOperatorAssignments(Long operatorId) { return formationAssignmentRepository.findByOperatorId(operatorId); }
}
