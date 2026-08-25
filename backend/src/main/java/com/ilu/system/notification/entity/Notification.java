package com.ilu.system.notification.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notification")
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "recipient_id", nullable = false)
    private Long recipientId;

    @Enumerated(EnumType.STRING)
    @Column(name = "recipient_type", nullable = false)
    private RecipientType recipientType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType type;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String message;

    @Column(name = "related_planning_id")
    private Long relatedPlanningId;

    @Column(name = "related_operator_id")
    private Long relatedOperatorId;

    @Column(name = "is_read", nullable = false)
    private Boolean read = false;

    @Column(name = "email_sent", nullable = false)
    private Boolean emailSent = false;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public enum RecipientType {
        CHEF_EQUIPE,
        HR
    }

    public enum NotificationType {
        RECYCLAGE_30J,
        RECYCLAGE_20J,
        RECYCLAGE_15J,
        RECYCLAGE_10J,
        RECYCLAGE_DEMARRE,
        ABSENCE_DEBUT,
        ABSENCE_REPRISE,
        DEPART
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getRecipientId() {
        return recipientId;
    }

    public void setRecipientId(Long recipientId) {
        this.recipientId = recipientId;
    }

    public RecipientType getRecipientType() {
        return recipientType;
    }

    public void setRecipientType(RecipientType recipientType) {
        this.recipientType = recipientType;
    }

    public NotificationType getType() {
        return type;
    }

    public void setType(NotificationType type) {
        this.type = type;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Long getRelatedPlanningId() {
        return relatedPlanningId;
    }

    public void setRelatedPlanningId(Long relatedPlanningId) {
        this.relatedPlanningId = relatedPlanningId;
    }

    public Long getRelatedOperatorId() {
        return relatedOperatorId;
    }

    public void setRelatedOperatorId(Long relatedOperatorId) {
        this.relatedOperatorId = relatedOperatorId;
    }

    public Boolean getRead() {
        return read;
    }

    public void setRead(Boolean read) {
        this.read = read;
    }

    public Boolean getEmailSent() {
        return emailSent;
    }

    public void setEmailSent(Boolean emailSent) {
        this.emailSent = emailSent;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}