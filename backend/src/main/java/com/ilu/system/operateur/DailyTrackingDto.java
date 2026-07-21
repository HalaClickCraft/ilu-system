package com.ilu.system.operateur;

/**
 * DTO for daily tracking data of a formation.
 */
public class DailyTrackingDto {
    private Long idSuivi;
    private Integer jour;
    private Integer cadenceRealisee;
    private Integer nbDefauts;
    private String remarques;

    public DailyTrackingDto() {}

    public DailyTrackingDto(Integer jour, Integer cadenceRealisee, Integer nbDefauts, String remarques) {
        this.jour = jour;
        this.cadenceRealisee = cadenceRealisee;
        this.nbDefauts = nbDefauts;
        this.remarques = remarques;
    }

    public DailyTrackingDto(Long idSuivi, Integer jour, Integer cadenceRealisee, Integer nbDefauts, String remarques) {
        this.idSuivi = idSuivi;
        this.jour = jour;
        this.cadenceRealisee = cadenceRealisee;
        this.nbDefauts = nbDefauts;
        this.remarques = remarques;
    }

    // Getters and Setters
    public Long getIdSuivi() {
        return idSuivi;
    }

    public void setIdSuivi(Long idSuivi) {
        this.idSuivi = idSuivi;
    }

    public Integer getJour() {
        return jour;
    }

    public void setJour(Integer jour) {
        this.jour = jour;
    }

    public Integer getCadenceRealisee() {
        return cadenceRealisee;
    }

    public void setCadenceRealisee(Integer cadenceRealisee) {
        this.cadenceRealisee = cadenceRealisee;
    }

    public Integer getNbDefauts() {
        return nbDefauts;
    }

    public void setNbDefauts(Integer nbDefauts) {
        this.nbDefauts = nbDefauts;
    }

    public String getRemarques() {
        return remarques;
    }

    public void setRemarques(String remarques) {
        this.remarques = remarques;
    }
}
