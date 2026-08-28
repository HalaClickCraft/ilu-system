package com.ilu.system.recyclage.entity;

import com.ilu.system.operator.entity.Operator;
import com.ilu.system.structure.entity.Workstation;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "recyclage_planning")
public class RecyclagePlanning {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "operator_id", nullable = false)
    private Operator operator;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "workstation_id", nullable = false)
    private Workstation workstation;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PlanningType type;

    @Column(name = "scheduled_date", nullable = false)
    private LocalDate scheduledDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PlanningStatus status = PlanningStatus.PLANIFIEE;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PlanningSource source;

    @Column(name = "project_id")
    private Long projectId;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "niveau_obtenu")
    private String niveauObtenu;

    @Column(name = "evaluation_session_id")
    private Long evaluationSessionId;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public enum PlanningType {
        // Kept for records created before the workflow split.
        INITIALE,
        INITIALE_NOUVELLE_RECRUE,
        EVALUATION_ANNUELLE_MOIS_1,
        RECYCLAGE,
        RECYCLAGE_NOUVELLE_RECRUE,
        EVALUATION_ANNUELLE_MOIS_7
    }

    public enum PlanningStatus {
        PLANIFIEE,
        EN_COURS,
        TERMINEE,
        ANNULEE
    }

    public enum PlanningSource {
        ANNUELLE,
        NOUVELLE_RECRUE,
        REPRISE_ABSENCE,
        CHEF_EQUIPE
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Operator getOperator() {
        return operator;
    }

    public void setOperator(Operator operator) {
        this.operator = operator;
    }

    public Workstation getWorkstation() {
        return workstation;
    }

    public void setWorkstation(Workstation workstation) {
        this.workstation = workstation;
    }

    public PlanningType getType() {
        return type;
    }

    public void setType(PlanningType type) {
        this.type = type;
    }

    public LocalDate getScheduledDate() {
        return scheduledDate;
    }

    public void setScheduledDate(LocalDate scheduledDate) {
        this.scheduledDate = scheduledDate;
    }

    public PlanningStatus getStatus() {
        return status;
    }

    public void setStatus(PlanningStatus status) {
        this.status = status;
    }

    public PlanningSource getSource() {
        return source;
    }

    public void setSource(PlanningSource source) {
        this.source = source;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }

    public String getNiveauObtenu() {
        return niveauObtenu;
    }

    public void setNiveauObtenu(String niveauObtenu) {
        this.niveauObtenu = niveauObtenu;
    }

    public Long getEvaluationSessionId() {
        return evaluationSessionId;
    }

    public void setEvaluationSessionId(Long evaluationSessionId) {
        this.evaluationSessionId = evaluationSessionId;
    }
}
