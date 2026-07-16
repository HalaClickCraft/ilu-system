package com.ilu.system.structure;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.PrePersist;

import java.time.LocalDateTime;

@Entity
@Table(name = "PROJET")
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_projet")
    private Long idProjet;

    @Column(name = "nom", nullable = false, unique = true)
    private String nom;

    @Column(name = "logo")
    private String logo;

    @Column(name = "date_creation", nullable = false, updatable = false,
            columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime dateCreation;

    @Column(name = "cree_par", nullable = false, updatable = false)
    private String creePar;

    public Project() {}

    public Project(String nom) {
        this.nom = nom;
    }

    public Project(String nom, String creePar) {
        this.nom = nom;
        this.creePar = creePar;
    }

    @PrePersist
    void setCreationMetadata() {
        if (dateCreation == null) dateCreation = LocalDateTime.now();
        if (creePar == null || creePar.isBlank()) creePar = "Système";
    }

    public Long getIdProjet() { return idProjet; }
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
    public String getLogo() { return logo; }
    public void setLogo(String logo) { this.logo = logo; }
    public LocalDateTime getDateCreation() { return dateCreation; }
    public String getCreePar() { return creePar; }
}
