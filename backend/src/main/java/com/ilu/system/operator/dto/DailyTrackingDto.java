package com.ilu.system.operator.dto;
import java.time.LocalDate;
public class DailyTrackingDto {
    private Long id;
    private LocalDate trackingDate;
    private String dailyLevel;
    private Integer objectif;
    private Integer cadence;
    private Integer defauts;
    private String comment;
    private String supervisor;
    public Long getId() { return id; } public void setId(Long id) { this.id = id; }
    public LocalDate getTrackingDate() { return trackingDate; } public void setTrackingDate(LocalDate v) { this.trackingDate = v; }
    public String getDailyLevel() { return dailyLevel; } public void setDailyLevel(String v) { this.dailyLevel = v; }
    public Integer getObjectif() { return objectif; } public void setObjectif(Integer v) { this.objectif = v; }
    public Integer getCadence() { return cadence; } public void setCadence(Integer v) { this.cadence = v; }
    public Integer getDefauts() { return defauts; } public void setDefauts(Integer v) { this.defauts = v; }
    public String getComment() { return comment; } public void setComment(String v) { this.comment = v; }
    public String getSupervisor() { return supervisor; } public void setSupervisor(String v) { this.supervisor = v; }
}