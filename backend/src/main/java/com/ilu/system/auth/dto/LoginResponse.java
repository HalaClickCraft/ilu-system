package com.ilu.system.auth.dto;
import java.util.Set;
public class LoginResponse {
    private String token;
    private String employeeId;
    private String name;
    private Boolean mustChangePassword;
    private Set<String> roles;
    public String getToken() { return token; } public void setToken(String v) { this.token = v; }
    public String getEmployeeId() { return employeeId; } public void setEmployeeId(String v) { this.employeeId = v; }
    public String getName() { return name; } public void setName(String v) { this.name = v; }
    public Boolean getMustChangePassword() { return mustChangePassword; } public void setMustChangePassword(Boolean v) { this.mustChangePassword = v; }
    public Set<String> getRoles() { return roles; } public void setRoles(Set<String> v) { this.roles = v; }
}
