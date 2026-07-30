package com.ilu.system.operator.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "daily_formation_tracking")
public class DailyFormationTracking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "formation_id", nullable = false)
    private WorkstationFormation formation;

    @Column(name = "tracking_date", nullable = false)
    private LocalDate trackingDate;

    @Column(name = "daily_level")
    private String dailyLevel;

    @Column
    private Integer objectif;

    @Column
    private Integer cadence;

    @Column
    private Integer defauts;

    @Column
    private String comment;

    @Column
    private String supervisor;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public WorkstationFormation getFormation() { return formation; }
    public void setFormation(WorkstationFormation formation) { this.formation = formation; }
    public LocalDate getTrackingDate() { return trackingDate; }
    public void setTrackingDate(LocalDate trackingDate) { this.trackingDate = trackingDate; }
    public String getDailyLevel() { return dailyLevel; }
    public void setDailyLevel(String dailyLevel) { this.dailyLevel = dailyLevel; }
    public Integer getObjectif() { return objectif; }
    public void setObjectif(Integer objectif) { this.objectif = objectif; }
    public Integer getCadence() { return cadence; }
    public void setCadence(Integer cadence) { this.cadence = cadence; }
    public Integer getDefauts() { return defauts; }
    public void setDefauts(Integer defauts) { this.defauts = defauts; }
    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
    public String getSupervisor() { return supervisor; }
    public void setSupervisor(String supervisor) { this.supervisor = supervisor; }
}