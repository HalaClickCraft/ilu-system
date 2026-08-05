package com.ilu.system.operator.dto;

import java.time.LocalDate;

public class DailyBatchEntryDto {
    private Long formationId;
    private Integer dayNumber;
    private LocalDate trackingDate;
    private Integer cadence;
    private Integer defauts;

    public Long getFormationId() { return formationId; }
    public void setFormationId(Long formationId) { this.formationId = formationId; }
    public Integer getDayNumber() { return dayNumber; }
    public void setDayNumber(Integer dayNumber) { this.dayNumber = dayNumber; }
    public LocalDate getTrackingDate() { return trackingDate; }
    public void setTrackingDate(LocalDate trackingDate) { this.trackingDate = trackingDate; }
    public Integer getCadence() { return cadence; }
    public void setCadence(Integer cadence) { this.cadence = cadence; }
    public Integer getDefauts() { return defauts; }
    public void setDefauts(Integer defauts) { this.defauts = defauts; }

    public DailyTrackingDto toDailyTrackingDto() {
        DailyTrackingDto dto = new DailyTrackingDto();
        dto.setDayNumber(dayNumber);
        dto.setTrackingDate(trackingDate);
        dto.setCadence(cadence);
        dto.setDefauts(defauts);
        return dto;
    }
}
