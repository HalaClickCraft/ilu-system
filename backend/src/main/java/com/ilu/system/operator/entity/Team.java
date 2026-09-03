package com.ilu.system.operator.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.ilu.system.structure.entity.Project;
import jakarta.persistence.*;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "teams")
public class Team {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(name = "team_leader")
    private String teamLeader;

    @Column(name = "team_leader_employee_id")
    private String teamLeaderEmployeeId;

    @Column(name = "agent_qualite")
    private String agentQualite;

    @Column(name = "agent_qualite_employee_id")
    private String agentQualiteEmployeeId;

    @Column(name = "quality_manager")
    private String qualityManager;

    @Column(name = "quality_manager_employee_id")
    private String qualityManagerEmployeeId;

    @Column(name = "project_manager")
    private String projectManager;

    @Column(name = "project_manager_employee_id")
    private String projectManagerEmployeeId;

    @Column(name = "hse_manager")
    private String hseManager;

    @Column(name = "hse_manager_employee_id")
    private String hseManagerEmployeeId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "project_id")
    private Project project;

    // FIX: Added @JsonIgnore to prevent infinite JSON serialization loop
    // Team -> operators -> each Operator -> team -> Team -> operators -> ...
    @JsonIgnore
    @OneToMany(mappedBy = "team", cascade = CascadeType.ALL)
    private List<Operator> operators;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "team_projects",
        joinColumns = @JoinColumn(name = "team_id"),
        inverseJoinColumns = @JoinColumn(name = "project_id")
    )
    private Set<Project> projects;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getTeamLeader() { return teamLeader; }
    public void setTeamLeader(String teamLeader) { this.teamLeader = teamLeader; }
    public String getTeamLeaderEmployeeId() { return teamLeaderEmployeeId; }
    public void setTeamLeaderEmployeeId(String teamLeaderEmployeeId) { this.teamLeaderEmployeeId = teamLeaderEmployeeId; }
    
    public String getAgentQualite() { return agentQualite; }
    public void setAgentQualite(String agentQualite) { this.agentQualite = agentQualite; }
    public String getAgentQualiteEmployeeId() { return agentQualiteEmployeeId; }
    public void setAgentQualiteEmployeeId(String agentQualiteEmployeeId) { this.agentQualiteEmployeeId = agentQualiteEmployeeId; }

    public String getQualityManager() { return qualityManager; }
    public void setQualityManager(String qualityManager) { this.qualityManager = qualityManager; }
    public String getQualityManagerEmployeeId() { return qualityManagerEmployeeId; }
    public void setQualityManagerEmployeeId(String qualityManagerEmployeeId) { this.qualityManagerEmployeeId = qualityManagerEmployeeId; }

    public String getProjectManager() { return projectManager; }
    public void setProjectManager(String projectManager) { this.projectManager = projectManager; }
    public String getProjectManagerEmployeeId() { return projectManagerEmployeeId; }
    public void setProjectManagerEmployeeId(String projectManagerEmployeeId) { this.projectManagerEmployeeId = projectManagerEmployeeId; }

    public String getHseManager() { return hseManager; }
    public void setHseManager(String hseManager) { this.hseManager = hseManager; }
    public String getHseManagerEmployeeId() { return hseManagerEmployeeId; }
    public void setHseManagerEmployeeId(String hseManagerEmployeeId) { this.hseManagerEmployeeId = hseManagerEmployeeId; }

    public Project getProject() { return project; }
    public void setProject(Project project) { this.project = project; }

    public List<Operator> getOperators() { return operators; }
    public void setOperators(List<Operator> operators) { this.operators = operators; }
    public Set<Project> getProjects() { return projects; }
    public void setProjects(Set<Project> projects) { this.projects = projects; }
}
