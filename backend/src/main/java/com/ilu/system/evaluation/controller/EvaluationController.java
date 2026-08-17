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
                if (!hasRole(authentication, "AGENT_QUALITE") && !hasRole(authentication, "RESP_HSE") && !hasRole(authentication, "RESP_QUALITE") && !hasRole(authentication, "CHEF_EQUIPE") && !hasRole(authentication, "SUPERVISEUR") && !hasRole(authentication, "ADMIN")) {
            return ResponseEntity.status(403).body(Map.of("error", "Non autorise"));
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
                if (!hasRole(authentication, "AGENT_QUALITE") && !hasRole(authentication, "RESP_HSE") && !hasRole(authentication, "RESP_QUALITE") && !hasRole(authentication, "CHEF_EQUIPE") && !hasRole(authentication, "SUPERVISEUR") && !hasRole(authentication, "ADMIN")) {
            return ResponseEntity.status(403).body(Map.of("error", "Non autorise"));
        }
        return ResponseEntity.ok(evaluationService.addSection(
                templateId,
                (String) body.get("title"),
                body.get("displayOrder") != null ? Integer.valueOf(body.get("displayOrder").toString()) : null,
                body.get("complementaryQuestions") != null ? body.get("complementaryQuestions").toString() : null
        ));
    }

    @PostMapping("/templates/{templateId}/questions")
    public ResponseEntity<Map<String, Object>> addQuestion(
            @PathVariable Long templateId,
            @RequestBody Map<String, Object> body,
            Authentication authentication) {
        if (!hasRole(authentication, "CHEF_EQUIPE") && !hasRole(authentication, "AGENT_QUALITE") && !hasRole(authentication, "RESP_QUALITE") && !hasRole(authentication, "SUPERVISEUR") && !hasRole(authentication, "ADMIN")) {
            return ResponseEntity.status(403).body(Map.of("error", "Non autorise"));
        }
        Long userId = getCurrentUserId(authentication);
        return ResponseEntity.ok(evaluationService.addQuestion(
                templateId,
                body.get("sectionId") != null ? Long.valueOf(body.get("sectionId").toString()) : null,
                (String) body.get("questionText"),
                (String) body.get("expectedAnswer"),
                body.get("questionNumber") != null ? Integer.valueOf(body.get("questionNumber").toString()) : null,
                (String) body.get("validatorRole"),
                body.get("complementaryQuestions") != null ? body.get("complementaryQuestions").toString() : null,
                userId
        ));
    }

    @PutMapping("/questions/{questionId}")
    public ResponseEntity<Map<String, Object>> updateQuestion(
            @PathVariable Long questionId,
            @RequestBody Map<String, Object> body,
            Authentication authentication) {
        if (!hasRole(authentication, "CHEF_EQUIPE") && !hasRole(authentication, "AGENT_QUALITE") && !hasRole(authentication, "RESP_QUALITE") && !hasRole(authentication, "SUPERVISEUR") && !hasRole(authentication, "ADMIN")) {
            return ResponseEntity.status(403).body(Map.of("error", "Non autorise"));
        }

        return ResponseEntity.ok(evaluationService.updateQuestion(
                questionId,
                body.get("questionText") != null ? (String) body.get("questionText") : null,
                body.get("expectedAnswer") != null ? (String) body.get("expectedAnswer") : null,
                body.get("validatorRole") != null ? (String) body.get("validatorRole") : null,
                body.get("questionNumber") != null ? Integer.valueOf(body.get("questionNumber").toString()) : null,
                body.get("sectionId") != null ? Long.valueOf(body.get("sectionId").toString()) : null,
                body.get("templateId") != null ? Long.valueOf(body.get("templateId").toString()) : null,
                body.get("complementaryQuestions") != null ? (String) body.get("complementaryQuestions") : null
        ));
    }

    @DeleteMapping("/questions/{questionId}")
    public ResponseEntity<Map<String, Object>> deleteQuestion(
            @PathVariable Long questionId,
            @RequestParam(required = false) Long templateId,
            Authentication authentication) {
        if (!hasRole(authentication, "CHEF_EQUIPE") && !hasRole(authentication, "AGENT_QUALITE") && !hasRole(authentication, "RESP_QUALITE") && !hasRole(authentication, "SUPERVISEUR") && !hasRole(authentication, "ADMIN")) {
            return ResponseEntity.status(403).body(Map.of("error", "Non autorise"));
        }
        return ResponseEntity.ok(evaluationService.deleteQuestion(questionId, templateId));
    }

    // ======================== QUESTION VALIDATION (Responsable) ========================

    @GetMapping("/questions/pending")
    public ResponseEntity<List<Map<String, Object>>> getPendingQuestions(Authentication authentication) {
        if (!hasRole(authentication, "RESP_QUALITE") && !hasRole(authentication, "ADMIN")) {
            return ResponseEntity.status(403).body(List.of());
        }
        return ResponseEntity.ok(evaluationService.getPendingQuestions());
    }

    @PostMapping("/questions/{questionId}/validate")
    public ResponseEntity<Map<String, Object>> validateQuestion(
            @PathVariable Long questionId,
            Authentication authentication) {
        if (!hasRole(authentication, "RESP_QUALITE") && !hasRole(authentication, "ADMIN")) {
            return ResponseEntity.status(403).body(Map.of("error", "Seule la responsable Qualite peut valider une question"));
        }
        Long userId = getCurrentUserId(authentication);
        return ResponseEntity.ok(evaluationService.validateQuestion(questionId, userId));
    }

    @PostMapping("/questions/{questionId}/reject")
    public ResponseEntity<Map<String, Object>> rejectQuestion(
            @PathVariable Long questionId,
            @RequestBody Map<String, Object> body,
            Authentication authentication) {
        if (!hasRole(authentication, "RESP_QUALITE") && !hasRole(authentication, "ADMIN")) {
            return ResponseEntity.status(403).body(Map.of("error", "Seule la responsable Qualite peut rejeter une question"));
        }
        Long userId = getCurrentUserId(authentication);
        return ResponseEntity.ok(evaluationService.rejectQuestion(questionId, userId, (String) body.get("reason")));
    }

    // ======================== TEMPLATE VALIDATION ========================

    @PostMapping("/templates/{templateId}/validate")
    public ResponseEntity<Map<String, Object>> validateTemplate(
            @PathVariable Long templateId,
            Authentication authentication) {
        if (!hasRole(authentication, "CHEF_EQUIPE") && !hasRole(authentication, "RESP_QUALITE") && !hasRole(authentication, "ADMIN")) {
            return ResponseEntity.status(403).body(Map.of("error", "Seul le chef d'equipe ou la responsable Qualite peut valider le template"));
        }
        Long userId = getCurrentUserId(authentication);
        return ResponseEntity.ok(evaluationService.validateTemplate(templateId, userId));
    }

    // ======================== INITIAL EVALUATION ========================

    /**
     * Auto-resolve templates for INITIAL evaluation flow.
     * Returns the generic template (global, no workstation) and production template (matched by workstation).
     */
    @GetMapping("/initial/resolve-templates")
    public ResponseEntity<Map<String, Object>> resolveTemplatesForInitial(
            @RequestParam Long operatorId,
            @RequestParam Long formationId) {
        return ResponseEntity.ok(evaluationService.resolveTemplatesForInitial(operatorId, formationId));
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
                userId,
                body.get("mode") != null ? body.get("mode").toString() : null,
                body.get("nextTemplateId") != null ? Long.valueOf(body.get("nextTemplateId").toString()) : null
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

    @GetMapping("/pending/operator/{operatorId}")
    public ResponseEntity<List<Map<String, Object>>> getPendingEvaluationsForOperator(
            @PathVariable Long operatorId) {
        return ResponseEntity.ok(evaluationService.getPendingEvaluationsForOperator(operatorId));
    }

    @GetMapping("/pending/all")
    public ResponseEntity<List<Map<String, Object>>> getAllPendingEvaluations(Authentication authentication) {
        return ResponseEntity.ok(evaluationService.getAllPendingEvaluations());
    }

    // ======================== POLYVALENCE MATRIX ========================

    @GetMapping("/matrix")
    public ResponseEntity<Map<String, Object>> getPolyvalenceMatrix() {
        return ResponseEntity.ok(evaluationService.getPolyvalenceMatrix());
    }

    @GetMapping("/double-failures")
    public ResponseEntity<List<Map<String, Object>>> getDoubleFailures(Authentication authentication) {
        if (!hasRole(authentication, "ADMIN") && !hasRole(authentication, "RH") && !hasRole(authentication, "RESP_QUALITE") && !hasRole(authentication, "SUPERVISEUR")) {
            return ResponseEntity.status(403).build();
        }
        return ResponseEntity.ok(evaluationService.getDoubleFailures());
    }
    // ======================== EVALUATION HISTORY ========================

    @GetMapping("/history")
    public ResponseEntity<Map<String, Object>> getEvaluationHistory() {
        return ResponseEntity.ok(evaluationService.getEvaluationHistory());
    }
}