package com.ilu.system.operator.dto;
public class CreateOperatorRequest {
    private String employeeId;
    private String lastName;
    private String firstName;
    private String role;
    private String hireDate;
    private Long teamId;
    public String getEmployeeId() { return employeeId; } public void setEmployeeId(String v) { this.employeeId = v; }
    public String getLastName() { return lastName; } public void setLastName(String v) { this.lastName = v; }
    public String getFirstName() { return firstName; } public void setFirstName(String v) { this.firstName = v; }
    public String getRole() { return role; } public void setRole(String v) { this.role = v; }
    public String getHireDate() { return hireDate; } public void setHireDate(String v) { this.hireDate = v; }
    public Long getTeamId() { return teamId; } public void setTeamId(Long v) { this.teamId = v; }
}
