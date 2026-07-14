package com.ilu.system.auth.dto;

public class LoginResponse {
    private final String token;
    private final String matricule;
    private final String nom;
    private final String role;
    private final boolean doitChangerMdp;

    public LoginResponse(String token, String matricule, String nom, String role, boolean doitChangerMdp) {
        this.token = token;
        this.matricule = matricule;
        this.nom = nom;
        this.role = role;
        this.doitChangerMdp = doitChangerMdp;
    }

    public String getToken() { return token; }
    public String getMatricule() { return matricule; }
    public String getNom() { return nom; }
    public String getRole() { return role; }
    public boolean isDoitChangerMdp() { return doitChangerMdp; }
}