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
    private final com.ilu.system.structure.repository.ProjectRepository projectRepository;

    public TeamController(TeamRepository teamRepository,
                          TeamUpdateRequestRepository requestRepository,
                          OperatorRepository operatorRepository,
                          com.ilu.system.structure.repository.ProjectRepository projectRepository) {
        this.teamRepository = teamRepository;
        this.requestRepository = requestRepository;
        this.operatorRepository = operatorRepository;
        this.projectRepository = projectRepository;
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
            map.put("agentQualite", team.getAgentQualite());
            map.put("agentQualiteEmployeeId", team.getAgentQualiteEmployeeId());
            map.put("qualityManager", team.getQualityManager());
            map.put("qualityManagerEmployeeId", team.getQualityManagerEmployeeId());
            map.put("projectManager", team.getProjectManager());
            map.put("projectManagerEmployeeId", team.getProjectManagerEmployeeId());
            map.put("hseManager", team.getHseManager());
            map.put("hseManagerEmployeeId", team.getHseManagerEmployeeId());

            if (team.getProject() != null) {
                Map<String, Object> projMap = new LinkedHashMap<>();
                projMap.put("id", team.getProject().getId());
                projMap.put("name", team.getProject().getName());
                map.put("project", projMap);
            }

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

    @PostMapping
    @Transactional
    public ResponseEntity<Team> createTeam(@RequestBody Team team) {
        if (team.getProjects() != null && !team.getProjects().isEmpty()) {
            Set<com.ilu.system.structure.entity.Project> managedProjects = new HashSet<>();
            for (var p : team.getProjects()) {
                if (p.getId() != null) {
                    projectRepository.findById(p.getId()).ifPresent(managedProjects::add);
                }
            }
            team.setProjects(managedProjects);
            if (!managedProjects.isEmpty()) {
                team.setProject(managedProjects.iterator().next());
            }
        } else if (team.getProject() != null && team.getProject().getId() != null) {
            projectRepository.findById(team.getProject().getId()).ifPresent(p -> {
                team.setProject(p);
                team.setProjects(Set.of(p));
            });
        }
        return ResponseEntity.ok(teamRepository.save(team));
    }

    @PutMapping("/{id}")
    @Transactional
    public ResponseEntity<Team> updateTeam(@PathVariable Long id, @RequestBody Team teamReq) {
        Team t = teamRepository.findById(id).orElseThrow(() -> new RuntimeException("Équipe introuvable"));
        if (teamReq.getName() != null) t.setName(teamReq.getName());
        if (teamReq.getTeamLeader() != null) t.setTeamLeader(teamReq.getTeamLeader());
        if (teamReq.getTeamLeaderEmployeeId() != null) t.setTeamLeaderEmployeeId(teamReq.getTeamLeaderEmployeeId());
        if (teamReq.getAgentQualite() != null) t.setAgentQualite(teamReq.getAgentQualite());
        if (teamReq.getAgentQualiteEmployeeId() != null) t.setAgentQualiteEmployeeId(teamReq.getAgentQualiteEmployeeId());
        if (teamReq.getQualityManager() != null) t.setQualityManager(teamReq.getQualityManager());
        if (teamReq.getQualityManagerEmployeeId() != null) t.setQualityManagerEmployeeId(teamReq.getQualityManagerEmployeeId());
        if (teamReq.getProjectManager() != null) t.setProjectManager(teamReq.getProjectManager());
        if (teamReq.getProjectManagerEmployeeId() != null) t.setProjectManagerEmployeeId(teamReq.getProjectManagerEmployeeId());
        if (teamReq.getHseManager() != null) t.setHseManager(teamReq.getHseManager());
        if (teamReq.getHseManagerEmployeeId() != null) t.setHseManagerEmployeeId(teamReq.getHseManagerEmployeeId());

        if (teamReq.getProjects() != null && !teamReq.getProjects().isEmpty()) {
            Set<com.ilu.system.structure.entity.Project> managedProjects = new HashSet<>();
            for (var p : teamReq.getProjects()) {
                if (p.getId() != null) {
                    projectRepository.findById(p.getId()).ifPresent(managedProjects::add);
                }
            }
            t.setProjects(managedProjects);
            if (!managedProjects.isEmpty()) {
                t.setProject(managedProjects.iterator().next());
            }
        } else if (teamReq.getProject() != null && teamReq.getProject().getId() != null) {
            projectRepository.findById(teamReq.getProject().getId()).ifPresent(p -> {
                t.setProject(p);
                t.setProjects(Set.of(p));
            });
        }
        return ResponseEntity.ok(teamRepository.save(t));
    }

    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<Void> deleteTeam(@PathVariable Long id) {
        Team t = teamRepository.findById(id).orElseThrow(() -> new RuntimeException("Équipe introuvable"));
        if (t.getOperators() != null) {
            for (Operator op : t.getOperators()) {
                op.setTeam(null);
                operatorRepository.save(op);
            }
        }
        teamRepository.delete(t);
        return ResponseEntity.noContent().build();
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
