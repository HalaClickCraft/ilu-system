package com.ilu.system.structure;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.PrePersist;

import java.time.LocalDateTime;

@Entity
@Table(name = "ZONE_LIGNE")
public class Zone {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_zone")
    private Long idZone;

    @Column(name = "nom", nullable = false)
    private String nom;

    @Column(name = "date_creation", nullable = false, updatable = false,
            columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime dateCreation;

    @Column(name = "cree_par", nullable = false, updatable = false)
    private String creePar;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "projet_id")
    private Project projet;

    public Zone() {}

    public Zone(String nom, Project projet) {
        this.nom = nom;
        this.projet = projet;
    }

    public Zone(String nom, Project projet, String creePar) {
        this.nom = nom;
        this.projet = projet;
        this.creePar = creePar;
    }

    @PrePersist
    void setCreationMetadata() {
        if (dateCreation == null) dateCreation = LocalDateTime.now();
        if (creePar == null || creePar.isBlank()) creePar = "Système";
    }

    public Long getIdZone() { return idZone; }
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
    public Project getProjet() { return projet; }
    public void setProjet(Project projet) { this.projet = projet; }
    public LocalDateTime getDateCreation() { return dateCreation; }
    public String getCreePar() { return creePar; }
}
