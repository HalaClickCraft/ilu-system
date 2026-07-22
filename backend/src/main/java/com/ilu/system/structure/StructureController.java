package com.ilu.system.structure;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ilu.system.structure.ProjectMemberDto;

import java.util.HashMap;
import java.util.List;
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

    @PutMapping("/projects/{projectId}/members/{memberId}")
    public ProjectMemberDto updateProjectMember(@PathVariable Long projectId, @PathVariable Long memberId,
                                                @RequestBody Map<String, Object> payload) {
        Long userId = payload.containsKey("userId") && payload.get("userId") != null
                ? Long.valueOf(payload.get("userId").toString())
                : null;
        String roleProjet = payload.containsKey("roleProjet") && payload.get("roleProjet") != null
                ? payload.get("roleProjet").toString()
                : null;
        return structureService.updateProjectMember(projectId, memberId, userId, roleProjet);
    }

    @DeleteMapping("/projects/{projectId}/members/{memberId}")
    public void removeProjectMember(@PathVariable Long projectId, @PathVariable Long memberId) {
        structureService.removeProjectMember(projectId, memberId);
    }

    @GetMapping("/postes")
    public List<PosteTravailDto> getAllPostes() {
        return structureService.getAllPostes();
    }

     @PostMapping("/postes")
    public PosteTravailDto createPoste(@AuthenticationPrincipal UserDetails userDetails, @RequestBody Map<String, Object> payload) {
        String niveauCibleIlu = payload.get("niveauCibleIlu") != null ? payload.get("niveauCibleIlu").toString() : null;
        return structureService.createPoste(Long.valueOf(payload.get("zoneId").toString()), payload.get("nom").toString(),
                userDetails.getUsername(), niveauCibleIlu);
    }
}
