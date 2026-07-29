package com.ilu.system.operator.dto;
public class EncadrementDto {
    private Long id;
    private String supervisorName;
    private String operatorName;
    private String workstationName;
    private String status;
    private Integer currentLevel;
    public Long getId() { return id; } public void setId(Long id) { this.id = id; }
    public String getSupervisorName() { return supervisorName; } public void setSupervisorName(String v) { this.supervisorName = v; }
    public String getOperatorName() { return operatorName; } public void setOperatorName(String v) { this.operatorName = v; }
    public String getWorkstationName() { return workstationName; } public void setWorkstationName(String v) { this.workstationName = v; }
    public String getStatus() { return status; } public void setStatus(String v) { this.status = v; }
    public Integer getCurrentLevel() { return currentLevel; } public void setCurrentLevel(Integer v) { this.currentLevel = v; }
}
