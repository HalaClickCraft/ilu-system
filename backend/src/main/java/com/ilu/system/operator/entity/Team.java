package com.ilu.system.operator.entity;

import jakarta.persistence.*;
import java.util.List;

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

    @OneToMany(mappedBy = "team", cascade = CascadeType.ALL)
    private List<Operator> operators;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getTeamLeader() { return teamLeader; }
    public void setTeamLeader(String teamLeader) { this.teamLeader = teamLeader; }
    public List<Operator> getOperators() { return operators; }
    public void setOperators(List<Operator> operators) { this.operators = operators; }
}
