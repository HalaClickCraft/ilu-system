package com.ilu.system.operator.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "project_transfer_requests")
public class ProjectTransferRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "employee_id", nullable = false)
    private String employeeId;

    @Column(name = "operator_name")
    private String operatorName;

    @Column(name = "source_project_id")
    private Long sourceProjectId;

    @Column(name = "source_project_name")
    private String sourceProjectName;

    @Column(name = "target_project_id", nullable = false)
    private Long targetProjectId;

    @Column(name = "target_project_name")
    private String targetProjectName;

    @Column(name = "requested_by", nullable = false)
    private String requestedBy;

    @Column(nullable = false)
    private String status = "PENDING"; // PENDING, APPROVED, REJECTED

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "approved_by")
    private String approvedBy;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "target_team_id")
    private Long targetTeamId;

    @Column(name = "target_team_name")
    private String targetTeamName;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getEmployeeId() { return employeeId; }
    public void setEmployeeId(String employeeId) { this.employeeId = employeeId; }
    public String getOperatorName() { return operatorName; }
    public void setOperatorName(String operatorName) { this.operatorName = operatorName; }
    public Long getSourceProjectId() { return sourceProjectId; }
    public void setSourceProjectId(Long sourceProjectId) { this.sourceProjectId = sourceProjectId; }
    public String getSourceProjectName() { return sourceProjectName; }
    public void setSourceProjectName(String sourceProjectName) { this.sourceProjectName = sourceProjectName; }
    public Long getTargetProjectId() { return targetProjectId; }
    public void setTargetProjectId(Long targetProjectId) { this.targetProjectId = targetProjectId; }
    public String getTargetProjectName() { return targetProjectName; }
    public void setTargetProjectName(String targetProjectName) { this.targetProjectName = targetProjectName; }
    public String getRequestedBy() { return requestedBy; }
    public void setRequestedBy(String requestedBy) { this.requestedBy = requestedBy; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public String getApprovedBy() { return approvedBy; }
    public void setApprovedBy(String approvedBy) { this.approvedBy = approvedBy; }
    public LocalDateTime getApprovedAt() { return approvedAt; }
    public void setApprovedAt(LocalDateTime approvedAt) { this.approvedAt = approvedAt; }
    public Long getTargetTeamId() { return targetTeamId; }
    public void setTargetTeamId(Long targetTeamId) { this.targetTeamId = targetTeamId; }
    public String getTargetTeamName() { return targetTeamName; }
    public void setTargetTeamName(String targetTeamName) { this.targetTeamName = targetTeamName; }
}
