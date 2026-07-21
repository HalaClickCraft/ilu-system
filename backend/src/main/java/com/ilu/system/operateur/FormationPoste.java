package com.ilu.system.operateur;

import com.ilu.system.structure.PosteTravail;
import jakarta.persistence.*;
import java.time.LocalDate;

/** Une ligne par poste appris : un opérateur peut donc être formé sur plusieurs postes. */
@Entity
@Table(name = "OPERATEUR_POSTE", uniqueConstraints = @UniqueConstraint(columnNames = {"operateur_matricule", "poste_id"}))
public class FormationPoste {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER) @JoinColumn(name = "operateur_matricule", nullable = false)
    private Operateur operateur;

    @ManyToOne(fetch = FetchType.EAGER) @JoinColumn(name = "poste_id", nullable = false)
    private PosteTravail poste;

    @Column(nullable = false, length = 50)
    private String statut;

    @Column(name = "date_debut", nullable = false)
    private LocalDate dateDebut;

    @Column(name = "date_evaluation_prevue", nullable = false)
    private LocalDate dateEvaluationPrevue;

    // Remplie seulement quand l'évaluation a réellement eu lieu (peut être avant ou après la date prévue)
    @Column(name = "date_evaluation_reelle")
    private LocalDate dateEvaluationReelle;

    protected FormationPoste() { }

    public static FormationPoste enFormation(Operateur operateur, PosteTravail poste) {
        FormationPoste formation = new FormationPoste();
        formation.operateur = operateur;
        formation.poste = poste;
        formation.statut = "EN_FORMATION";
        formation.dateDebut = LocalDate.now();
        formation.dateEvaluationPrevue = formation.dateDebut.plusDays(12);
        return formation;
    }

    public Long getId() { return id; }
    public PosteTravail getPoste() { return poste; }
    public Operateur getOperateur() { return operateur; }
    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }
    public LocalDate getDateDebut() { return dateDebut; }
    public LocalDate getDateEvaluationPrevue() { return dateEvaluationPrevue; }
    public LocalDate getDateEvaluationReelle() { return dateEvaluationReelle; }
    public void setDateEvaluationReelle(LocalDate dateEvaluationReelle) { this.dateEvaluationReelle = dateEvaluationReelle; }
}