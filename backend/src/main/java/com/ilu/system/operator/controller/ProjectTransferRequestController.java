package com.ilu.system.operator.controller;

import com.ilu.system.operator.entity.Operator;
import com.ilu.system.operator.entity.ProjectTransferRequest;
import com.ilu.system.operator.repository.OperatorRepository;
import com.ilu.system.operator.repository.ProjectTransferRequestRepository;
import com.ilu.system.structure.entity.Project;
import com.ilu.system.structure.repository.ProjectRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/project-transfers")
public class ProjectTransferRequestController {

    private final ProjectTransferRequestRepository requestRepository;
    private final OperatorRepository operatorRepository;
    private final ProjectRepository projectRepository;

    public ProjectTransferRequestController(
            ProjectTransferRequestRepository requestRepository,
            OperatorRepository operatorRepository,
            ProjectRepository projectRepository) {
        this.requestRepository = requestRepository;
        this.operatorRepository = operatorRepository;
        this.projectRepository = projectRepository;
    }

    @PostMapping("/request")
    @Transactional
    public ResponseEntity<Map<String, Object>> createRequest(
            @RequestParam String employeeId,
            @RequestParam Long targetProjectId,
            Authentication authentication) {

        Operator operator = operatorRepository.findByEmployeeId(employeeId)
                .orElseThrow(() -> new RuntimeException("Opérateur introuvable avec le matricule: " + employeeId));

        Project targetProject = projectRepository.findById(targetProjectId)
                .orElseThrow(() -> new RuntimeException("Projet cible introuvable"));

        boolean isSupervisorOrAdmin = authentication != null && authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(a -> a.contains("SUPERVISEUR") || a.contains("ADMIN") || a.contains("RH"));

        Map<String, Object> res = new LinkedHashMap<>();

        if (isSupervisorOrAdmin) {
            // Apply instantly
            operator.setProject(targetProject);
            operatorRepository.save(operator);

            ProjectTransferRequest req = new ProjectTransferRequest();
            req.setEmployeeId(operator.getEmployeeId());
            req.setOperatorName(operator.getLastName() + " " + operator.getFirstName());
            if (operator.getProject() != null) {
                req.setSourceProjectId(operator.getProject().getId());
                req.setSourceProjectName(operator.getProject().getName());
            }
            req.setTargetProjectId(targetProject.getId());
            req.setTargetProjectName(targetProject.getName());
            req.setRequestedBy(authentication != null ? authentication.getName() : "Superviseur");
            req.setStatus("APPROVED");
            req.setApprovedBy(authentication != null ? authentication.getName() : "Superviseur");
            req.setApprovedAt(LocalDateTime.now());
            requestRepository.save(req);

            res.put("status", "APPROVED");
            res.put("message", "Opérateur transféré immédiatement par le Superviseur.");
        } else {
            // Create request
            ProjectTransferRequest req = new ProjectTransferRequest();
            req.setEmployeeId(operator.getEmployeeId());
            req.setOperatorName(operator.getLastName() + " " + operator.getFirstName());
            if (operator.getProject() != null) {
                req.setSourceProjectId(operator.getProject().getId());
                req.setSourceProjectName(operator.getProject().getName());
            }
            req.setTargetProjectId(targetProject.getId());
            req.setTargetProjectName(targetProject.getName());
            req.setRequestedBy(authentication != null ? authentication.getName() : "Chef d'équipe");
            req.setStatus("PENDING");
            requestRepository.save(req);

            res.put("status", "PENDING");
            res.put("requestId", req.getId());
            res.put("message", "Demande de transfert transmise au Superviseur de Production pour validation.");
        }

        return ResponseEntity.ok(res);
    }

    @GetMapping("/pending")
    public ResponseEntity<List<ProjectTransferRequest>> getPendingRequests() {
        return ResponseEntity.ok(requestRepository.findByStatusOrderByCreatedAtDesc("PENDING"));
    }

    @PostMapping("/requests/{requestId}/approve")
    @Transactional
    public ResponseEntity<Map<String, Object>> approveRequest(
            @PathVariable Long requestId,
            Authentication authentication) {

        ProjectTransferRequest req = requestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Demande introuvable"));

        Operator operator = operatorRepository.findByEmployeeId(req.getEmployeeId())
                .orElseThrow(() -> new RuntimeException("Opérateur introuvable"));

        Project targetProject = projectRepository.findById(req.getTargetProjectId())
                .orElseThrow(() -> new RuntimeException("Projet cible introuvable"));

        // Update operator's project
        operator.setProject(targetProject);
        operatorRepository.save(operator);

        req.setStatus("APPROVED");
        req.setApprovedBy(authentication != null ? authentication.getName() : "Superviseur");
        req.setApprovedAt(LocalDateTime.now());
        requestRepository.save(req);

        Map<String, Object> res = new LinkedHashMap<>();
        res.put("status", "APPROVED");
        res.put("message", "Demande de transfert validée et appliquée avec succès par le Superviseur.");
        return ResponseEntity.ok(res);
    }

    @PostMapping("/requests/{requestId}/reject")
    @Transactional
    public ResponseEntity<Map<String, Object>> rejectRequest(
            @PathVariable Long requestId,
            Authentication authentication) {

        ProjectTransferRequest req = requestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Demande introuvable"));

        req.setStatus("REJECTED");
        req.setApprovedBy(authentication != null ? authentication.getName() : "Superviseur");
        req.setApprovedAt(LocalDateTime.now());
        requestRepository.save(req);

        Map<String, Object> res = new LinkedHashMap<>();
        res.put("status", "REJECTED");
        res.put("message", "Demande de transfert refusée par le Superviseur.");
        return ResponseEntity.ok(res);
    }
}
