package com.ilu.system.structure;

import java.time.LocalDateTime;

public class PosteTravailDto {
    private Long idPoste;
    private String nom;
    private String typePoste;
    private int cadenceObjectif;
    private int ciblePolyvalence;
    private LocalDateTime dateCreation;
    private String creePar;

    public PosteTravailDto(Long idPoste, String nom, String typePoste, int cadenceObjectif, int ciblePolyvalence,
                            LocalDateTime dateCreation, String creePar) {
        this.idPoste = idPoste;
        this.nom = nom;
        this.typePoste = typePoste;
        this.cadenceObjectif = cadenceObjectif;
        this.ciblePolyvalence = ciblePolyvalence;
        this.dateCreation = dateCreation;
        this.creePar = creePar;
    }

    public Long getIdPoste() { return idPoste; }
    public String getNom() { return nom; }
    public String getTypePoste() { return typePoste; }
    public int getCadenceObjectif() { return cadenceObjectif; }
    public int getCiblePolyvalence() { return ciblePolyvalence; }
    public LocalDateTime getDateCreation() { return dateCreation; }
    public String getCreePar() { return creePar; }
}
