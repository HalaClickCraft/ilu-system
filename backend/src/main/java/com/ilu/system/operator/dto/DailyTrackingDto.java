package com.ilu.system.operator.dto;
import java.time.LocalDate;
public class DailyTrackingDto {
    private Long id;
    private LocalDate trackingDate;
    private Integer dailyLevel;
    private String comment;
    private String supervisor;
    public Long getId() { return id; } public void setId(Long id) { this.id = id; }
    public LocalDate getTrackingDate() { return trackingDate; } public void setTrackingDate(LocalDate v) { this.trackingDate = v; }
    public Integer getDailyLevel() { return dailyLevel; } public void setDailyLevel(Integer v) { this.dailyLevel = v; }
    public String getComment() { return comment; } public void setComment(String v) { this.comment = v; }
    public String getSupervisor() { return supervisor; } public void setSupervisor(String v) { this.supervisor = v; }
}
