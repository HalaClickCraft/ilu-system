package com.ilu.system.operator.dto;
import java.util.List;
public class FormationStatisticsDto {
    private long totalOperators;
    private long operatorsInTraining;
    private long operatorsCertified;
    private long operatorsNotStarted;
    private Double averageLevel;
    private Long totalWorkstations;
    private Long totalTeams;
    private List<ChartDataDto> statusDistribution;
    private List<ChartDataDto> teamDistribution;
    public long getTotalOperators() { return totalOperators; } public void setTotalOperators(long v) { this.totalOperators = v; }
    public long getOperatorsInTraining() { return operatorsInTraining; } public void setOperatorsInTraining(long v) { this.operatorsInTraining = v; }
    public long getOperatorsCertified() { return operatorsCertified; } public void setOperatorsCertified(long v) { this.operatorsCertified = v; }
    public long getOperatorsNotStarted() { return operatorsNotStarted; } public void setOperatorsNotStarted(long v) { this.operatorsNotStarted = v; }
    public Double getAverageLevel() { return averageLevel; } public void setAverageLevel(Double v) { this.averageLevel = v; }
    public Long getTotalWorkstations() { return totalWorkstations; } public void setTotalWorkstations(Long v) { this.totalWorkstations = v; }
    public Long getTotalTeams() { return totalTeams; } public void setTotalTeams(Long v) { this.totalTeams = v; }
    public List<ChartDataDto> getStatusDistribution() { return statusDistribution; } public void setStatusDistribution(List<ChartDataDto> v) { this.statusDistribution = v; }
    public List<ChartDataDto> getTeamDistribution() { return teamDistribution; } public void setTeamDistribution(List<ChartDataDto> v) { this.teamDistribution = v; }
}
