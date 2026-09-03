package com.ilu.system.structure.service;

import com.ilu.system.structure.dto.*;
import com.ilu.system.structure.entity.*;
import com.ilu.system.structure.repository.*;
import com.ilu.system.auth.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

import com.ilu.system.operator.repository.OperatorRepository;
import com.ilu.system.operator.repository.TeamRepository;
import com.ilu.system.operator.repository.ProjectTransferRequestRepository;
import com.ilu.system.operator.entity.Operator;
import com.ilu.system.operator.entity.Team;
import com.ilu.system.operator.entity.ProjectTransferRequest;

@Service
public class StructureService {
    private final ProjectRepository projectRepo;
    private final ZoneRepository zoneRepo;
    private final WorkstationRepository wsRepo;
    private final UserRepository userRepo;
    private final OperatorRepository operatorRepo;
    private final TeamRepository teamRepo;
    private final ProjectTransferRequestRepository transferRepo;

    @Autowired
    public StructureService(ProjectRepository projectRepo, ZoneRepository zoneRepo, WorkstationRepository wsRepo, UserRepository userRepo,
                            OperatorRepository operatorRepo, TeamRepository teamRepo, ProjectTransferRequestRepository transferRepo) {
        this.projectRepo = projectRepo; this.zoneRepo = zoneRepo; this.wsRepo = wsRepo; this.userRepo = userRepo;
        this.operatorRepo = operatorRepo; this.teamRepo = teamRepo; this.transferRepo = transferRepo;
    }

    public StructureService(ProjectRepository projectRepo, ZoneRepository zoneRepo, WorkstationRepository wsRepo, UserRepository userRepo) {
        this(projectRepo, zoneRepo, wsRepo, userRepo, null, null, null);
    }

    @Transactional
    public ProjectDto createProject(CreateProjectRequest req) {
        if (projectRepo.existsByName(req.getName())) throw new RuntimeException("Project already exists");
        Project p = new Project(); p.setName(req.getName()); p = projectRepo.save(p);
        return toDto(p);
    }

    public List<ProjectDto> listProjects() { return projectRepo.findAll().stream().map(this::toDto).collect(Collectors.toList()); }
    public ProjectDto getProject(Long id) { return toDto(projectRepo.findById(id).orElseThrow(() -> new RuntimeException("Project not found"))); }
    @Transactional
    public ProjectDto updateProject(Long id, CreateProjectRequest req) {
        Project p = projectRepo.findById(id).orElseThrow(() -> new RuntimeException("Project not found"));
        if (req.getName() != null) p.setName(req.getName()); return toDto(projectRepo.save(p));
    }
    @jakarta.persistence.PersistenceContext
    private jakarta.persistence.EntityManager entityManager;

    @Transactional
    public void deleteProject(Long id) {
        Project project = projectRepo.findById(id).orElse(null);
        if (project == null) return;

        // Dissociate operators from this project and its zones
        if (operatorRepo != null) {
            List<Operator> ops = operatorRepo.findByProjectId(id);
            for (Operator op : ops) {
                op.setProject(null);
                op.setZone(null);
                operatorRepo.save(op);
            }
        }

        // Dissociate teams from this project
        if (teamRepo != null) {
            List<Team> teams = teamRepo.findAll();
            for (Team team : teams) {
                boolean modified = false;
                if (team.getProject() != null && team.getProject().getId().equals(id)) {
                    team.setProject(null);
                    modified = true;
                }
                if (team.getProjects() != null && team.getProjects().removeIf(p -> p.getId().equals(id))) {
                    modified = true;
                }
                if (modified) {
                    teamRepo.save(team);
                }
            }
        }

        // Delete transfer requests associated with this project
        if (transferRepo != null) {
            List<ProjectTransferRequest> transfers = transferRepo.findAll().stream()
                .filter(t -> (t.getSourceProjectId() != null && t.getSourceProjectId().equals(id))
                          || (t.getTargetProjectId() != null && t.getTargetProjectId().equals(id)))
                .collect(Collectors.toList());
            transferRepo.deleteAll(transfers);
        }

        // Native cleanup for any join tables with foreign key to project (e.g. project_members, team_projects)
        if (entityManager != null) {
            try {
                entityManager.createNativeQuery("DELETE FROM project_members WHERE project_id = :pId")
                    .setParameter("pId", id).executeUpdate();
            } catch (Exception ignored) {}

            try {
                entityManager.createNativeQuery("DELETE FROM team_projects WHERE project_id = :pId")
                    .setParameter("pId", id).executeUpdate();
            } catch (Exception ignored) {}
        }

        // Clean up workstations and zones
        if (project.getZones() != null) {
            for (Zone z : project.getZones()) {
                if (z.getWorkstations() != null && !z.getWorkstations().isEmpty()) {
                    wsRepo.deleteAll(z.getWorkstations());
                }
            }
            zoneRepo.deleteAll(project.getZones());
        }

        projectRepo.delete(project);
    }

    @Transactional
    public ZoneDto createZone(String name, Long projectId) {
        Project p = projectRepo.findById(projectId).orElseThrow(() -> new RuntimeException("Project not found"));
        Zone z = new Zone(); z.setName(name); z.setProject(p); return toZoneDto(zoneRepo.save(z));
    }
    public List<ZoneDto> listZones(Long projectId) { return zoneRepo.findByProjectId(projectId).stream().map(this::toZoneDto).collect(Collectors.toList()); }
    @Transactional
    public void deleteZone(Long id) {
        Zone zone = zoneRepo.findById(id).orElse(null);
        if (zone == null) return;

        // Dissociate operators from this zone
        if (operatorRepo != null) {
            List<Operator> ops = operatorRepo.findByZoneId(id);
            for (Operator op : ops) {
                op.setZone(null);
                operatorRepo.save(op);
            }
        }

        // Clean up workstations in this zone
        if (zone.getWorkstations() != null && !zone.getWorkstations().isEmpty()) {
            for (Workstation ws : zone.getWorkstations()) {
                deleteWorkstationInternal(ws.getId());
            }
        }

        zoneRepo.delete(zone);
    }

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
    @Transactional
    public void deleteWorkstation(Long id) {
        deleteWorkstationInternal(id);
    }

    private void deleteWorkstationInternal(Long id) {
        Workstation ws = wsRepo.findById(id).orElse(null);
        if (ws == null) return;
        wsRepo.delete(ws);
    }

    private ProjectDto toDto(Project p) {
        ProjectDto d = new ProjectDto(); d.setId(p.getId()); d.setName(p.getName());
        d.setZones(p.getZones() != null ? p.getZones().stream().map(this::toZoneDto).collect(Collectors.toList()) : List.of());
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
}