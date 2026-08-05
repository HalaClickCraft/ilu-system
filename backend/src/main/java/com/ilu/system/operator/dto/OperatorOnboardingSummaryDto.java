package com.ilu.system.operator.dto;

import java.util.Map;

public class OperatorOnboardingSummaryDto {

    private Long operatorId;
    private String firstName;
    private String lastName;
    private String matricule;
    private long totalModules;
    private long completedModules;
    private double completionPercentage;
    private boolean onboardingComplete;
    private Map<String, DepartmentProgress> departmentProgress;

    public static class DepartmentProgress {
        private long total;
        private long completed;
        private double percentage;
        private boolean departmentComplete;

        public DepartmentProgress() {}

        public DepartmentProgress(long total, long completed) {
            this.total = total;
            this.completed = completed;
            this.percentage = total > 0 ? Math.round((double) completed / total * 100.0 * 10.0) / 10.0 : 0.0;
            this.departmentComplete = completed == total;
        }

        public long getTotal() { return total; }
        public void setTotal(long total) { this.total = total; }

        public long getCompleted() { return completed; }
        public void setCompleted(long completed) { this.completed = completed; }

        public double getPercentage() { return percentage; }
        public void setPercentage(double percentage) { this.percentage = percentage; }

        public boolean isDepartmentComplete() { return departmentComplete; }
        public void setDepartmentComplete(boolean departmentComplete) { this.departmentComplete = departmentComplete; }
    }

    public Long getOperatorId() { return operatorId; }
    public void setOperatorId(Long operatorId) { this.operatorId = operatorId; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getMatricule() { return matricule; }
    public void setMatricule(String matricule) { this.matricule = matricule; }

    public long getTotalModules() { return totalModules; }
    public void setTotalModules(long totalModules) { this.totalModules = totalModules; }

    public long getCompletedModules() { return completedModules; }
    public void setCompletedModules(long completedModules) { this.completedModules = completedModules; }

    public double getCompletionPercentage() { return completionPercentage; }
    public void setCompletionPercentage(double completionPercentage) { this.completionPercentage = completionPercentage; }

    public boolean isOnboardingComplete() { return onboardingComplete; }
    public void setOnboardingComplete(boolean onboardingComplete) { this.onboardingComplete = onboardingComplete; }

    public Map<String, DepartmentProgress> getDepartmentProgress() { return departmentProgress; }
    public void setDepartmentProgress(Map<String, DepartmentProgress> departmentProgress) { this.departmentProgress = departmentProgress; }
}