package com.ilu.system.operateur;

public class CreateOperateurRequest {
    private String matricule;
    private String nom;
    private String dateEmbauche;
    private String dateSortie;
    private String statut;
    private boolean formationRework;
    private Long equipeId;

    public CreateOperateurRequest() {}

    public String getMatricule() { return matricule; }
    public void setMatricule(String matricule) { this.matricule = matricule; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getDateEmbauche() { return dateEmbauche; }
    public void setDateEmbauche(String dateEmbauche) { this.dateEmbauche = dateEmbauche; }

    public String getDateSortie() { return dateSortie; }
    public void setDateSortie(String dateSortie) { this.dateSortie = dateSortie; }

    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }

    public boolean isFormationRework() { return formationRework; }
    public void setFormationRework(boolean formationRework) { this.formationRework = formationRework; }

    public Long getEquipeId() { return equipeId; }
    public void setEquipeId(Long equipeId) { this.equipeId = equipeId; }
}
