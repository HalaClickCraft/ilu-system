package com.ilu.system.structure;

import java.util.List;

public class CreateProjectRequest {
    private String nom;
    private String logo;
    private List<MemberAssignment> membres;

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
    public String getLogo() { return logo; }
    public void setLogo(String logo) { this.logo = logo; }
    public List<MemberAssignment> getMembres() { return membres; }
    public void setMembres(List<MemberAssignment> membres) { this.membres = membres; }

    public static class MemberAssignment {
        private Long utilisateurId;
        private String roleProjet;

        public Long getUtilisateurId() { return utilisateurId; }
        public void setUtilisateurId(Long utilisateurId) { this.utilisateurId = utilisateurId; }
        public String getRoleProjet() { return roleProjet; }
        public void setRoleProjet(String roleProjet) { this.roleProjet = roleProjet; }
    }
}