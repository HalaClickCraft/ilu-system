package com.ilu.system.operateur;

import com.ilu.system.structure.PosteTravail;
import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Formation template for a Poste (work station).
 * Defines the training objectives and quality standards for that Poste.
 */
@Entity
@Table(name = "FORMATION_TEMPLATE", uniqueConstraints = @UniqueConstraint(columnNames = {"poste_id"}))
public class FormationTemplate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_template")
    private Long idTemplate;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "poste_id", nullable = false, unique = true)
    private PosteTravail poste;

    @Column(name = "cadence_objectif", nullable = false)
    private Integer cadenceObjectif;

    @Column(name = "qualite_objectif_texte", nullable = false, columnDefinition = "VARCHAR(255)")
    private String qualiteObjectifTexte; // e.g., "< 7 defects in 12 days"

    @Column(name = "date_creation", nullable = false, updatable = false)
    private LocalDateTime dateCreation;

    @Column(name = "cree_par", nullable = false)
    private String creePar;

    @Column(name = "date_modification")
    private LocalDateTime dateModification;

    @Column(name = "modifie_par")
    private String modifiePar;

    @PrePersist
    protected void onCreate() {
        dateCreation = LocalDateTime.now();
        if (creePar == null || creePar.isBlank()) {
            creePar = "Système";
        }
    }

    @PreUpdate
    protected void onUpdate() {
        dateModification = LocalDateTime.now();
    }

    public FormationTemplate() {}

    public FormationTemplate(PosteTravail poste, Integer cadenceObjectif, String qualiteObjectifTexte, String creePar) {
        this.poste = poste;
        this.cadenceObjectif = cadenceObjectif;
        this.qualiteObjectifTexte = qualiteObjectifTexte;
        this.creePar = creePar;
    }

    // Getters and Setters
    public Long getIdTemplate() {
        return idTemplate;
    }

    public void setIdTemplate(Long idTemplate) {
        this.idTemplate = idTemplate;
    }

    public PosteTravail getPoste() {
        return poste;
    }

    public void setPoste(PosteTravail poste) {
        this.poste = poste;
    }

    public Integer getCadenceObjectif() {
        return cadenceObjectif;
    }

    public void setCadenceObjectif(Integer cadenceObjectif) {
        this.cadenceObjectif = cadenceObjectif;
    }

    public String getQualiteObjectifTexte() {
        return qualiteObjectifTexte;
    }

    public void setQualiteObjectifTexte(String qualiteObjectifTexte) {
        this.qualiteObjectifTexte = qualiteObjectifTexte;
    }

    public LocalDateTime getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(LocalDateTime dateCreation) {
        this.dateCreation = dateCreation;
    }

    public String getCreePar() {
        return creePar;
    }

    public void setCreePar(String creePar) {
        this.creePar = creePar;
    }

    public LocalDateTime getDateModification() {
        return dateModification;
    }

    public void setDateModification(LocalDateTime dateModification) {
        this.dateModification = dateModification;
    }

    public String getModifiePar() {
        return modifiePar;
    }

    public void setModifiePar(String modifiePar) {
        this.modifiePar = modifiePar;
    }
}
