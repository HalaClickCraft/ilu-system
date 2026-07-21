package com.ilu.system.operateur;

/**
 * DTO for formation statistics (averages, totals, etc).
 */
public class FormationStatisticsDto {
    private Double cadenceMoyenne;
    private Integer totalDefauts;
    private Integer daysWithData;
    private Double percentageOfDaysWithData;
    private boolean qualityObjectifMet;

    public FormationStatisticsDto() {}

    public FormationStatisticsDto(Double cadenceMoyenne, Integer totalDefauts, Integer daysWithData, Double percentageOfDaysWithData, boolean qualityObjectifMet) {
        this.cadenceMoyenne = cadenceMoyenne;
        this.totalDefauts = totalDefauts;
        this.daysWithData = daysWithData;
        this.percentageOfDaysWithData = percentageOfDaysWithData;
        this.qualityObjectifMet = qualityObjectifMet;
    }

    // Getters and Setters
    public Double getCadenceMoyenne() {
        return cadenceMoyenne;
    }

    public void setCadenceMoyenne(Double cadenceMoyenne) {
        this.cadenceMoyenne = cadenceMoyenne;
    }

    public Integer getTotalDefauts() {
        return totalDefauts;
    }

    public void setTotalDefauts(Integer totalDefauts) {
        this.totalDefauts = totalDefauts;
    }

    public Integer getDaysWithData() {
        return daysWithData;
    }

    public void setDaysWithData(Integer daysWithData) {
        this.daysWithData = daysWithData;
    }

    public Double getPercentageOfDaysWithData() {
        return percentageOfDaysWithData;
    }

    public void setPercentageOfDaysWithData(Double percentageOfDaysWithData) {
        this.percentageOfDaysWithData = percentageOfDaysWithData;
    }

    public boolean isQualityObjectifMet() {
        return qualityObjectifMet;
    }

    public void setQualityObjectifMet(boolean qualityObjectifMet) {
        this.qualityObjectifMet = qualityObjectifMet;
    }
}
