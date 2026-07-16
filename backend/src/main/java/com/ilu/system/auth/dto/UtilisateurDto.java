package com.ilu.system.auth.dto;

public class UtilisateurDto {
    private Long id;
    private String matricule;
    private String nom;
    private String cin;
    private String role;
    private boolean actif;
    private boolean doitChangerMdp;

    public UtilisateurDto(Long id, String matricule, String nom, String cin, String role, boolean actif, boolean doitChangerMdp) {
        this.id = id;
        this.matricule = matricule;
        this.nom = nom;
        this.cin = cin;
        this.role = role;
        this.actif = actif;
        this.doitChangerMdp = doitChangerMdp;
    }

    public Long getId() { return id; }
    public String getMatricule() { return matricule; }
    public String getNom() { return nom; }
    public String getCin() { return cin; }
    public String getRole() { return role; }
    public boolean isActif() { return actif; }
    public boolean isDoitChangerMdp() { return doitChangerMdp; }
}