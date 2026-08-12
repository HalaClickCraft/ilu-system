package com.ilu.system.evaluation.controller;

import com.ilu.system.auth.entity.User;
import com.ilu.system.auth.repository.UserRepository;
import com.ilu.system.evaluation.service.EvaluationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/evaluation")
public class EvaluationController {

    private final EvaluationService evaluationService;
    private final UserRepository userRepo;

    public EvaluationController(EvaluationService evaluationService, UserRepository userRepo) {
        this.evaluationService = evaluationService;
        this.userRepo = userRepo;
    }

    // ======================== HELPERS ========================

    private Long getCurrentUserId(Authentication authentication) {
        return userRepo.findByEmployeeId(authentication.getName())
                .map(User::getId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouve"));
    }

    private Set<String> getCurrentRoles(Authentication authentication) {
        return userRepo.findByEmployeeId(authentication.getName())
                .map(user -> user.getRoles().stream().map(role -> role.getLabel()).collect(Collectors.toSet()))
                .orElse(Set.of());
    }

    private boolean hasRole(Authentication authentication, String role) {
        return getCurrentRoles(authentication).contains(role);
    }

    // ======================== TEMPLATE MANAGEMENT ========================

    @PostMapping("/templates")
    public ResponseEntity<Map<String, Object>> createTemplate(
            @RequestBody Map<String, Object> body,
            Authentication authentication) {
        if (!hasRole(authentication, "ADMIN") && !hasRole(authentication, "AGENT_QUALITE") && !hasRole(authentication, "RESP_HSE") && !hasRole(authentication, "RESP_QUALITE") && !hasRole(authentication, "CHEF_EQUIPE")) {
            return ResponseEntity.status(403).body(Map.of("error", "Seul l'agent qualite ou resp HSE peut creer des templates"));
        }
        Long userId = getCurrentUserId(authentication);
        Map<String, Object> result = evaluationService.createTemplate(
                (String) body.get("name"),
                (String) body.get("description"),
                (String) body.get("type"),
                body.get("workstationId") != null ? Long.valueOf(body.get("workstationId").toString()) : null,
                (String) body.get("targetNiveau"),
                userId
        );
        return ResponseEntity.ok(result);
    }

    @GetMapping("/templates")
    public ResponseEntity<List<Map<String, Object>>> getAllTemplates() {
        return ResponseEntity.ok(evaluationService.getAllTemplates());
    }

    @GetMapping("/templates/{templateId}")
    public ResponseEntity<Map<String, Object>> getTemplateWithQuestions(@PathVariable Long templateId) {
        return ResponseEntity.ok(evaluationService.getTemplateWithQuestions(templateId));
    }

    // ======================== SECTIONS & QUESTIONS ========================

    @PostMapping("/templates/{templateId}/sections")
    public ResponseEntity<Map<String, Object>> addSection(
            @PathVariable Long templateId,
            @RequestBody Map<String, Object> body,
            Authentication authentication) {
       if (!hasRole(authentication, "ADMIN") && !hasRole(authentication, "AGENT_QUALITE") && !hasRole(authentication, "RESP_HSE") && !hasRole(authentication, "RESP_QUALITE") && !hasRole(authentication, "CHEF_EQUIPE")) { 
            return ResponseEntity.status(403).body(Map.of("error", "Non autorise"));
        }
        return ResponseEntity.ok(evaluationService.addSection(
                templateId,
                (String) body.get("title"),
                body.get("displayOrder") != null ? Integer.valueOf(body.get("displayOrder").toString()) : null
        ));
    }

   @PostMapping("/templates/{templateId}/questions")
public ResponseEntity<Map<String, Object>> addQuestion(
        @PathVariable Long templateId,
        @RequestBody Map<String, Object> body,
        Authentication authentication) {
    Long userId = getCurrentUserId(authentication);
    Set<String> roles = getCurrentRoles(authentication);
    return ResponseEntity.ok(evaluationService.addQuestion(
            templateId,
            body.get("sectionId") != null ? Long.valueOf(body.get("sectionId").toString()) : null,
            (String) body.get("questionText"),
            (String) body.get("expectedAnswer"),
            body.get("questionNumber") != null ? Integer.valueOf(body.get("questionNumber").toString()) : null,
            roles,
            userId
    ));
}

    // ======================== QUESTION VALIDATION (Responsable) ========================

    @GetMapping("/questions/pending")
    public ResponseEntity<List<Map<String, Object>>> getPendingQuestions(Authentication authentication) {
        if (!hasRole(authentication, "RESP_QUALITE") && !hasRole(authentication, "RESP_HSE")) {
            return ResponseEntity.status(403).body(List.of());
        }
        return ResponseEntity.ok(evaluationService.getPendingQuestions());
    }

    @PostMapping("/questions/{questionId}/validate")
    public ResponseEntity<Map<String, Object>> validateQuestion(
            @PathVariable Long questionId,
            Authentication authentication) {
        if (!hasRole(authentication, "RESP_QUALITE") && !hasRole(authentication, "RESP_HSE")) {
            return ResponseEntity.status(403).body(Map.of("error", "Seul le responsable peut valider"));
        }
        Long userId = getCurrentUserId(authentication);
        return ResponseEntity.ok(evaluationService.validateQuestion(questionId, userId));
    }

    @PostMapping("/questions/{questionId}/reject")
    public ResponseEntity<Map<String, Object>> rejectQuestion(
            @PathVariable Long questionId,
            @RequestBody Map<String, Object> body,
            Authentication authentication) {
        if (!hasRole(authentication, "RESP_QUALITE") && !hasRole(authentication, "RESP_HSE")) {
            return ResponseEntity.status(403).body(Map.of("error", "Seul le responsable peut rejeter"));
        }
        Long userId = getCurrentUserId(authentication);
        return ResponseEntity.ok(evaluationService.rejectQuestion(questionId, userId, (String) body.get("reason")));
    }

    // ======================== TEMPLATE VALIDATION ========================

    @PostMapping("/templates/{templateId}/validate")
    public ResponseEntity<Map<String, Object>> validateTemplate(
            @PathVariable Long templateId,
            Authentication authentication) {
        if (!hasRole(authentication, "RESP_QUALITE") && !hasRole(authentication, "RESP_HSE")) {
            return ResponseEntity.status(403).body(Map.of("error", "Seul le responsable peut valider le template"));
        }
        Long userId = getCurrentUserId(authentication);
        return ResponseEntity.ok(evaluationService.validateTemplate(templateId, userId));
    }

    // ======================== EVALUATION SESSION ========================

    @PostMapping("/sessions/start")
    public ResponseEntity<Map<String, Object>> startEvaluation(
            @RequestBody Map<String, Object> body,
            Authentication authentication) {
        Long userId = getCurrentUserId(authentication);
        Map<String, Object> result = evaluationService.startEvaluation(
                Long.valueOf(body.get("operatorId").toString()),
                Long.valueOf(body.get("templateId").toString()),
                body.get("formationId") != null ? Long.valueOf(body.get("formationId").toString()) : null,
                userId
        );
        return ResponseEntity.ok(result);
    }

    @PostMapping("/sessions/{sessionId}/answers")
    public ResponseEntity<Map<String, Object>> submitAnswers(
            @PathVariable Long sessionId,
            @RequestBody Map<String, Object> body,
            Authentication authentication) {
        Long userId = getCurrentUserId(authentication);
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> answers = (List<Map<String, Object>>) body.get("answers");
        return ResponseEntity.ok(evaluationService.submitAnswers(sessionId, answers, userId));
    }

    @PostMapping("/sessions/{sessionId}/complete")
    public ResponseEntity<Map<String, Object>> completeEvaluation(@PathVariable Long sessionId) {
        return ResponseEntity.ok(evaluationService.completeEvaluation(sessionId));
    }

    @GetMapping("/sessions/{sessionId}")
    public ResponseEntity<Map<String, Object>> getSessionDetail(@PathVariable Long sessionId) {
        return ResponseEntity.ok(evaluationService.getSessionDetail(sessionId));
    }

    // ======================== AUTO-TRIGGER ENDPOINTS ========================

    /**
     * Returns pending evaluations for a specific operator.
     * Called from FormationDetail when a suivi is COMPLETED.
     */
    @GetMapping("/pending/operator/{operatorId}")
    public ResponseEntity<List<Map<String, Object>>> getPendingEvaluationsForOperator(
            @PathVariable Long operatorId) {
        return ResponseEntity.ok(evaluationService.getPendingEvaluationsForOperator(operatorId));
    }

    /**
     * Global view: all operators with completed suivi but no evaluation yet.
     */
    @GetMapping("/pending/all")
    public ResponseEntity<List<Map<String, Object>>> getAllPendingEvaluations(Authentication authentication) {
        return ResponseEntity.ok(evaluationService.getAllPendingEvaluations());
    }

    // ======================== POLYVALENCE MATRIX ========================

    @GetMapping("/matrix")
    public ResponseEntity<Map<String, Object>> getPolyvalenceMatrix() {
        return ResponseEntity.ok(evaluationService.getPolyvalenceMatrix());
    }
    @PutMapping("/questions/{questionId}")
public ResponseEntity<Map<String, Object>> updateQuestion(
        @PathVariable Long questionId,
        @RequestBody Map<String, Object> body,
        Authentication authentication) {
    Set<String> roles = getCurrentRoles(authentication);
    if (!roles.contains("ADMIN") && !roles.contains("AGENT_QUALITE") && !roles.contains("RESP_HSE")
            && !roles.contains("RESP_QUALITE") && !roles.contains("CHEF_EQUIPE")) {
        return ResponseEntity.status(403).body(Map.of("error", "Non autorise"));
    }
    return ResponseEntity.ok(evaluationService.updateQuestion(
            questionId,
            (String) body.get("questionText"),
            (String) body.get("expectedAnswer"),
            body.get("questionNumber") != null ? Integer.valueOf(body.get("questionNumber").toString()) : null,
            body.get("sectionId") != null ? Long.valueOf(body.get("sectionId").toString()) : null
    ));
}

@DeleteMapping("/questions/{questionId}")
public ResponseEntity<Map<String, Object>> deleteQuestion(
        @PathVariable Long questionId,
        Authentication authentication) {
    Set<String> roles = getCurrentRoles(authentication);
    if (!roles.contains("ADMIN") && !roles.contains("AGENT_QUALITE") && !roles.contains("RESP_HSE")
            && !roles.contains("RESP_QUALITE") && !roles.contains("CHEF_EQUIPE")) {
        return ResponseEntity.status(403).body(Map.of("error", "Non autorise"));
    }
    return ResponseEntity.ok(evaluationService.deleteQuestion(questionId));
}
@PutMapping("/templates/{templateId}")
public ResponseEntity<Map<String, Object>> updateTemplate(
        @PathVariable Long templateId,
        @RequestBody Map<String, Object> body,
        Authentication authentication) {
    Set<String> roles = getCurrentRoles(authentication);
    if (!roles.contains("ADMIN") && !roles.contains("AGENT_QUALITE") && !roles.contains("RESP_HSE")
            && !roles.contains("RESP_QUALITE") && !roles.contains("CHEF_EQUIPE")) {
        return ResponseEntity.status(403).body(Map.of("error", "Non autorise"));
    }
    return ResponseEntity.ok(evaluationService.updateTemplate(templateId,
            (String) body.get("name"), (String) body.get("description"), (String) body.get("targetNiveau")));
}

@DeleteMapping("/templates/{templateId}")
public ResponseEntity<Map<String, Object>> deleteTemplate(
        @PathVariable Long templateId,
        Authentication authentication) {
    Set<String> roles = getCurrentRoles(authentication);
    if (!roles.contains("ADMIN") && !roles.contains("AGENT_QUALITE") && !roles.contains("RESP_HSE")
            && !roles.contains("RESP_QUALITE") && !roles.contains("CHEF_EQUIPE")) {
        return ResponseEntity.status(403).body(Map.of("error", "Non autorise"));
    }
    return ResponseEntity.ok(evaluationService.deleteTemplate(templateId));
}
}