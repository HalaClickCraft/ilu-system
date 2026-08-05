package com.ilu.system.auth.dto;
import java.util.Set;
public class UserDto {
    private Long id;
    private String employeeId;
    private String name;
    private String nationalId;
    private Boolean mustChangePassword;
    private String department;
    private Boolean active;
    private Set<String> roles;
    public Long getId() { return id; } public void setId(Long id) { this.id = id; }
    public String getEmployeeId() { return employeeId; } public void setEmployeeId(String employeeId) { this.employeeId = employeeId; }
    public String getName() { return name; } public void setName(String name) { this.name = name; }
    public String getNationalId() { return nationalId; } public void setNationalId(String nationalId) { this.nationalId = nationalId; }
    public Boolean getMustChangePassword() { return mustChangePassword; } public void setMustChangePassword(Boolean v) { this.mustChangePassword = v; }
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; } 
    public Boolean getActive() { return active; } public void setActive(Boolean v) { this.active = v; }
    public Set<String> getRoles() { return roles; } public void setRoles(Set<String> roles) { this.roles = roles; }
}
