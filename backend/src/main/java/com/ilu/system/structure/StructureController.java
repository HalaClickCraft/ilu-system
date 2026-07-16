package com.ilu.system.structure;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/structure")
public class StructureController {

    private final StructureService structureService;

    public StructureController(StructureService structureService) {
        this.structureService = structureService;
    }

    @GetMapping
    public Map<String, Object> getStructure() {
        structureService.seedDefaultStructureIfNeeded();
        Map<String, Object> response = new HashMap<>();
        response.put("projects", structureService.getProjects());
        return response;
    }

    @PostMapping("/projects")
    public ProjectDto createProject(@AuthenticationPrincipal UserDetails userDetails, @RequestBody CreateProjectRequest payload) {
        return structureService.createProject(payload, userDetails.getUsername());
    }

    @PostMapping("/zones")
    public ZoneDto createZone(@AuthenticationPrincipal UserDetails userDetails, @RequestBody Map<String, Object> payload) {
        return structureService.createZone(Long.valueOf(payload.get("projectId").toString()), payload.get("nom").toString(), userDetails.getUsername());
    }

    @PostMapping("/projects/{projectId}/members")
    public ProjectMemberDto addProjectMember(@PathVariable Long projectId, @RequestBody Map<String, Object> payload) {
        String roleProjet = payload.containsKey("roleProjet") && payload.get("roleProjet") != null ? payload.get("roleProjet").toString() : "SUPERVISEUR";
        return structureService.addProjectMember(projectId, Long.valueOf(payload.get("userId").toString()), roleProjet);
    }

    @PostMapping("/postes")
    public PosteTravailDto createPoste(@AuthenticationPrincipal UserDetails userDetails, @RequestBody Map<String, Object> payload) {
        return structureService.createPoste(Long.valueOf(payload.get("zoneId").toString()), payload.get("nom").toString(), userDetails.getUsername());
    }
}
