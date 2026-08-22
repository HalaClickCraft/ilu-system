package com.ilu.system.security;

import com.ilu.system.auth.entity.User;
import com.ilu.system.auth.repository.UserRepository;
import com.ilu.system.operator.repository.OperatorRepository;
import com.ilu.system.structure.entity.ProjectMember;
import com.ilu.system.structure.repository.ProjectMemberRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AccessControlService {
    private final UserRepository userRepository;
    private final OperatorRepository operatorRepository;
    private final ProjectMemberRepository projectMemberRepository;

    public AccessControlService(UserRepository userRepository, OperatorRepository operatorRepository,
                                ProjectMemberRepository projectMemberRepository) {
        this.userRepository = userRepository;
        this.operatorRepository = operatorRepository;
        this.projectMemberRepository = projectMemberRepository;
    }

    public User currentUser(Authentication authentication) {
        return userRepository.findByEmployeeId(authentication.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Utilisateur non trouve"));
    }

    public void requireAbsenceManagement(Authentication authentication, Long operatorId) {
        User user = currentUser(authentication);
        if (hasAnyRole(user, "ADMIN", "RH", "SUPERVISEUR")) return;
        if (!hasRole(user, "CHEF_EQUIPE")) deny();
        Long projectId = operatorRepository.findById(operatorId)
                .map(operator -> operator.getProject() != null ? operator.getProject().getId() : null)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Operateur introuvable"));
        requireChefProject(user, projectId);
    }

    public void requireRecyclageManagement(Authentication authentication, Long projectId) {
        User user = currentUser(authentication);
        if (hasAnyRole(user, "ADMIN", "RH", "SUPERVISEUR", "RESP_QUALITE")) return;
        if (!hasRole(user, "CHEF_EQUIPE")) deny();
        requireChefProject(user, projectId);
    }

    public boolean hasRole(User user, String role) {
        return user.getRoles().stream().anyMatch(item -> role.equals(item.getLabel()));
    }

    private boolean hasAnyRole(User user, String... roles) {
        for (String role : roles) if (hasRole(user, role)) return true;
        return false;
    }

    private void requireChefProject(User user, Long projectId) {
        boolean leadsProject = projectId != null && projectMemberRepository.findByEmployeeId(user.getEmployeeId()).stream()
                .anyMatch(member -> projectId.equals(member.getProject().getId())
                        && member.getProjectRole() == ProjectMember.ProjectRole.TEAM_LEADER);
        if (!leadsProject) deny();
    }

    private void deny() {
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Acces refuse pour ce projet ou cette equipe");
    }
}
