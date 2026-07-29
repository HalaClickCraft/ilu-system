package com.ilu.system.auth.dto;
public class LoginRequest {
    private String employeeId;
    private String password;
    public String getEmployeeId() { return employeeId; } public void setEmployeeId(String v) { this.employeeId = v; }
    public String getPassword() { return password; } public void setPassword(String v) { this.password = v; }
}
