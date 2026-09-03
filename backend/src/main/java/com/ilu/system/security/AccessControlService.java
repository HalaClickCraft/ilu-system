package com.ilu.system.security;

import com.ilu.system.auth.entity.User;
import com.ilu.system.auth.repository.UserRepository;
import com.ilu.system.operator.repository.OperatorRepository;
import com.ilu.system.operator.entity.Operator;
import com.ilu.system.operator.repository.TeamRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AccessControlService {
    private final UserRepository userRepository;
    private final OperatorRepository operatorRepository;
    private final TeamRepository teamRepository;

    public AccessControlService(UserRepository userRepository, OperatorRepository operatorRepository,
                                TeamRepository teamRepository) {
        this.userRepository = userRepository;
        this.operatorRepository = operatorRepository;
        this.teamRepository = teamRepository;
    }

    public User currentUser(Authentication authentication) {
        return userRepository.findByEmployeeId(authentication.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Utilisateur non trouve"));
    }

    public void requireAbsenceManagement(Authentication authentication, Long operatorId) {
        User user = currentUser(authentication);
        if (hasAnyRole(user, "ADMIN", "RH", "SUPERVISEUR", "CHEF_EQUIPE", "RESP_QUALITE", "RESP_HSE")) return;
        deny();
    }

    public void requireRecyclageManagement(Authentication authentication, Long projectId) {
        User user = currentUser(authentication);
        if (hasAnyRole(user, "ADMIN", "RH", "SUPERVISEUR", "RESP_QUALITE", "RESP_HSE", "CHEF_EQUIPE")) return;
        deny();
    }

    public boolean hasRole(User user, String role) {
        return user.getRoles().stream().anyMatch(item -> role.equals(item.getLabel()));
    }

    private boolean hasAnyRole(User user, String... roles) {
        for (String role : roles) if (hasRole(user, role)) return true;
        return false;
    }

    private void requireChefProject(User user, Long projectId) {
        boolean leadsProject = projectId != null && teamRepository.findByProjectId(projectId).stream()
                .anyMatch(team -> user.getEmployeeId().equals(team.getTeamLeaderEmployeeId()));
        if (!leadsProject) deny();
    }

    private void deny() {
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Acces refuse pour ce projet ou cette equipe");
    }
}
