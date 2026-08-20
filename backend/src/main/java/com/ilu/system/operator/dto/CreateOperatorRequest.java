package com.ilu.system.operator.dto;
public class CreateOperatorRequest {
    private String employeeId;
    private String lastName;
    private String firstName;
    private String role;
    private String hireDate;
    private String exitDate;
    private String absenceReason;
    private Long teamId;
    private Long projectId;
    private Long zoneId;
    private String operatorType;
    public String getEmployeeId() { return employeeId; } public void setEmployeeId(String v) { this.employeeId = v; }
    public String getLastName() { return lastName; } public void setLastName(String v) { this.lastName = v; }
    public String getFirstName() { return firstName; } public void setFirstName(String v) { this.firstName = v; }
    public String getRole() { return role; } public void setRole(String v) { this.role = v; }
    public String getHireDate() { return hireDate; } public void setHireDate(String v) { this.hireDate = v; }
    public String getExitDate() { return exitDate; } public void setExitDate(String v) { this.exitDate = v; }
    public String getAbsenceReason() { return absenceReason; } public void setAbsenceReason(String v) { this.absenceReason = v; }
    public Long getTeamId() { return teamId; } public void setTeamId(Long v) { this.teamId = v; }
    public Long getProjectId() { return projectId; } public void setProjectId(Long v) { this.projectId = v; }
    public Long getZoneId() { return zoneId; } public void setZoneId(Long v) { this.zoneId = v; }
    public String getOperatorType() { return operatorType; } public void setOperatorType(String v) { this.operatorType = v; }
}