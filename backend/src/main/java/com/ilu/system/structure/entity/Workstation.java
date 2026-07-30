package com.ilu.system.structure.entity;

import com.ilu.system.auth.entity.User;
import com.ilu.system.operator.entity.WorkstationFormation;
import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "workstations")
public class Workstation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column
    private String type;

    @Column(name = "target_cadence")
    private Integer targetCadence;

    @Column(name = "versatility_target")
    private Integer versatilityTarget;

    @Column(name = "target_ilu_level")
    private Integer targetIluLevel;

    @ManyToOne
    @JoinColumn(name = "zone_id")
    private Zone zone;

    @ManyToOne
    @JoinColumn(name = "created_by")
    private User createdBy;

    @OneToMany(mappedBy = "workstation", cascade = CascadeType.ALL)
    private List<WorkstationFormation> formations;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public Integer getTargetCadence() { return targetCadence; }
    public void setTargetCadence(Integer targetCadence) { this.targetCadence = targetCadence; }
    public Integer getVersatilityTarget() { return versatilityTarget; }
    public void setVersatilityTarget(Integer versatilityTarget) { this.versatilityTarget = versatilityTarget; }
    public Integer getTargetIluLevel() { return targetIluLevel; }
    public void setTargetIluLevel(Integer targetIluLevel) { this.targetIluLevel = targetIluLevel; }
    public Zone getZone() { return zone; }
    public void setZone(Zone zone) { this.zone = zone; }
    public User getCreatedBy() { return createdBy; }
    public void setCreatedBy(User createdBy) { this.createdBy = createdBy; }
    public List<WorkstationFormation> getFormations() { return formations; }
    public void setFormations(List<WorkstationFormation> formations) { this.formations = formations; }
}