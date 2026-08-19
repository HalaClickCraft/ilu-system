package com.ilu.system.operator.controller;

import com.ilu.system.operator.entity.Team;
import com.ilu.system.operator.repository.TeamRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/teams")
public class TeamController {
    private final TeamRepository teamRepository;
    public TeamController(TeamRepository teamRepository) { this.teamRepository = teamRepository; }

    // FIX: Return DTOs (Maps) instead of raw Team entities.
    // Previously Team -> operators -> Operator -> team -> ... caused
    // infinite JSON serialization loop (HTTP 500), making the
    // Equipes page and /operators endpoints fail.
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

            // Operator summaries (id, lastName, firstName, employeeId, active)
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

            // Project summaries (id, name) — needed for multi-project filtering
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
}
