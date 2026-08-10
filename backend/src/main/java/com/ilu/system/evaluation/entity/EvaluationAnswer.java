package com.ilu.system.evaluation.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "evaluation_answers")
public class EvaluationAnswer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "session_id", nullable = false)
    private EvaluationSession session;

    @ManyToOne
    @JoinColumn(name = "question_id", nullable = false)
    private EvaluationQuestion question;

    @Column(nullable = false)
    private Integer answer; // 1 = correct, 0 = incorrect

    @Column(columnDefinition = "TEXT")
    private String comment;

    @Column(name = "answered_by_id")
    private Long answeredById;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public EvaluationSession getSession() { return session; }
    public void setSession(EvaluationSession session) { this.session = session; }
    public EvaluationQuestion getQuestion() { return question; }
    public void setQuestion(EvaluationQuestion question) { this.question = question; }
    public Integer getAnswer() { return answer; }
    public void setAnswer(Integer answer) { this.answer = answer; }
    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
    public Long getAnsweredById() { return answeredById; }
    public void setAnsweredById(Long answeredById) { this.answeredById = answeredById; }
}