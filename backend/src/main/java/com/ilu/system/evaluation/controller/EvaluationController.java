package com.ilu.system.evaluation.controller;

import com.ilu.system.auth.entity.User;
import com.ilu.system.auth.repository.UserRepository;
import com.ilu.system.evaluation.service.EvaluationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
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

    private User currentUser(Authentication authentication) {
        return userRepo.findByEmployeeId(authentication.getName()).orElseThrow(() -> new IllegalStateException("Utilisateur non trouve"));
    }

    private Set<String> roles(Authentication authentication) {
        return currentUser(authentication).getRoles().stream().map(role -> role.getLabel()).collect(Collectors.toSet());
    }

    private Long userId(Authentication authentication) { return currentUser(authentication).getId(); }

    @PostMapping("/templates")
    public ResponseEntity<Map<String, Object>> createTemplate(@RequestBody Map<String, Object> body, Authentication authentication) {
        List<Long> workstationIds = ids(body.get("workstationIds"));
        if (workstationIds.isEmpty() && body.get("workstationId") != null) workstationIds = List.of(Long.valueOf(body.get("workstationId").toString()));
        return ResponseEntity.ok(evaluationService.createTemplate((String) body.get("name"), (String) body.get("description"),
                (String) body.get("type"), workstationIds, (String) body.get("targetNiveau"), userId(authentication), roles(authentication)));
    }

    @GetMapping("/templates")
    public ResponseEntity<List<Map<String, Object>>> getAllTemplates(Authentication authentication) {
        return ResponseEntity.ok(evaluationService.getAllTemplates(roles(authentication)));
    }

    @GetMapping("/templates/{templateId}")
    public ResponseEntity<Map<String, Object>> getTemplate(@PathVariable Long templateId, Authentication authentication) {
        return ResponseEntity.ok(evaluationService.getTemplateWithQuestions(templateId, roles(authentication)));
    }

    @PostMapping("/templates/{templateId}/sections")
    public ResponseEntity<Map<String, Object>> addSection(@PathVariable Long templateId, @RequestBody Map<String, Object> body, Authentication authentication) {
        return ResponseEntity.ok(evaluationService.addSection(templateId, (String) body.get("title"), (String) body.get("description"),
                integer(body.get("displayOrder")), (String) body.get("domain"), roles(authentication)));
    }

    @PostMapping("/templates/{templateId}/questions")
    public ResponseEntity<Map<String, Object>> addQuestion(@PathVariable Long templateId, @RequestBody Map<String, Object> body, Authentication authentication) {
        return ResponseEntity.ok(evaluationService.addQuestion(templateId, longValue(body.get("sectionId")), (String) body.get("questionText"),
                (String) body.get("expectedAnswer"), integer(body.get("questionNumber")),
                (String) body.get("responseType"),
                userId(authentication), roles(authentication)));
    }

    @GetMapping("/questions/pending")
    public ResponseEntity<List<Map<String, Object>>> pendingQuestions(Authentication authentication) {
        return ResponseEntity.ok(evaluationService.getPendingQuestions(roles(authentication)));
    }

    @PostMapping("/questions/{questionId}/validate")
    public ResponseEntity<Map<String, Object>> validateQuestion(@PathVariable Long questionId, Authentication authentication) {
        return ResponseEntity.ok(evaluationService.validateQuestion(questionId, userId(authentication), roles(authentication)));
    }

    @PostMapping("/questions/{questionId}/reject")
    public ResponseEntity<Map<String, Object>> rejectQuestion(@PathVariable Long questionId, @RequestBody Map<String, Object> body, Authentication authentication) {
        return ResponseEntity.ok(evaluationService.rejectQuestion(questionId, userId(authentication), (String) body.get("reason"), roles(authentication)));
    }

    @PostMapping("/templates/{templateId}/validate")
    public ResponseEntity<Map<String, Object>> validateTemplate(@PathVariable Long templateId, Authentication authentication) {
        return ResponseEntity.ok(evaluationService.validateTemplate(templateId, roles(authentication)));
    }

    @PostMapping("/sessions/start")
    public ResponseEntity<Map<String, Object>> startEvaluation(@RequestBody Map<String, Object> body, Authentication authentication) {
        return ResponseEntity.ok(evaluationService.startEvaluation(Long.valueOf(body.get("operatorId").toString()),
                longValue(body.get("formationId")), userId(authentication), roles(authentication)));
    }

    @PostMapping("/sessions/{sessionId}/answers")
    @SuppressWarnings("unchecked")
    public ResponseEntity<Map<String, Object>> submitAnswers(@PathVariable Long sessionId, @RequestBody Map<String, Object> body, Authentication authentication) {
        return ResponseEntity.ok(evaluationService.submitAnswers(sessionId, (List<Map<String, Object>>) body.get("answers"), userId(authentication), roles(authentication)));
    }

    @PostMapping("/sessions/{sessionId}/complete")
    public ResponseEntity<Map<String, Object>> completeEvaluation(@PathVariable Long sessionId, Authentication authentication) {
        return ResponseEntity.ok(evaluationService.completeEvaluation(sessionId, roles(authentication)));
    }

    @GetMapping("/sessions/{sessionId}")
    public ResponseEntity<Map<String, Object>> session(@PathVariable Long sessionId, Authentication authentication) {
        return ResponseEntity.ok(evaluationService.getSessionDetail(sessionId, roles(authentication)));
    }

    @GetMapping("/pending/operator/{operatorId}")
    public ResponseEntity<List<Map<String, Object>>> pendingForOperator(@PathVariable Long operatorId, Authentication authentication) {
        return ResponseEntity.ok(evaluationService.getPendingEvaluationsForOperator(operatorId, roles(authentication)));
    }

    @GetMapping("/pending/all")
    public ResponseEntity<List<Map<String, Object>>> pending(Authentication authentication) {
        return ResponseEntity.ok(evaluationService.getAllPendingEvaluations(roles(authentication)));
    }

    @GetMapping("/matrix")
    public ResponseEntity<Map<String, Object>> matrix(Authentication authentication) {
        return ResponseEntity.ok(evaluationService.getPolyvalenceMatrix(roles(authentication)));
    }

    private Long longValue(Object value) { return value == null ? null : Long.valueOf(value.toString()); }
    private Integer integer(Object value) { return value == null ? null : Integer.valueOf(value.toString()); }
    private List<Long> ids(Object value) {
        if (!(value instanceof List<?>)) return new ArrayList<>();
        return ((List<?>) value).stream().map(v -> Long.valueOf(v.toString())).collect(Collectors.toList());
    }
}
