package com.ilu.system.operator.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "operator_onboarding", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"operator_id", "module_id"})
})
public class OperatorOnboarding {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "operator_id", nullable = false)
    private Long operatorId;

    @Column(name = "module_id", nullable = false)
    private Long moduleId;

    @Column(name = "is_completed", nullable = false)
    private Boolean completed = false;

    @Column(name = "completed_date")
    private LocalDate completedDate;

    @Column(name = "validated_by")
    private String validatedBy;

    @Column(length = 1000)
    private String comment;

    @Column(name = "created_at")
    private LocalDate createdAt;

    public OperatorOnboarding() {
        this.createdAt = LocalDate.now();
    }

    public OperatorOnboarding(Long operatorId, Long moduleId) {
        this.operatorId = operatorId;
        this.moduleId = moduleId;
        this.completed = false;
        this.createdAt = LocalDate.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getOperatorId() { return operatorId; }
    public void setOperatorId(Long operatorId) { this.operatorId = operatorId; }

    public Long getModuleId() { return moduleId; }
    public void setModuleId(Long moduleId) { this.moduleId = moduleId; }

    public Boolean getCompleted() { return completed; }
    public void setCompleted(Boolean completed) { this.completed = completed; }

    public LocalDate getCompletedDate() { return completedDate; }
    public void setCompletedDate(LocalDate completedDate) { this.completedDate = completedDate; }

    public String getValidatedBy() { return validatedBy; }
    public void setValidatedBy(String validatedBy) { this.validatedBy = validatedBy; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    public LocalDate getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDate createdAt) { this.createdAt = createdAt; }
}