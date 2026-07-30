package com.ilu.system.structure.dto;
import java.util.List;
public class CreateProjectRequest {
    private String name; private List<Long> teamIds; private List<MemberAssignment> members;
    public static class MemberAssignment {
        private String employeeId; private String employeeName; private String projectRole;
        public String getEmployeeId() { return employeeId; } public void setEmployeeId(String v) { this.employeeId = v; }
        public String getEmployeeName() { return employeeName; } public void setEmployeeName(String v) { this.employeeName = v; }
        public String getProjectRole() { return projectRole; } public void setProjectRole(String v) { this.projectRole = v; }
    }
    public String getName() { return name; } public void setName(String v) { this.name = v; }
    public List<Long> getTeamIds() { return teamIds; } public void setTeamIds(List<Long> v) { this.teamIds = v; }
    public List<MemberAssignment> getMembers() { return members; } public void setMembers(List<MemberAssignment> v) { this.members = v; }
}