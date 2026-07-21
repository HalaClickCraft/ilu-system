package com.ilu.system.operateur;

import com.ilu.system.auth.Utilisateur;
import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entrée quotidienne du journal de formation. Chaque affectation de formation maintient
 * un journal de 12 jours indépendant avec une entrée par jour.
 */
@Entity
@Table(name = "SUIVI_FORMATION_JOURNALIER", uniqueConstraints = @UniqueConstraint(columnNames = {"id_affectation", "jour"}))
public class SuiviFormationJournalier {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_suivi")
    private Long idSuivi;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_affectation", nullable = false)
    private AffectationFormation affectation;

    @Column(nullable = false)
    private Integer jour; // 1-12

    @Column(name = "cadence_realisee")
    private Integer cadenceRealisee;

    @Column(name = "nb_defauts", nullable = false)
    private Integer nbDefauts = 0;

    @Column(columnDefinition = "TEXT")
    private String remarques;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "saisie_par")
    private Utilisateur saisieePar;

    @Column(name = "date_saisie", nullable = false, updatable = false)
    private LocalDateTime dateSaisie = LocalDateTime.now();

    public SuiviFormationJournalier() {}

    public SuiviFormationJournalier(AffectationFormation affectation, Integer jour) {
        this.affectation = affectation;
        this.jour = jour;
        this.nbDefauts = 0;
    }

    // Getters and setters
    public Long getIdSuivi() { return idSuivi; }
    public void setIdSuivi(Long idSuivi) { this.idSuivi = idSuivi; }

    public AffectationFormation getAffectation() { return affectation; }
    public void setAffectation(AffectationFormation affectation) { this.affectation = affectation; }

    public Integer getJour() { return jour; }
    public void setJour(Integer jour) { this.jour = jour; }

    public Integer getCadenceRealisee() { return cadenceRealisee; }
    public void setCadenceRealisee(Integer cadenceRealisee) { this.cadenceRealisee = cadenceRealisee; }

    public Integer getNbDefauts() { return nbDefauts; }
    public void setNbDefauts(Integer nbDefauts) { this.nbDefauts = nbDefauts; }

    public String getRemarques() { return remarques; }
    public void setRemarques(String remarques) { this.remarques = remarques; }

    public Utilisateur getSaisieePar() { return saisieePar; }
    public void setSaisieePar(Utilisateur saisieePar) { this.saisieePar = saisieePar; }

    public LocalDateTime getDateSaisie() { return dateSaisie; }
    public void setDateSaisie(LocalDateTime dateSaisie) { this.dateSaisie = dateSaisie; }
}
