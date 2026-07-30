package com.ilu.system.operator.controller;

import com.ilu.system.operator.entity.Team;
import com.ilu.system.operator.repository.TeamRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/teams")
public class TeamController {
    private final TeamRepository teamRepository;
    public TeamController(TeamRepository teamRepository) { this.teamRepository = teamRepository; }

    @GetMapping
    public ResponseEntity<List<Team>> getAll() {
        return ResponseEntity.ok(teamRepository.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Team> getById(@PathVariable Long id) {
        return ResponseEntity.ok(teamRepository.findById(id).orElseThrow(() -> new RuntimeException("Team not found")));
    }
}
