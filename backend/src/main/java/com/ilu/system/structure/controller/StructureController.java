package com.ilu.system.structure.controller;

import com.ilu.system.structure.dto.*;
import com.ilu.system.structure.service.StructureService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/structure")
public class StructureController {
    private final StructureService structureService;
    public StructureController(StructureService structureService) { this.structureService = structureService; }

    @GetMapping public ResponseEntity<List<ProjectDto>> getAll() { return ResponseEntity.ok(structureService.listProjects()); }
    @PostMapping("/projects") public ResponseEntity<ProjectDto> createProject(@RequestBody CreateProjectRequest req) { return ResponseEntity.status(HttpStatus.CREATED).body(structureService.createProject(req)); }
    @GetMapping("/projects/{id}") public ResponseEntity<ProjectDto> getProject(@PathVariable Long id) { return ResponseEntity.ok(structureService.getProject(id)); }
    @PutMapping("/projects/{id}") public ResponseEntity<ProjectDto> updateProject(@PathVariable Long id, @RequestBody CreateProjectRequest req) { return ResponseEntity.ok(structureService.updateProject(id, req)); }
    @DeleteMapping("/projects/{id}") public ResponseEntity<Void> deleteProject(@PathVariable Long id) { structureService.deleteProject(id); return ResponseEntity.noContent().build(); }
    @PostMapping("/projects/{projectId}/zones") public ResponseEntity<ZoneDto> createZone(@PathVariable Long projectId, @RequestParam String name) { return ResponseEntity.status(HttpStatus.CREATED).body(structureService.createZone(name, projectId)); }
    @GetMapping("/projects/{projectId}/zones") public ResponseEntity<List<ZoneDto>> listZones(@PathVariable Long projectId) { return ResponseEntity.ok(structureService.listZones(projectId)); }
    @DeleteMapping("/zones/{id}") public ResponseEntity<Void> deleteZone(@PathVariable Long id) { structureService.deleteZone(id); return ResponseEntity.noContent().build(); }
    @PostMapping("/workstations") public ResponseEntity<WorkstationDto> createWorkstation(@RequestBody WorkstationDto dto) { return ResponseEntity.status(HttpStatus.CREATED).body(structureService.createWorkstation(dto)); }
    @GetMapping("/workstations") public ResponseEntity<List<WorkstationDto>> listWorkstations() { return ResponseEntity.ok(structureService.listWorkstations()); }
    @GetMapping("/zones/{zoneId}/workstations") public ResponseEntity<List<WorkstationDto>> listByZone(@PathVariable Long zoneId) { return ResponseEntity.ok(structureService.listWorkstationsByZone(zoneId)); }
    @PutMapping("/workstations/{id}") public ResponseEntity<WorkstationDto> updateWorkstation(@PathVariable Long id, @RequestBody WorkstationDto dto) { return ResponseEntity.ok(structureService.updateWorkstation(id, dto)); }
    @DeleteMapping("/workstations/{id}") public ResponseEntity<Void> deleteWorkstation(@PathVariable Long id) { structureService.deleteWorkstation(id); return ResponseEntity.noContent().build(); }
    @PostMapping("/projects/{projectId}/members") public ResponseEntity<ProjectMemberDto> addMember(@PathVariable Long projectId, @RequestParam String employeeId, @RequestParam String employeeName, @RequestParam(required = false) String role) { return ResponseEntity.status(HttpStatus.CREATED).body(structureService.addMember(projectId, employeeId, employeeName, role)); }
    @PutMapping("/members/{memberId}") public ResponseEntity<ProjectMemberDto> updateMember(@PathVariable Long memberId, @RequestParam String role) { return ResponseEntity.ok(structureService.updateMember(memberId, role)); }
    @DeleteMapping("/members/{memberId}") public ResponseEntity<Void> removeMember(@PathVariable Long memberId) { structureService.removeMember(memberId); return ResponseEntity.noContent().build(); }
}