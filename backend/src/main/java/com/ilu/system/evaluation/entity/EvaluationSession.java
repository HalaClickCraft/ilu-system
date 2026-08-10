package com.ilu.system.evaluation.entity;

import com.ilu.system.operator.entity.FormationAssignment;
import com.ilu.system.operator.entity.Operator;
import com.ilu.system.operator.entity.WorkstationFormation;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "evaluation_sessions")
public class EvaluationSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "operator_id", nullable = false)
    private Operator operator;

    @ManyToOne
    @JoinColumn(name = "formation_id")
    private FormationAssignment formation;

    @ManyToOne
    @JoinColumn(name = "workstation_formation_id")
    private WorkstationFormation practicalFormation;

    @ManyToOne
    @JoinColumn(name = "template_id", nullable = false)
    private EvaluationTemplate template;

    @Column(name = "evaluator_id")
    private Long evaluatorId;

    @Column(name = "evaluator_name")
    private String evaluatorName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SessionStatus status = SessionStatus.IN_PROGRESS;

    // Generic part scores (HSE + Quality)
    @Column(name = "generic_total")
    private Integer genericTotal = 0;

    @Column(name = "generic_correct")
    private Integer genericCorrect = 0;

    @Column(name = "generic_percentage")
    private Double genericPercentage = 0.0;

    // Production part scores
    @Column(name = "production_total")
    private Integer productionTotal = 0;

    @Column(name = "production_correct")
    private Integer productionCorrect = 0;

    @Column(name = "production_percentage")
    private Double productionPercentage = 0.0;

    // Overall
    @Column(name = "total_questions")
    private Integer totalQuestions = 0;

    @Column(name = "correct_answers")
    private Integer correctAnswers = 0;

    @Column(name = "score_percentage")
    private Double scorePercentage = 0.0;

    private String decision;

    @Column(name = "niveau")
    private String niveau;

    @Column(name = "operator_seniority_months")
    private Long operatorSeniorityMonths;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @OneToMany(mappedBy = "session", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<EvaluationAnswer> answers;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public enum SessionStatus {
        IN_PROGRESS, COMPLETED, PASSED, FAILED, BLOCKED
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Operator getOperator() { return operator; }
    public void setOperator(Operator operator) { this.operator = operator; }
    public FormationAssignment getFormation() { return formation; }
    public void setFormation(FormationAssignment formation) { this.formation = formation; }
    public WorkstationFormation getPracticalFormation() { return practicalFormation; }
    public void setPracticalFormation(WorkstationFormation practicalFormation) { this.practicalFormation = practicalFormation; }
    public EvaluationTemplate getTemplate() { return template; }
    public void setTemplate(EvaluationTemplate template) { this.template = template; }
    public Long getEvaluatorId() { return evaluatorId; }
    public void setEvaluatorId(Long evaluatorId) { this.evaluatorId = evaluatorId; }
    public String getEvaluatorName() { return evaluatorName; }
    public void setEvaluatorName(String evaluatorName) { this.evaluatorName = evaluatorName; }
    public SessionStatus getStatus() { return status; }
    public void setStatus(SessionStatus status) { this.status = status; }
    public Integer getGenericTotal() { return genericTotal; }
    public void setGenericTotal(Integer genericTotal) { this.genericTotal = genericTotal; }
    public Integer getGenericCorrect() { return genericCorrect; }
    public void setGenericCorrect(Integer genericCorrect) { this.genericCorrect = genericCorrect; }
    public Double getGenericPercentage() { return genericPercentage; }
    public void setGenericPercentage(Double genericPercentage) { this.genericPercentage = genericPercentage; }
    public Integer getProductionTotal() { return productionTotal; }
    public void setProductionTotal(Integer productionTotal) { this.productionTotal = productionTotal; }
    public Integer getProductionCorrect() { return productionCorrect; }
    public void setProductionCorrect(Integer productionCorrect) { this.productionCorrect = productionCorrect; }
    public Double getProductionPercentage() { return productionPercentage; }
    public void setProductionPercentage(Double productionPercentage) { this.productionPercentage = productionPercentage; }
    public Integer getTotalQuestions() { return totalQuestions; }
    public void setTotalQuestions(Integer totalQuestions) { this.totalQuestions = totalQuestions; }
    public Integer getCorrectAnswers() { return correctAnswers; }
    public void setCorrectAnswers(Integer correctAnswers) { this.correctAnswers = correctAnswers; }
    public Double getScorePercentage() { return scorePercentage; }
    public void setScorePercentage(Double scorePercentage) { this.scorePercentage = scorePercentage; }
    public String getDecision() { return decision; }
    public void setDecision(String decision) { this.decision = decision; }
    public String getNiveau() { return niveau; }
    public void setNiveau(String niveau) { this.niveau = niveau; }
    public Long getOperatorSeniorityMonths() { return operatorSeniorityMonths; }
    public void setOperatorSeniorityMonths(Long operatorSeniorityMonths) { this.operatorSeniorityMonths = operatorSeniorityMonths; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }
    public List<EvaluationAnswer> getAnswers() { return answers; }
    public void setAnswers(List<EvaluationAnswer> answers) { this.answers = answers; }
}