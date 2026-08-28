package com.ilu.system.operator.controller;

import com.ilu.system.operator.entity.Operator;
import com.ilu.system.operator.entity.Team;
import com.ilu.system.operator.entity.TeamUpdateRequest;
import com.ilu.system.operator.repository.OperatorRepository;
import com.ilu.system.operator.repository.TeamRepository;
import com.ilu.system.operator.repository.TeamUpdateRequestRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/teams")
public class TeamController {

    private final TeamRepository teamRepository;
    private final TeamUpdateRequestRepository requestRepository;
    private final OperatorRepository operatorRepository;

    public TeamController(TeamRepository teamRepository,
                          TeamUpdateRequestRepository requestRepository,
                          OperatorRepository operatorRepository) {
        this.teamRepository = teamRepository;
        this.requestRepository = requestRepository;
        this.operatorRepository = operatorRepository;
    }

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getAll() {
        List<Team> teams = teamRepository.findAll();
        List<Map<String, Object>> result = new ArrayList<>();
        for (Team team : teams) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", team.getId());
            map.put("name", team.getName());
            map.put("teamLeader", team.getTeamLeader());
            map.put("teamLeaderEmployeeId", team.getTeamLeaderEmployeeId());

            List<Map<String, Object>> ops = new ArrayList<>();
            if (team.getOperators() != null) {
                for (var op : team.getOperators()) {
                    Map<String, Object> opMap = new LinkedHashMap<>();
                    opMap.put("id", op.getId());
                    opMap.put("lastName", op.getLastName());
                    opMap.put("firstName", op.getFirstName());
                    opMap.put("employeeId", op.getEmployeeId());
                    opMap.put("active", op.getActive());
                    ops.add(opMap);
                }
            }
            map.put("operators", ops);

            List<Map<String, Object>> projs = new ArrayList<>();
            if (team.getProjects() != null) {
                for (var p : team.getProjects()) {
                    Map<String, Object> pMap = new LinkedHashMap<>();
                    pMap.put("id", p.getId());
                    pMap.put("name", p.getName());
                    projs.add(pMap);
                }
            }
            map.put("projects", projs);

            result.add(map);
        }
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Team> getById(@PathVariable Long id) {
        return ResponseEntity.ok(teamRepository.findById(id).orElseThrow(() -> new RuntimeException("Team not found")));
    }

    // Submit team update request (Chef d'équipe -> Needs Superviseur Approval, Superviseur/Admin -> Applies immediately)
    @PostMapping("/{id}/request-update")
    @Transactional
    public ResponseEntity<Map<String, Object>> requestUpdate(
            @PathVariable Long id,
            @RequestBody Map<String, Object> payload,
            Authentication authentication) {

        Team team = teamRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Équipe introuvable"));

        @SuppressWarnings("unchecked")
        List<Number> opIdNums = (List<Number>) payload.get("operatorIds");
        List<Long> opIds = opIdNums != null ? opIdNums.stream().map(Number::longValue).collect(Collectors.toList()) : Collections.emptyList();

        boolean isSupervisorOrAdmin = authentication != null && authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(a -> a.contains("SUPERVISEUR") || a.contains("ADMIN") || a.contains("RH"));

        Map<String, Object> res = new LinkedHashMap<>();

        if (isSupervisorOrAdmin) {
            applyTeamUpdate(team, opIds);
            res.put("status", "APPROVED");
            res.put("message", "Mise à jour de l'équipe appliquée immédiatement par le Superviseur.");
        } else {
            TeamUpdateRequest req = new TeamUpdateRequest();
            req.setTeamId(team.getId());
            req.setTeamName(team.getName());
            req.setRequestedBy(authentication != null ? authentication.getName() : "Chef d'équipe");
            req.setOperatorIds(opIds.stream().map(String::valueOf).collect(Collectors.joining(",")));
            req.setStatus("PENDING_APPROVAL");
            requestRepository.save(req);

            res.put("status", "PENDING_APPROVAL");
            res.put("requestId", req.getId());
            res.put("message", "Demande de mise à jour transmise au Superviseur de Production pour validation.");
        }

        return ResponseEntity.ok(res);
    }

    @GetMapping("/pending-requests")
    public ResponseEntity<List<TeamUpdateRequest>> getPendingRequests() {
        return ResponseEntity.ok(requestRepository.findByStatusOrderByCreatedAtDesc("PENDING_APPROVAL"));
    }

    @PostMapping("/requests/{requestId}/approve")
    @Transactional
    public ResponseEntity<Map<String, Object>> approveRequest(
            @PathVariable Long requestId,
            Authentication authentication) {

        TeamUpdateRequest req = requestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Demande introuvable"));

        Team team = teamRepository.findById(req.getTeamId())
                .orElseThrow(() -> new RuntimeException("Équipe introuvable"));

        List<Long> opIds = Arrays.stream(req.getOperatorIds().split(","))
                .filter(s -> !s.isBlank())
                .map(Long::parseLong)
                .collect(Collectors.toList());

        applyTeamUpdate(team, opIds);

        req.setStatus("APPROVED");
        req.setApprovedBy(authentication != null ? authentication.getName() : "Superviseur");
        req.setApprovedAt(LocalDateTime.now());
        requestRepository.save(req);

        Map<String, Object> res = new LinkedHashMap<>();
        res.put("status", "APPROVED");
        res.put("message", "Mise à jour d'équipe validée et appliquée avec succès par le Superviseur.");
        return ResponseEntity.ok(res);
    }

    @PostMapping("/requests/{requestId}/reject")
    @Transactional
    public ResponseEntity<Map<String, Object>> rejectRequest(
            @PathVariable Long requestId,
            Authentication authentication) {

        TeamUpdateRequest req = requestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Demande introuvable"));

        req.setStatus("REJECTED");
        req.setApprovedBy(authentication != null ? authentication.getName() : "Superviseur");
        req.setApprovedAt(LocalDateTime.now());
        requestRepository.save(req);

        Map<String, Object> res = new LinkedHashMap<>();
        res.put("status", "REJECTED");
        res.put("message", "Demande de mise à jour d'équipe refusée par le Superviseur.");
        return ResponseEntity.ok(res);
    }

    private void applyTeamUpdate(Team team, List<Long> operatorIds) {
        if (team.getOperators() != null) {
            for (Operator op : team.getOperators()) {
                op.setTeam(null);
                operatorRepository.save(op);
            }
        }

        List<Operator> newOps = operatorRepository.findAllById(operatorIds);
        for (Operator op : newOps) {
            op.setTeam(team);
            operatorRepository.save(op);
        }
    }
}
