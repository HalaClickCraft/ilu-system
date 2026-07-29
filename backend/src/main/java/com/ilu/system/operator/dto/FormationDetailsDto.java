package com.ilu.system.operator.dto;
import java.time.LocalDate;
public class FormationDetailsDto {
    private Long id;
    private Long operatorId;
    private String operatorName;
    private Long workstationId;
    private String workstationName;
    private LocalDate startDate;
    private LocalDate endDate;
    private String status;
    private Integer achievedLevel;
    private Integer targetLevel;
    public Long getId() { return id; } public void setId(Long id) { this.id = id; }
    public Long getOperatorId() { return operatorId; } public void setOperatorId(Long v) { this.operatorId = v; }
    public String getOperatorName() { return operatorName; } public void setOperatorName(String v) { this.operatorName = v; }
    public Long getWorkstationId() { return workstationId; } public void setWorkstationId(Long v) { this.workstationId = v; }
    public String getWorkstationName() { return workstationName; } public void setWorkstationName(String v) { this.workstationName = v; }
    public LocalDate getStartDate() { return startDate; } public void setStartDate(LocalDate v) { this.startDate = v; }
    public LocalDate getEndDate() { return endDate; } public void setEndDate(LocalDate v) { this.endDate = v; }
    public String getStatus() { return status; } public void setStatus(String v) { this.status = v; }
    public Integer getAchievedLevel() { return achievedLevel; } public void setAchievedLevel(Integer v) { this.achievedLevel = v; }
    public Integer getTargetLevel() { return targetLevel; } public void setTargetLevel(Integer v) { this.targetLevel = v; }
}
