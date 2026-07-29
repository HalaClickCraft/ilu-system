package com.ilu.system.operator.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "operators")
public class Operator {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String employeeId;

    @Column(nullable = false)
    private String lastName;

    @Column
    private String firstName;

    @Column
    private String role;

    @Column(name = "hire_date")
    private LocalDate hireDate;

    @Column(name = "exit_date")
    private LocalDate exitDate;

    @Column(name = "absence_reason")
    private String absenceReason;

    @Column(nullable = false)
    private Boolean active = true;

    @ManyToOne
    @JoinColumn(name = "team_id")
    private Team team;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getEmployeeId() { return employeeId; }
    public void setEmployeeId(String employeeId) { this.employeeId = employeeId; }
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public LocalDate getHireDate() { return hireDate; }
    public void setHireDate(LocalDate hireDate) { this.hireDate = hireDate; }
    public LocalDate getExitDate() { return exitDate; }
    public void setExitDate(LocalDate exitDate) { this.exitDate = exitDate; }
    public String getAbsenceReason() { return absenceReason; }
    public void setAbsenceReason(String absenceReason) { this.absenceReason = absenceReason; }
    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }
    public Team getTeam() { return team; }
    public void setTeam(Team team) { this.team = team; }
}
