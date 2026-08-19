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

    // FIX: Added @JsonIgnore to prevent infinite JSON serialization loop
    // Team -> operators -> each Operator -> team -> Team -> operators -> ...
    @JsonIgnore
    @OneToMany(mappedBy = "team", cascade = CascadeType.ALL)
    private List<Operator> operators;

    // FIX: Added @JsonIgnore to prevent potential loop through
    // Team -> projects -> Project -> zones -> Zone -> project -> ...
    @JsonIgnore
    @ManyToMany
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
    public List<Operator> getOperators() { return operators; }
    public void setOperators(List<Operator> operators) { this.operators = operators; }
    public Set<Project> getProjects() { return projects; }
    public void setProjects(Set<Project> projects) { this.projects = projects; }
}
