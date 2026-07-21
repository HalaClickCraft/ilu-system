package com.ilu.system.operateur;

import java.util.List;
import java.util.Map;

/**
 * DTO for chart data (Cadence réalisée vs Cadence objectif du poste).
 */
public class ChartDataDto {
    private List<Integer> labels; // J1, J2, ..., J12
    private Map<String, Object> cadenceObjectifDataset;
    private Map<String, Object> cadenceRealiseeDataset;

    public ChartDataDto() {}

    public ChartDataDto(List<Integer> labels, Map<String, Object> cadenceObjectifDataset, Map<String, Object> cadenceRealiseeDataset) {
        this.labels = labels;
        this.cadenceObjectifDataset = cadenceObjectifDataset;
        this.cadenceRealiseeDataset = cadenceRealiseeDataset;
    }

    // Getters and Setters
    public List<Integer> getLabels() {
        return labels;
    }

    public void setLabels(List<Integer> labels) {
        this.labels = labels;
    }

    public Map<String, Object> getCadenceObjectifDataset() {
        return cadenceObjectifDataset;
    }

    public void setCadenceObjectifDataset(Map<String, Object> cadenceObjectifDataset) {
        this.cadenceObjectifDataset = cadenceObjectifDataset;
    }

    public Map<String, Object> getCadenceRealiseeDataset() {
        return cadenceRealiseeDataset;
    }

    public void setCadenceRealiseeDataset(Map<String, Object> cadenceRealiseeDataset) {
        this.cadenceRealiseeDataset = cadenceRealiseeDataset;
    }
}
