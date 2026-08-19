package com.ilu.system.operator.entity;

import com.ilu.system.structure.entity.Workstation;
import jakarta.persistence.*;
import java.time.LocalDate;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "formation_assignments")
public class FormationAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "operator_id", nullable = false)
    private Operator operator;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "workstation_id", nullable = false)
    private Workstation workstation;

    @Column(name = "is_primary_assignment")
    private Boolean isPrimaryAssignment = false;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(nullable = false)
    private String status;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Operator getOperator() { return operator; }
    public void setOperator(Operator operator) { this.operator = operator; }
    public Workstation getWorkstation() { return workstation; }
    public void setWorkstation(Workstation workstation) { this.workstation = workstation; }
    public Boolean getIsPrimaryAssignment() { return isPrimaryAssignment; }
    public void setIsPrimaryAssignment(Boolean isPrimaryAssignment) { this.isPrimaryAssignment = isPrimaryAssignment; }
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
