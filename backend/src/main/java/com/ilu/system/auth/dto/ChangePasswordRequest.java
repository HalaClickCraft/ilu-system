package com.ilu.system.auth.dto;

public class ChangePasswordRequest {
    private String ancienMotDePasse;
    private String nouveauMotDePasse;

    public String getAncienMotDePasse() { return ancienMotDePasse; }
    public void setAncienMotDePasse(String v) { this.ancienMotDePasse = v; }
    public String getNouveauMotDePasse() { return nouveauMotDePasse; }
    public void setNouveauMotDePasse(String v) { this.nouveauMotDePasse = v; }
}