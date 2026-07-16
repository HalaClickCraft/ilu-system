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
@Table(name = "POSTE")
public class PosteTravail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_poste")
    private Long idPoste;

    @Column(name = "nom", nullable = false)
    private String nom;

    @Column(name = "type_poste", nullable = false)
    private String typePoste = "Manuel";

    @Column(name = "cadence_objectif", nullable = false)
    private int cadenceObjectif = 100;

    @Column(name = "cible_polyvalence", nullable = false)
    private int ciblePolyvalence = 3;

    @Column(name = "date_creation", nullable = false, updatable = false,
            columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime dateCreation;

    @Column(name = "cree_par", nullable = false, updatable = false)
    private String creePar;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "zone_id")
    private Zone zone;

    public PosteTravail() {}

    public PosteTravail(String nom, Zone zone) {
        this.nom = nom;
        this.zone = zone;
    }

    public PosteTravail(String nom, Zone zone, String creePar) {
        this.nom = nom;
        this.zone = zone;
        this.creePar = creePar;
    }

    @PrePersist
    void setCreationMetadata() {
        if (dateCreation == null) dateCreation = LocalDateTime.now();
        if (creePar == null || creePar.isBlank()) creePar = "Système";
    }

    public Long getIdPoste() { return idPoste; }
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
    public String getTypePoste() { return typePoste; }
    public void setTypePoste(String typePoste) { this.typePoste = typePoste; }
    public int getCadenceObjectif() { return cadenceObjectif; }
    public void setCadenceObjectif(int cadenceObjectif) { this.cadenceObjectif = cadenceObjectif; }
    public int getCiblePolyvalence() { return ciblePolyvalence; }
    public void setCiblePolyvalence(int ciblePolyvalence) { this.ciblePolyvalence = ciblePolyvalence; }
    public Zone getZone() { return zone; }
    public void setZone(Zone zone) { this.zone = zone; }
    public LocalDateTime getDateCreation() { return dateCreation; }
    public String getCreePar() { return creePar; }
}
