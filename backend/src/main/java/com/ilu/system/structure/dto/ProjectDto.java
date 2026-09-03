package com.ilu.system.structure.dto;
import java.util.List;
public class ProjectDto {
    private Long id; private String name; private String createdByName; private List<ZoneDto> zones;
    public Long getId() { return id; } public void setId(Long id) { this.id = id; }
    public String getName() { return name; } public void setName(String v) { this.name = v; }
    public String getCreatedByName() { return createdByName; } public void setCreatedByName(String v) { this.createdByName = v; }
    public List<ZoneDto> getZones() { return zones; } public void setZones(List<ZoneDto> v) { this.zones = v; }
}