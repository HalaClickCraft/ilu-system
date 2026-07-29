package com.ilu.system.auth.dto;
import java.util.Set;
public class CreateUserRequest {
    private String employeeId;
    private String name;
    private String nationalId;
    private String password;
    private Set<String> roles;
    public String getEmployeeId() { return employeeId; } public void setEmployeeId(String v) { this.employeeId = v; }
    public String getName() { return name; } public void setName(String v) { this.name = v; }
    public String getNationalId() { return nationalId; } public void setNationalId(String v) { this.nationalId = v; }
    public String getPassword() { return password; } public void setPassword(String v) { this.password = v; }
    public Set<String> getRoles() { return roles; } public void setRoles(Set<String> v) { this.roles = v; }
}
