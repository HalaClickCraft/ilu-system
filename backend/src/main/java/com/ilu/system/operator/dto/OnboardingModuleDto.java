package com.ilu.system.operator.dto;

public class OnboardingModuleDto {

    private Long id;
    private String name;
    private String department;
    private Integer displayOrder;
    private String description;
    private Boolean completed = false;
    private String completedDate;
    private String validatedBy;
    private String comment;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public Integer getDisplayOrder() { return displayOrder; }
    public void setDisplayOrder(Integer displayOrder) { this.displayOrder = displayOrder; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Boolean getCompleted() { return completed; }
    public void setCompleted(Boolean completed) { this.completed = completed; }

    public String getCompletedDate() { return completedDate; }
    public void setCompletedDate(String completedDate) { this.completedDate = completedDate; }

    public String getValidatedBy() { return validatedBy; }
    public void setValidatedBy(String validatedBy) { this.validatedBy = validatedBy; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
}