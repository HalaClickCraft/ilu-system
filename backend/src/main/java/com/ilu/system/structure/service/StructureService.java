package com.ilu.system.structure.service;

import com.ilu.system.structure.dto.*;
import com.ilu.system.structure.entity.*;
import com.ilu.system.structure.repository.*;
import com.ilu.system.auth.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class StructureService {
    private final ProjectRepository projectRepo;
    private final ZoneRepository zoneRepo;
    private final WorkstationRepository wsRepo;
       private final ProjectMemberRepository memberRepo;
    private final UserRepository userRepo;

    public StructureService(ProjectRepository projectRepo, ZoneRepository zoneRepo, WorkstationRepository wsRepo, ProjectMemberRepository memberRepo, UserRepository userRepo) {
        this.projectRepo = projectRepo; this.zoneRepo = zoneRepo; this.wsRepo = wsRepo; this.memberRepo = memberRepo; this.userRepo = userRepo;
    }

    @Transactional
    public ProjectDto createProject(CreateProjectRequest req) {
        if (projectRepo.existsByName(req.getName())) throw new RuntimeException("Project already exists");
        Project p = new Project(); p.setName(req.getName()); p = projectRepo.save(p);
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
        Zone z = new Zone(); z.setName(name); z.setProject(p); return toZoneDto(zoneRepo.save(z));
    }
    public List<ZoneDto> listZones(Long projectId) { return zoneRepo.findByProjectId(projectId).stream().map(this::toZoneDto).collect(Collectors.toList()); }
    @Transactional public void deleteZone(Long id) { zoneRepo.deleteById(id); }

    @Transactional
    public WorkstationDto createWorkstation(WorkstationDto dto) {
        Workstation w = new Workstation(); w.setName(dto.getName()); w.setType(dto.getType());
        w.setTargetCadence(dto.getTargetCadence()); w.setVersatilityTarget(dto.getVersatilityTarget()); w.setTargetIluLevel(dto.getTargetIluLevel());
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
        d.setZones(p.getZones() != null ? p.getZones().stream().map(this::toZoneDto).collect(Collectors.toList()) : List.of());
        d.setMembers(p.getMembers() != null ? p.getMembers().stream().map(this::toMemberDto).collect(Collectors.toList()) : List.of());
        return d;
    }
        public List<java.util.Map<String, Object>> listAvailableUsers() {
        return userRepo.findAll().stream()
            .filter(u -> u.getActive() != null && u.getActive())
            .filter(u -> u.getRoles().stream().noneMatch(r -> r.getLabel().equals("ADMIN") || r.getLabel().equals("RH")))
            .map(u -> {
                var map = new java.util.LinkedHashMap<String, Object>();
                map.put("employeeId", u.getEmployeeId());
                map.put("name", u.getName());
                map.put("roles", u.getRoles().stream().map(r -> r.getLabel()).collect(Collectors.toList()));
                return map;
            }).collect(Collectors.toList());
    }
    private ZoneDto toZoneDto(Zone z) {
        ZoneDto d = new ZoneDto(); d.setId(z.getId()); d.setName(z.getName());
        if (z.getProject() != null) { d.setProjectId(z.getProject().getId()); d.setProjectName(z.getProject().getName()); }
        d.setWorkstations(z.getWorkstations() != null ? z.getWorkstations().stream().map(this::toWsDto).collect(Collectors.toList()) : List.of());
        return d;
    }
    private WorkstationDto toWsDto(Workstation w) {
        WorkstationDto d = new WorkstationDto(); d.setId(w.getId()); d.setName(w.getName()); d.setType(w.getType());
        d.setTargetCadence(w.getTargetCadence()); d.setVersatilityTarget(w.getVersatilityTarget()); d.setTargetIluLevel(w.getTargetIluLevel());
        if (w.getZone() != null) { d.setZoneId(w.getZone().getId()); d.setZoneName(w.getZone().getName()); }
        return d;
    }
    private ProjectMemberDto toMemberDto(ProjectMember m) {
        return new ProjectMemberDto(m.getId(), m.getEmployeeId(), m.getEmployeeName(), m.getProjectRole() != null ? m.getProjectRole().name() : null, m.getProject() != null ? m.getProject().getId() : null);
    }
}