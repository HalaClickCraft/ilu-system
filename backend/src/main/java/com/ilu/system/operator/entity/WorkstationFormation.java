package com.ilu.system.operator.entity;

import com.ilu.system.structure.entity.Workstation;
import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "workstation_formations")
public class WorkstationFormation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "operator_id", nullable = false)
    private Operator operator;

    @ManyToOne
    @JoinColumn(name = "workstation_id", nullable = false)
    private Workstation workstation;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(nullable = false)
    private String status;

    @Column(name = "achieved_level")
    private Integer achievedLevel;

    @Column(name = "target_level")
    private Integer targetLevel;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Operator getOperator() { return operator; }
    public void setOperator(Operator operator) { this.operator = operator; }
    public Workstation getWorkstation() { return workstation; }
    public void setWorkstation(Workstation workstation) { this.workstation = workstation; }
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Integer getAchievedLevel() { return achievedLevel; }
    public void setAchievedLevel(Integer achievedLevel) { this.achievedLevel = achievedLevel; }
    public Integer getTargetLevel() { return targetLevel; }
    public void setTargetLevel(Integer targetLevel) { this.targetLevel = targetLevel; }
}
