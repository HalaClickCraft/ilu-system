package com.ilu.system.operateur;

import com.ilu.system.structure.PosteTravail;
import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "OPERATEUR")
public class Operateur {

    @Id
    @Column(length = 50)
    private String matricule;

    @Column(nullable = false, length = 150)
    private String nom;

    @Column(name = "date_embauche", nullable = false)
    private LocalDate dateEmbauche;

    @Column(name = "date_sortie")
    private LocalDate dateSortie;

    @Column(nullable = false, length = 50)
    private String statut;

    @Column(name = "formation_rework", nullable = false)
    private boolean formationRework = false;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "equipe_id")
    private Equipe equipe;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "poste_affecte_id")
    private PosteTravail posteAffecte;

    public Operateur() {}

    public String getMatricule() {
        return matricule;
    }

    public void setMatricule(String matricule) {
        this.matricule = matricule;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public LocalDate getDateEmbauche() {
        return dateEmbauche;
    }

    public void setDateEmbauche(LocalDate dateEmbauche) {
        this.dateEmbauche = dateEmbauche;
    }

    public LocalDate getDateSortie() {
        return dateSortie;
    }

    public void setDateSortie(LocalDate dateSortie) {
        this.dateSortie = dateSortie;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    public boolean isFormationRework() {
        return formationRework;
    }

    public void setFormationRework(boolean formationRework) {
        this.formationRework = formationRework;
    }

    public Equipe getEquipe() {
        return equipe;
    }

    public void setEquipe(Equipe equipe) {
        this.equipe = equipe;
    }

    public PosteTravail getPosteAffecte() {
        return posteAffecte;
    }

    public void setPosteAffecte(PosteTravail posteAffecte) {
        this.posteAffecte = posteAffecte;
    }
}
