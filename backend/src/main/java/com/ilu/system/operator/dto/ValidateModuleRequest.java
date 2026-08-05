package com.ilu.system.operator.dto;

public class ValidateModuleRequest {

    private Boolean completed;
    private String comment;

    public Boolean getCompleted() { return completed; }
    public void setCompleted(Boolean completed) { this.completed = completed; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
}