package com.ilu.system.structure.service;

import com.ilu.system.auth.entity.User;
import com.ilu.system.auth.repository.UserRepository;
import com.ilu.system.structure.dto.*;
import com.ilu.system.structure.entity.*;
import com.ilu.system.structure.repository.*;
import com.ilu.system.operator.repository.TeamRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class StructureService {
    private final ProjectRepository projectRepo;
    private final ZoneRepository zoneRepo;
    private final WorkstationRepository wsRepo;
    private final ProjectMemberRepository memberRepo;
    private final UserRepository userRepo;
    private final TeamRepository teamRepo;

    public StructureService(ProjectRepository projectRepo, ZoneRepository zoneRepo, WorkstationRepository wsRepo, ProjectMemberRepository memberRepo, UserRepository userRepo, TeamRepository teamRepo) {
        this.projectRepo = projectRepo; this.zoneRepo = zoneRepo; this.wsRepo = wsRepo; this.memberRepo = memberRepo;
        this.userRepo = userRepo; this.teamRepo = teamRepo;
    }

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) return null;
        return userRepo.findByEmployeeId(auth.getName()).orElse(null);
    }

    @Transactional
    public ProjectDto createProject(CreateProjectRequest req) {
        if (projectRepo.existsByName(req.getName())) throw new RuntimeException("Project already exists");
        Project p = new Project(); p.setName(req.getName());
        p.setCreatedBy(getCurrentUser());
        p = projectRepo.save(p);
        // Link teams if provided
        if (req.getTeamIds() != null) {
            Set<com.ilu.system.operator.entity.Team> teams = teamRepo.findAllById(req.getTeamIds()).stream().collect(Collectors.toSet());
            p.setTeams(teams);
            p = projectRepo.save(p);
        }
        if (req.getMembers() != null) {
            for (CreateProjectRequest.MemberAssignment m : req.getMembers()) {
                ProjectMember member = new ProjectMember(); member.setProject(p);
                member.setEmployeeId(m.getEmployeeId()); member.setEmployeeName(m.getEmployeeName());
                if (m.getProjectRole() != null) member.setProjectRole(ProjectMember.ProjectRole.valueOf(m.getProjectRole()));
                memberRepo.save(member);
            }
        }
        return toDto(p);
    }

    public List<ProjectDto> listProjects() { return projectRepo.findAll().stream().map(this::toDto).collect(Collectors.toList()); }
    public ProjectDto getProject(Long id) { return toDto(projectRepo.findById(id).orElseThrow(() -> new RuntimeException("Project not found"))); }
    @Transactional
    public ProjectDto updateProject(Long id, CreateProjectRequest req) {
        Project p = projectRepo.findById(id).orElseThrow(() -> new RuntimeException("Project not found"));
        if (req.getName() != null) p.setName(req.getName()); return toDto(projectRepo.save(p));
    }
    @Transactional public void deleteProject(Long id) { projectRepo.deleteById(id); }

    @Transactional
    public ZoneDto createZone(String name, Long projectId) {
        Project p = projectRepo.findById(projectId).orElseThrow(() -> new RuntimeException("Project not found"));
        Zone z = new Zone(); z.setName(name); z.setProject(p); z.setCreatedBy(getCurrentUser());
        return toZoneDto(zoneRepo.save(z));
    }
    public List<ZoneDto> listZones(Long projectId) { return zoneRepo.findByProjectId(projectId).stream().map(this::toZoneDto).collect(Collectors.toList()); }
    @Transactional public void deleteZone(Long id) { zoneRepo.deleteById(id); }

    @Transactional
    public WorkstationDto createWorkstation(WorkstationDto dto) {
        Workstation w = new Workstation(); w.setName(dto.getName()); w.setType(dto.getType());
        w.setTargetCadence(dto.getTargetCadence()); w.setVersatilityTarget(dto.getVersatilityTarget()); w.setTargetIluLevel(dto.getTargetIluLevel());
        w.setCreatedBy(getCurrentUser());
        if (dto.getZoneId() != null) w.setZone(zoneRepo.findById(dto.getZoneId()).orElseThrow(() -> new RuntimeException("Zone not found")));
        return toWsDto(wsRepo.save(w));
    }
    public List<WorkstationDto> listWorkstations() { return wsRepo.findAll().stream().map(this::toWsDto).collect(Collectors.toList()); }
    public List<WorkstationDto> listWorkstationsByZone(Long zoneId) { return wsRepo.findByZoneId(zoneId).stream().map(this::toWsDto).collect(Collectors.toList()); }
    public List<WorkstationDto> listWorkstationsByProject(Long projectId) { return wsRepo.findByProjectId(projectId).stream().map(this::toWsDto).collect(Collectors.toList()); }
    @Transactional
    public WorkstationDto updateWorkstation(Long id, WorkstationDto dto) {
        Workstation w = wsRepo.findById(id).orElseThrow(() -> new RuntimeException("Workstation not found"));
        if (dto.getName() != null) w.setName(dto.getName()); if (dto.getType() != null) w.setType(dto.getType());
        if (dto.getTargetCadence() != null) w.setTargetCadence(dto.getTargetCadence());
        if (dto.getVersatilityTarget() != null) w.setVersatilityTarget(dto.getVersatilityTarget());
        if (dto.getTargetIluLevel() != null) w.setTargetIluLevel(dto.getTargetIluLevel());
        if (dto.getZoneId() != null) w.setZone(zoneRepo.findById(dto.getZoneId()).orElseThrow(() -> new RuntimeException("Zone not found")));
        return toWsDto(wsRepo.save(w));
    }
    @Transactional public void deleteWorkstation(Long id) { wsRepo.deleteById(id); }

    @Transactional
    public ProjectMemberDto addMember(Long projectId, String employeeId, String employeeName, String role) {
        Project p = projectRepo.findById(projectId).orElseThrow(() -> new RuntimeException("Project not found"));
        ProjectMember m = new ProjectMember(); m.setProject(p); m.setEmployeeId(employeeId); m.setEmployeeName(employeeName);
        if (role != null) m.setProjectRole(ProjectMember.ProjectRole.valueOf(role));
        return toMemberDto(memberRepo.save(m));
    }
    @Transactional
    public ProjectMemberDto updateMember(Long memberId, String role) {
        ProjectMember m = memberRepo.findById(memberId).orElseThrow(() -> new RuntimeException("Member not found"));
        if (role != null) m.setProjectRole(ProjectMember.ProjectRole.valueOf(role));
        return toMemberDto(memberRepo.save(m));
    }
    @Transactional public void removeMember(Long memberId) { memberRepo.deleteById(memberId); }

    private ProjectDto toDto(Project p) {
        ProjectDto d = new ProjectDto(); d.setId(p.getId()); d.setName(p.getName());
        if (p.getCreatedBy() != null) d.setCreatedByName(p.getCreatedBy().getName());
        d.setZones(p.getZones() != null ? p.getZones().stream().map(this::toZoneDto).collect(Collectors.toList()) : List.of());
        d.setMembers(p.getMembers() != null ? p.getMembers().stream().map(this::toMemberDto).collect(Collectors.toList()) : List.of());
        return d;
    }
    private ZoneDto toZoneDto(Zone z) {
        ZoneDto d = new ZoneDto(); d.setId(z.getId()); d.setName(z.getName());
        if (z.getProject() != null) { d.setProjectId(z.getProject().getId()); d.setProjectName(z.getProject().getName()); }
        if (z.getCreatedBy() != null) d.setCreatedByName(z.getCreatedBy().getName());
        d.setWorkstations(z.getWorkstations() != null ? z.getWorkstations().stream().map(this::toWsDto).collect(Collectors.toList()) : List.of());
        return d;
    }
    private WorkstationDto toWsDto(Workstation w) {
        WorkstationDto d = new WorkstationDto(); d.setId(w.getId()); d.setName(w.getName()); d.setType(w.getType());
        d.setTargetCadence(w.getTargetCadence()); d.setVersatilityTarget(w.getVersatilityTarget()); d.setTargetIluLevel(w.getTargetIluLevel());
        if (w.getZone() != null) { d.setZoneId(w.getZone().getId()); d.setZoneName(w.getZone().getName()); }
        if (w.getCreatedBy() != null) d.setCreatedByName(w.getCreatedBy().getName());
        return d;
    }
    private ProjectMemberDto toMemberDto(ProjectMember m) {
        return new ProjectMemberDto(m.getId(), m.getEmployeeId(), m.getEmployeeName(), m.getProjectRole() != null ? m.getProjectRole().name() : null, m.getProject() != null ? m.getProject().getId() : null);
    }
}