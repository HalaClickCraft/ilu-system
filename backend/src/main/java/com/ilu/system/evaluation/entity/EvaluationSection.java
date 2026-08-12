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
    @JoinColumn(name = "template_id", nullable = false)
    private EvaluationTemplate template;

    @Column(nullable = false)
    private String title;

    @Column(name = "display_order")
    private Integer displayOrder = 0;

    @OneToMany(mappedBy = "section", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("questionNumber ASC")
    private List<EvaluationQuestion> questions;

    
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public EvaluationTemplate getTemplate() { return template; }
    public void setTemplate(EvaluationTemplate template) { this.template = template; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public Integer getDisplayOrder() { return displayOrder; }
    public void setDisplayOrder(Integer displayOrder) { this.displayOrder = displayOrder; }
    public List<EvaluationQuestion> getQuestions() { return questions; }
    public void setQuestions(List<EvaluationQuestion> questions) { this.questions = questions; }
}