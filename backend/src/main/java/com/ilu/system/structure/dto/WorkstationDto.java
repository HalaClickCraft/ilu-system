package com.ilu.system.structure.dto;
public class WorkstationDto {
    private Long id; private String name; private String type;
    private Integer targetCadence; private Integer versatilityTarget; private String targetIluLevel;
    private Integer qualityObjective;
    private Long zoneId; private String zoneName;
    public Long getId() { return id; } public void setId(Long id) { this.id = id; }
    public String getName() { return name; } public void setName(String v) { this.name = v; }
    public String getType() { return type; } public void setType(String v) { this.type = v; }
    public Integer getTargetCadence() { return targetCadence; } public void setTargetCadence(Integer v) { this.targetCadence = v; }
    public Integer getVersatilityTarget() { return versatilityTarget; } public void setVersatilityTarget(Integer v) { this.versatilityTarget = v; }
    public String getTargetIluLevel() { return targetIluLevel; } public void setTargetIluLevel(String v) { this.targetIluLevel = v; }
        public Integer getQualityObjective() { return qualityObjective; }
    public void setQualityObjective(Integer v) { this.qualityObjective = v; }
    public Long getZoneId() { return zoneId; } public void setZoneId(Long v) { this.zoneId = v; }
    public String getZoneName() { return zoneName; } public void setZoneName(String v) { this.zoneName = v; }
}