package com.ilu.system.operator.dto;

import java.util.List;

public class DepartmentOnboardingStatusDto {

    private String department;
    private long totalModules;
    private long completedModules;
    private double completionPercentage;
    private boolean editable;
    private List<OnboardingModuleDto> modules;

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public long getTotalModules() { return totalModules; }
    public void setTotalModules(long totalModules) { this.totalModules = totalModules; }

    public long getCompletedModules() { return completedModules; }
    public void setCompletedModules(long completedModules) { this.completedModules = completedModules; }

    public double getCompletionPercentage() { return completionPercentage; }
    public void setCompletionPercentage(double completionPercentage) { this.completionPercentage = completionPercentage; }

    public boolean isEditable() { return editable; }
    public void setEditable(boolean editable) { this.editable = editable; }

    public List<OnboardingModuleDto> getModules() { return modules; }
    public void setModules(List<OnboardingModuleDto> modules) { this.modules = modules; }
}