package com.ilu.system.structure.dto;
import java.util.List;
public class ZoneDto {
    private Long id; private String name; private Long projectId; private String projectName; private String createdByName;
    private List<WorkstationDto> workstations;
    public Long getId() { return id; } public void setId(Long id) { this.id = id; }
    public String getName() { return name; } public void setName(String v) { this.name = v; }
    public Long getProjectId() { return projectId; } public void setProjectId(Long v) { this.projectId = v; }
    public String getProjectName() { return projectName; } public void setProjectName(String v) { this.projectName = v; }
    public String getCreatedByName() { return createdByName; } public void setCreatedByName(String v) { this.createdByName = v; }
    public List<WorkstationDto> getWorkstations() { return workstations; } public void setWorkstations(List<WorkstationDto> v) { this.workstations = v; }
}