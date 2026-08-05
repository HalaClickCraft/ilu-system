package com.ilu.system.operator.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(
    name = "daily_formation_tracking",
    uniqueConstraints = @UniqueConstraint(columnNames = {"formation_id", "day_number"})
)
public class DailyFormationTracking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "formation_id", nullable = false)
    private WorkstationFormation formation;

    @Column(name = "tracking_date", nullable = false)
    private LocalDate trackingDate;

    @Column(name = "day_number")
    private Integer dayNumber;

    @Column(name = "daily_level")
    private Integer dailyLevel;

    @Column
    private String comment;

    @Column
    private String supervisor;

    @Column
    private Integer objectif;

    @Column
    private Integer cadence;

    @Column
    private Integer defauts;

    @Column(name = "cadence_submitted_by")
    private String cadenceSubmittedBy;

    @Column(name = "defects_submitted_by")
    private String defectsSubmittedBy;

    // --- Aliases used by TrainingService ---
    public Integer getActualCadence() { return cadence; }
    public void setActualCadence(Integer cadence) { this.cadence = cadence; }

    public Integer getDefects() { return defauts; }
    public void setDefects(Integer defauts) { this.defauts = defauts; }

    // --- Standard getters/setters ---

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public WorkstationFormation getFormation() { return formation; }
    public void setFormation(WorkstationFormation formation) { this.formation = formation; }
    public LocalDate getTrackingDate() { return trackingDate; }
    public void setTrackingDate(LocalDate trackingDate) { this.trackingDate = trackingDate; }
    public Integer getDayNumber() { return dayNumber; }
    public void setDayNumber(Integer dayNumber) { this.dayNumber = dayNumber; }
    public Integer getDailyLevel() { return dailyLevel; }
    public void setDailyLevel(Integer dailyLevel) { this.dailyLevel = dailyLevel; }
    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
    public String getSupervisor() { return supervisor; }
    public void setSupervisor(String supervisor) { this.supervisor = supervisor; }
    public Integer getObjectif() { return objectif; }
    public void setObjectif(Integer objectif) { this.objectif = objectif; }
    public Integer getCadence() { return cadence; }
    public void setCadence(Integer cadence) { this.cadence = cadence; }
    public Integer getDefauts() { return defauts; }
    public void setDefauts(Integer defauts) { this.defauts = defauts; }
    public String getCadenceSubmittedBy() { return cadenceSubmittedBy; }
    public void setCadenceSubmittedBy(String cadenceSubmittedBy) { this.cadenceSubmittedBy = cadenceSubmittedBy; }
    public String getDefectsSubmittedBy() { return defectsSubmittedBy; }
    public void setDefectsSubmittedBy(String defectsSubmittedBy) { this.defectsSubmittedBy = defectsSubmittedBy; }
}