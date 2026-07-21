package com.ilu.system.operateur;

import com.ilu.system.auth.Utilisateur;
import com.ilu.system.structure.Project;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

@Entity
@Table(name = "EQUIPE")
public class Equipe {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_equipe")
    private Long idEquipe;

    @Column(nullable = false, length = 100)
    private String nom;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "chef_id")
    private Utilisateur chef;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "projet_id")
    @JsonIgnore
    private Project projet;

    public Equipe() {}

    public Equipe(String nom, Utilisateur chef) {
        this.nom = nom;
        this.chef = chef;
    }

    public Long getIdEquipe() {
        return idEquipe;
    }

    public void setIdEquipe(Long idEquipe) {
        this.idEquipe = idEquipe;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public Utilisateur getChef() {
        return chef;
    }

    public void setChef(Utilisateur chef) {
        this.chef = chef;
    }

    public Project getProjet() { return projet; }
    public void setProjet(Project projet) { this.projet = projet; }
}
