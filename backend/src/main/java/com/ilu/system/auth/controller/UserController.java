package com.ilu.system.auth.controller;
import com.ilu.system.auth.dto.CreateUserRequest;
import com.ilu.system.auth.dto.UserDto;
import com.ilu.system.auth.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;
    public UserController(UserService userService) { this.userService = userService; }
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDto> createUser(@RequestBody CreateUserRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.createUser(request));
    }
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserDto>> listAllUsers() { return ResponseEntity.ok(userService.listAll()); }
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDto> getUserById(@PathVariable Long id) { return ResponseEntity.ok(userService.findById(id)); }
    @PutMapping("/{id}/toggle-status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDto> toggleUserStatus(@PathVariable Long id) { return ResponseEntity.ok(userService.toggleActiveStatus(id)); }
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) { userService.deleteUser(id); return ResponseEntity.noContent().build(); }
    @PutMapping("/{id}/roles")
public ResponseEntity<UserDto> updateUserRoles(
        @PathVariable Long id,
        @RequestBody Map<String, Object> body) {
    @SuppressWarnings("unchecked")
    Set<String> roles = new HashSet<>((List<String>) body.get("roles"));
    String department = (String) body.get("department");
    return ResponseEntity.ok(userService.updateUserRoles(id, roles, department));
}

@PostMapping("/seed-roles")
public ResponseEntity<String> seedRoles() {
    userService.seedRoles();
    return ResponseEntity.ok("Roles seeded successfully");
}

@Autowired
private com.ilu.system.operator.repository.DailyFormationTrackingRepository dailyTrackingRepository;
@Autowired
private com.ilu.system.operator.repository.WorkstationFormationRepository formationRepository;
@Autowired
private com.ilu.system.evaluation.repository.EvaluationSessionRepository sessionRepository;
@Autowired
private com.ilu.system.recyclage.repository.RecyclagePlanningRepository recyclagePlanningRepository;
@Autowired
private com.ilu.system.operator.repository.OperatorRepository operatorRepository;
@Autowired
private com.ilu.system.structure.repository.ProjectMemberRepository projectMemberRepository;
@Autowired
private com.ilu.system.structure.repository.WorkstationRepository workstationRepository;
@Autowired
private com.ilu.system.structure.repository.ZoneRepository zoneRepository;
@Autowired
private com.ilu.system.structure.repository.ProjectRepository projectRepository;

@jakarta.persistence.PersistenceContext
private jakarta.persistence.EntityManager entityManager;

@PostMapping("/reset-database")
@org.springframework.transaction.annotation.Transactional
public ResponseEntity<String> resetDatabase() {
    try {
        entityManager.clear();
        entityManager.createNativeQuery("SET FOREIGN_KEY_CHECKS = 0").executeUpdate();
        String[] tables = {
            "daily_formation_tracking", "workstation_formations", "evaluation_answers",
            "evaluation_questions", "evaluation_sections", "evaluation_sessions",
            "evaluation_templates", "recyclage_planning", "operator_onboarding", "operators",
            "project_members", "workstations", "zones", "projects"
        };
        for (String table : tables) {
            try {
                entityManager.createNativeQuery("TRUNCATE TABLE " + table).executeUpdate();
            } catch (Exception ignored) {}
        }
        entityManager.createNativeQuery("SET FOREIGN_KEY_CHECKS = 1").executeUpdate();
        entityManager.clear();
        return ResponseEntity.ok("Base de données totalement réinitialisée à 0 (MySQL Truncate 100%) !");
    } catch (Exception e) {
        return ResponseEntity.status(500).body("Erreur: " + e.getMessage());
    }
}
}
