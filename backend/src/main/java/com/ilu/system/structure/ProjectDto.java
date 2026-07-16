package com.ilu.system.structure;

import java.util.List;
import java.time.LocalDateTime;

public class ProjectDto {
    private Long idProjet;
    private String nom;
    private String logo;
    private LocalDateTime dateCreation;
    private String creePar;
    private List<ZoneDto> zones;
    private List<ProjectMemberDto> membres;

    public ProjectDto(Long idProjet, String nom, String logo, LocalDateTime dateCreation, String creePar, List<ZoneDto> zones) {
        this(idProjet, nom, logo, dateCreation, creePar, zones, List.of());
    }
    public ProjectDto(Long idProjet, String nom, String logo, LocalDateTime dateCreation, String creePar, List<ZoneDto> zones, List<ProjectMemberDto> membres) {
        this.idProjet = idProjet;
        this.nom = nom;
        this.logo = logo;
        this.dateCreation = dateCreation;
        this.creePar = creePar;
        this.zones = zones;
        this.membres = membres;
    }

    public Long getIdProjet() { return idProjet; }
    public String getNom() { return nom; }
    public String getLogo() { return logo; }
    public LocalDateTime getDateCreation() { return dateCreation; }
    public String getCreePar() { return creePar; }
    public List<ZoneDto> getZones() { return zones; }
    public List<ProjectMemberDto> getMembres() { return membres; }
}
