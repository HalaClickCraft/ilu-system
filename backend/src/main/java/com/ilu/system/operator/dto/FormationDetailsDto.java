package com.ilu.system.operator.dto;
import java.time.LocalDate;
import java.util.List;
public class FormationDetailsDto {
    private Long id;
    private Long operatorId;
    private String operatorEmployeeId;
    private String operatorName;
    private Long workstationId;
    private String workstationName;
    private LocalDate startDate;
    private LocalDate endDate;
    private String status;
    private Integer achievedLevel;
    private Integer targetLevel;
    private Integer targetCadence;
    private Integer qualityObjective;
    private Double averageCadence;
    private Integer totalDefects;
    private Integer daysWithData;
    private Integer cadenceDaysCount;
    private Integer defectsDaysCount;
    private Boolean passedCadence;
    private Boolean passedQuality;
    public Long getId() { return id; } public void setId(Long id) { this.id = id; }
    public Long getOperatorId() { return operatorId; } public void setOperatorId(Long v) { this.operatorId = v; }
    public String getOperatorEmployeeId() { return operatorEmployeeId; } public void setOperatorEmployeeId(String v) { this.operatorEmployeeId = v; }
    public String getOperatorName() { return operatorName; } public void setOperatorName(String v) { this.operatorName = v; }
    public Long getWorkstationId() { return workstationId; } public void setWorkstationId(Long v) { this.workstationId = v; }
    public String getWorkstationName() { return workstationName; } public void setWorkstationName(String v) { this.workstationName = v; }
    public LocalDate getStartDate() { return startDate; } public void setStartDate(LocalDate v) { this.startDate = v; }
    public LocalDate getEndDate() { return endDate; } public void setEndDate(LocalDate v) { this.endDate = v; }
    public String getStatus() { return status; } public void setStatus(String v) { this.status = v; }
    public Integer getAchievedLevel() { return achievedLevel; } public void setAchievedLevel(Integer v) { this.achievedLevel = v; }
    public Integer getTargetLevel() { return targetLevel; } public void setTargetLevel(Integer v) { this.targetLevel = v; }
    public Integer getTargetCadence() { return targetCadence; } public void setTargetCadence(Integer v) { this.targetCadence = v; }
    public Integer getQualityObjective() { return qualityObjective; } public void setQualityObjective(Integer v) { this.qualityObjective = v; }
    public Double getAverageCadence() { return averageCadence; } public void setAverageCadence(Double v) { this.averageCadence = v; }
    public Integer getTotalDefects() { return totalDefects; } public void setTotalDefects(Integer v) { this.totalDefects = v; }
    public Integer getDaysWithData() { return daysWithData; } public void setDaysWithData(Integer v) { this.daysWithData = v; }
    public Integer getCadenceDaysCount() { return cadenceDaysCount; } public void setCadenceDaysCount(Integer v) { this.cadenceDaysCount = v; }
    public Integer getDefectsDaysCount() { return defectsDaysCount; } public void setDefectsDaysCount(Integer v) { this.defectsDaysCount = v; }
    public Boolean getPassedCadence() { return passedCadence; } public void setPassedCadence(Boolean v) { this.passedCadence = v; }
    public Boolean getPassedQuality() { return passedQuality; } public void setPassedQuality(Boolean v) { this.passedQuality = v; }
}