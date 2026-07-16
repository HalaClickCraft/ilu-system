package com.ilu.system.structure;

import java.util.List;
import java.time.LocalDateTime;

public class ZoneDto {
    private Long idZone;
    private String nom;
    private LocalDateTime dateCreation;
    private String creePar;
    private List<PosteTravailDto> postes;

    public ZoneDto(Long idZone, String nom, LocalDateTime dateCreation, String creePar, List<PosteTravailDto> postes) {
        this.idZone = idZone;
        this.nom = nom;
        this.dateCreation = dateCreation;
        this.creePar = creePar;
        this.postes = postes;
    }

    public Long getIdZone() { return idZone; }
    public String getNom() { return nom; }
    public LocalDateTime getDateCreation() { return dateCreation; }
    public String getCreePar() { return creePar; }
    public List<PosteTravailDto> getPostes() { return postes; }
}
