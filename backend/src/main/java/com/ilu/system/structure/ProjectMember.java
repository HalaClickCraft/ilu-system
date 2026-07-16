package com.ilu.system.structure;

import com.ilu.system.auth.Utilisateur;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "projet_membre", uniqueConstraints = @UniqueConstraint(columnNames = {"projet_id", "utilisateur_id"}))
public class ProjectMember {

    public enum RoleInProject {
        CHEF_DE_PROJET,
        RESPONSABLE_QUALITE,
        AGENT_QUALITE,
        RESPONSABLE_HSE,
        SUPERVISEUR
    }

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "projet_id", nullable = false)
    private Project projet;

    @ManyToOne(fetch = FetchType.EAGER) @JoinColumn(name = "utilisateur_id", nullable = false)
    private Utilisateur utilisateur;

    @Enumerated(EnumType.STRING)
    @Column(name = "role_projet", nullable = false)
    private RoleInProject roleProjet;

    protected ProjectMember() {}

    public ProjectMember(Project projet, Utilisateur utilisateur, RoleInProject roleProjet) {
        this.projet = projet;
        this.utilisateur = utilisateur;
        this.roleProjet = roleProjet;
    }

    public Long getId() { return id; }
    public Project getProjet() { return projet; }
    public Utilisateur getUtilisateur() { return utilisateur; }
    public RoleInProject getRoleProjet() { return roleProjet; }
}