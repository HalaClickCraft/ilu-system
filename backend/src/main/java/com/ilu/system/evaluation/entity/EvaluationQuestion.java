package com.ilu.system.evaluation.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "evaluation_questions")
public class EvaluationQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "template_id", nullable = false)
    private EvaluationTemplate template;

    @ManyToOne
    @JoinColumn(name = "section_id")
    private EvaluationSection section;

    @Column(name = "question_text", nullable = false, columnDefinition = "TEXT")
    private String questionText;

    @Column(name = "expected_answer", columnDefinition = "TEXT")
    private String expectedAnswer;

    @Column(name = "question_number")
    private Integer questionNumber;

    @Column(name = "complementary_questions", columnDefinition = "TEXT")
    private String complementaryQuestions;

    @Column(name = "image_url")
    private String imageUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "validator_role")
    private ValidatorRole validatorRole = ValidatorRole.CHEF_EQUIPE;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private QuestionStatus status = QuestionStatus.PENDING;

    @Column(name = "created_by_id")
    private Long createdById;

    @Column(name = "validated_by_id")
    private Long validatedById;

    @Column(name = "rejection_reason")
    private String rejectionReason;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public enum ValidatorRole {
        CHEF_EQUIPE, AGENT_QUALITE, RESP_HSE, RESP_QUALITE
    }

    public enum QuestionStatus {
        PENDING, VALIDATED, REJECTED
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public EvaluationTemplate getTemplate() { return template; }
    public void setTemplate(EvaluationTemplate template) { this.template = template; }
    public EvaluationSection getSection() { return section; }
    public void setSection(EvaluationSection section) { this.section = section; }
    public String getQuestionText() { return questionText; }
    public void setQuestionText(String questionText) { this.questionText = questionText; }
    public String getExpectedAnswer() { return expectedAnswer; }
    public void setExpectedAnswer(String expectedAnswer) { this.expectedAnswer = expectedAnswer; }
    public Integer getQuestionNumber() { return questionNumber; }
    public void setQuestionNumber(Integer questionNumber) { this.questionNumber = questionNumber; }
    public String getComplementaryQuestions() { return complementaryQuestions; }
    public void setComplementaryQuestions(String complementaryQuestions) { this.complementaryQuestions = complementaryQuestions; }
    public ValidatorRole getValidatorRole() { return validatorRole; }
    public void setValidatorRole(ValidatorRole validatorRole) { this.validatorRole = validatorRole; }
    public QuestionStatus getStatus() { return status; }
    public void setStatus(QuestionStatus status) { this.status = status; }
    public Long getCreatedById() { return createdById; }
    public void setCreatedById(Long createdById) { this.createdById = createdById; }
    public Long getValidatedById() { return validatedById; }
    public void setValidatedById(Long validatedById) { this.validatedById = validatedById; }
    public String getRejectionReason() { return rejectionReason; }
    public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}