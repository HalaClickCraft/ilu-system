package com.ilu.system.operateur;

import com.ilu.system.auth.Utilisateur;
import com.ilu.system.structure.PosteTravail;
import com.ilu.system.structure.Project;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Affectation de formation : un opérateur en formation sur un poste spécifique.
 * Peut être soit une affectation primaire (poste_affecte) ou une formation parallèle.
 * Chaque instance de formation dispose de son propre journal de 12 jours indépendant.
 */
@Entity
@Table(name = "AFFECTATION_FORMATION", uniqueConstraints = @UniqueConstraint(columnNames = {"operateur_matricule", "id_poste", "id_projet"}))
public class AffectationFormation {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_affectation")
    private Long idAffectation;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "operateur_matricule", referencedColumnName = "matricule", nullable = false)
    private Operateur operateur;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_poste", nullable = false)
    private PosteTravail poste;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_projet", nullable = false)
    private Project projet;

    @Column(name = "est_affectation_primaire", nullable = false)
    private boolean estAffectationPrimaire = false;

    @Column(nullable = false, length = 50)
    private String statut; // EN_FORMATION, EVALUEE, VALIDEE, ECHOUEE

    @Column(name = "date_debut", nullable = false)
    private LocalDate dateDebut;

    @Column(name = "date_evaluation_prevue")
    private LocalDate dateEvaluationPrevue;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cree_par")
    @com.fasterxml.jackson.annotation.JsonIgnore
    private Utilisateur creePar;

    @Column(name = "qualite_objectif", columnDefinition = "VARCHAR(255)")
    private String qualiteObjectif;

    @Column(name = "date_creation", nullable = false, updatable = false)
    private LocalDateTime dateCreation = LocalDateTime.now();

    public AffectationFormation() {}

    public static AffectationFormation enFormationPrimaire(Operateur operateur, PosteTravail poste, Project projet, Utilisateur creePar) {
        AffectationFormation af = new AffectationFormation();
        af.operateur = operateur;
        af.poste = poste;
        af.projet = projet;
        af.estAffectationPrimaire = true;
        af.statut = "EN_FORMATION";
        af.dateDebut = LocalDate.now();
        af.dateEvaluationPrevue = af.dateDebut.plusDays(12);
        af.creePar = creePar;
        return af;
    }

    public static AffectationFormation enFormationSecondaire(Operateur operateur, PosteTravail poste, Project projet, Utilisateur creePar) {
        AffectationFormation af = new AffectationFormation();
        af.operateur = operateur;
        af.poste = poste;
        af.projet = projet;
        af.estAffectationPrimaire = false;
        af.statut = "EN_FORMATION";
        af.dateDebut = LocalDate.now();
        af.dateEvaluationPrevue = af.dateDebut.plusDays(12);
        af.creePar = creePar;
        return af;
    }

    // Getters and setters
    public Long getIdAffectation() { return idAffectation; }
    public void setIdAffectation(Long idAffectation) { this.idAffectation = idAffectation; }

    public Operateur getOperateur() { return operateur; }
    public void setOperateur(Operateur operateur) { this.operateur = operateur; }

    public PosteTravail getPoste() { return poste; }
    public void setPoste(PosteTravail poste) { this.poste = poste; }

    public Project getProjet() { return projet; }
    public void setProjet(Project projet) { this.projet = projet; }

    public boolean isEstAffectationPrimaire() { return estAffectationPrimaire; }
    public void setEstAffectationPrimaire(boolean estAffectationPrimaire) { this.estAffectationPrimaire = estAffectationPrimaire; }

    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }

    public LocalDate getDateDebut() { return dateDebut; }
    public void setDateDebut(LocalDate dateDebut) { this.dateDebut = dateDebut; }

    public LocalDate getDateEvaluationPrevue() { return dateEvaluationPrevue; }
    public void setDateEvaluationPrevue(LocalDate dateEvaluationPrevue) { this.dateEvaluationPrevue = dateEvaluationPrevue; }

    public Utilisateur getCreePar() { return creePar; }
    public void setCreePar(Utilisateur creePar) { this.creePar = creePar; }

    public LocalDateTime getDateCreation() { return dateCreation; }
    public void setDateCreation(LocalDateTime dateCreation) { this.dateCreation = dateCreation; }

    public String getQualiteObjectif() { return qualiteObjectif; }
    public void setQualiteObjectif(String qualiteObjectif) { this.qualiteObjectif = qualiteObjectif; }
}
