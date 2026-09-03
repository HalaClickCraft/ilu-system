package com.ilu.system.structure.entity;

import com.ilu.system.auth.entity.User;
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
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties({"zones", "createdBy"})
    private Project project;

    @OneToMany(mappedBy = "zone", cascade = CascadeType.ALL)
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties({"zone"})
    private List<Workstation> workstations;

    @ManyToOne
    @JoinColumn(name = "created_by")
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties({"password", "roles"})
    private User createdBy;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Project getProject() { return project; }
    public void setProject(Project project) { this.project = project; }
    public List<Workstation> getWorkstations() { return workstations; }
    public void setWorkstations(List<Workstation> workstations) { this.workstations = workstations; }
    public User getCreatedBy() { return createdBy; }
    public void setCreatedBy(User createdBy) { this.createdBy = createdBy; }
}