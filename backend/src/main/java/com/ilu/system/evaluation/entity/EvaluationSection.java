package com.ilu.system.evaluation.entity;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "evaluation_sections")
public class EvaluationSection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "template_id")
    private EvaluationTemplate template;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "display_order")
    private Integer displayOrder = 0;

    @Enumerated(EnumType.STRING)
    @Column(name = "responsible_role")
    private EvaluationQuestion.ValidatorRole responsibleRole;

    @Enumerated(EnumType.STRING)
    @Column(name = "section_domain")
    private SectionDomain domain;

    @OneToMany(mappedBy = "section", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("questionNumber ASC")
    private List<EvaluationQuestion> questions;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public EvaluationTemplate getTemplate() { return template; }
    public void setTemplate(EvaluationTemplate template) { this.template = template; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Integer getDisplayOrder() { return displayOrder; }
    public void setDisplayOrder(Integer displayOrder) { this.displayOrder = displayOrder; }
    public EvaluationQuestion.ValidatorRole getResponsibleRole() { return responsibleRole; }
    public void setResponsibleRole(EvaluationQuestion.ValidatorRole responsibleRole) { this.responsibleRole = responsibleRole; }
    public SectionDomain getDomain() { return domain; }
    public void setDomain(SectionDomain domain) { this.domain = domain; }
    public List<EvaluationQuestion> getQuestions() { return questions; }
    public void setQuestions(List<EvaluationQuestion> questions) { this.questions = questions; }

    public enum SectionDomain {
        SECURITY_ENVIRONMENT(EvaluationQuestion.QuestionCategory.SECURITY_ENVIRONMENT, EvaluationQuestion.ValidatorRole.RESP_HSE),
        QUALITY(EvaluationQuestion.QuestionCategory.QUALITY, EvaluationQuestion.ValidatorRole.AGENT_QUALITE),
        FIVE_S(EvaluationQuestion.QuestionCategory.FIVE_S, EvaluationQuestion.ValidatorRole.CHEF_EQUIPE),
        TRACEABILITY(EvaluationQuestion.QuestionCategory.TRACEABILITY, EvaluationQuestion.ValidatorRole.CHEF_EQUIPE),
        PRODUCTION_ALARMS(EvaluationQuestion.QuestionCategory.PRODUCTION_ALARMS, EvaluationQuestion.ValidatorRole.CHEF_EQUIPE),
        PRODUCTION(EvaluationQuestion.QuestionCategory.PRODUCTION, EvaluationQuestion.ValidatorRole.CHEF_EQUIPE),
        ANIMATION(EvaluationQuestion.QuestionCategory.ANIMATION, EvaluationQuestion.ValidatorRole.CHEF_EQUIPE);

        private final EvaluationQuestion.QuestionCategory questionCategory;
        private final EvaluationQuestion.ValidatorRole responsibleRole;

        SectionDomain(EvaluationQuestion.QuestionCategory questionCategory, EvaluationQuestion.ValidatorRole responsibleRole) {
            this.questionCategory = questionCategory;
            this.responsibleRole = responsibleRole;
        }

        public EvaluationQuestion.QuestionCategory getQuestionCategory() { return questionCategory; }
        public EvaluationQuestion.ValidatorRole getResponsibleRole() { return responsibleRole; }
    }
}