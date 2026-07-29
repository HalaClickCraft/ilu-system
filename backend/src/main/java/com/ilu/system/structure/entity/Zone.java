package com.ilu.system.structure.entity;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "zones")
public class Zone {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @ManyToOne
    @JoinColumn(name = "project_id")
    private Project project;

    @OneToMany(mappedBy = "zone", cascade = CascadeType.ALL)
    private List<Workstation> workstations;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Project getProject() { return project; }
    public void setProject(Project project) { this.project = project; }
    public List<Workstation> getWorkstations() { return workstations; }
    public void setWorkstations(List<Workstation> workstations) { this.workstations = workstations; }
}
