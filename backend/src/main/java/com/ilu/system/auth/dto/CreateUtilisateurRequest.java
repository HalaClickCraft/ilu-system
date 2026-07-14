package com.ilu.system.auth.dto;

import com.ilu.system.auth.RoleType;

public class CreateUtilisateurRequest {
    private String matricule;
    private String nom;
    private String cin;
    private RoleType role;

    public String getMatricule() { return matricule; }
    public void setMatricule(String v) { this.matricule = v; }
    public String getNom() { return nom; }
    public void setNom(String v) { this.nom = v; }
    public String getCin() { return cin; }
    public void setCin(String v) { this.cin = v; }
    public RoleType getRole() { return role; }
    public void setRole(RoleType v) { this.role = v; }
}