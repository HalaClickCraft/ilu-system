package com.ilu.system.operateur;

import java.time.LocalDate;
import java.util.List;

/**
 * DTO for formation details including all tracking data.
 */
public class FormationDetailsDto {
    private Long idAffectation;
    private String operateurMatricule;
    private String operateurNom;
    private String operateurPrenom;
    private Long posteId;
    private String posteNom;
    private Long projetId;
    private String projetNom;
    private Integer cadenceObjectif;
    private String qualiteObjectif;
    private boolean estAffectationPrimaire;
    private String statut;
    private LocalDate dateDebut;
    private LocalDate dateEvaluationPrevue;
    private List<DailyTrackingDto> dailyTrackings;
    private FormationStatisticsDto statistics;

    public FormationDetailsDto() {}

    public FormationDetailsDto(Long idAffectation, String operateurMatricule, String operateurNom, 
                                String operateurPrenom, Long posteId, String posteNom, Long projetId, 
                                String projetNom, Integer cadenceObjectif, String qualiteObjectif,
                                boolean estAffectationPrimaire, String statut, LocalDate dateDebut, 
                                LocalDate dateEvaluationPrevue) {
        this.idAffectation = idAffectation;
        this.operateurMatricule = operateurMatricule;
        this.operateurNom = operateurNom;
        this.operateurPrenom = operateurPrenom;
        this.posteId = posteId;
        this.posteNom = posteNom;
        this.projetId = projetId;
        this.projetNom = projetNom;
        this.cadenceObjectif = cadenceObjectif;
        this.qualiteObjectif = qualiteObjectif;
        this.estAffectationPrimaire = estAffectationPrimaire;
        this.statut = statut;
        this.dateDebut = dateDebut;
        this.dateEvaluationPrevue = dateEvaluationPrevue;
    }

    // Getters and Setters
    public Long getIdAffectation() {
        return idAffectation;
    }

    public void setIdAffectation(Long idAffectation) {
        this.idAffectation = idAffectation;
    }

    public String getOperateurMatricule() {
        return operateurMatricule;
    }

    public void setOperateurMatricule(String operateurMatricule) {
        this.operateurMatricule = operateurMatricule;
    }

    public String getOperateurNom() {
        return operateurNom;
    }

    public void setOperateurNom(String operateurNom) {
        this.operateurNom = operateurNom;
    }

    public String getOperateurPrenom() {
        return operateurPrenom;
    }

    public void setOperateurPrenom(String operateurPrenom) {
        this.operateurPrenom = operateurPrenom;
    }

    public Long getPosteId() {
        return posteId;
    }

    public void setPosteId(Long posteId) {
        this.posteId = posteId;
    }

    public String getPosteNom() {
        return posteNom;
    }

    public void setPosteNom(String posteNom) {
        this.posteNom = posteNom;
    }

    public Long getProjetId() {
        return projetId;
    }

    public void setProjetId(Long projetId) {
        this.projetId = projetId;
    }

    public String getProjetNom() {
        return projetNom;
    }

    public void setProjetNom(String projetNom) {
        this.projetNom = projetNom;
    }

    public Integer getCadenceObjectif() {
        return cadenceObjectif;
    }

    public void setCadenceObjectif(Integer cadenceObjectif) {
        this.cadenceObjectif = cadenceObjectif;
    }

    public String getQualiteObjectif() {
        return qualiteObjectif;
    }

    public void setQualiteObjectif(String qualiteObjectif) {
        this.qualiteObjectif = qualiteObjectif;
    }

    public boolean isEstAffectationPrimaire() {
        return estAffectationPrimaire;
    }

    public void setEstAffectationPrimaire(boolean estAffectationPrimaire) {
        this.estAffectationPrimaire = estAffectationPrimaire;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    public LocalDate getDateDebut() {
        return dateDebut;
    }

    public void setDateDebut(LocalDate dateDebut) {
        this.dateDebut = dateDebut;
    }

    public LocalDate getDateEvaluationPrevue() {
        return dateEvaluationPrevue;
    }

    public void setDateEvaluationPrevue(LocalDate dateEvaluationPrevue) {
        this.dateEvaluationPrevue = dateEvaluationPrevue;
    }

    public List<DailyTrackingDto> getDailyTrackings() {
        return dailyTrackings;
    }

    public void setDailyTrackings(List<DailyTrackingDto> dailyTrackings) {
        this.dailyTrackings = dailyTrackings;
    }

    public FormationStatisticsDto getStatistics() {
        return statistics;
    }

    public void setStatistics(FormationStatisticsDto statistics) {
        this.statistics = statistics;
    }
}
