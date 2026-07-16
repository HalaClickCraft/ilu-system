package com.ilu.system.auth;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UtilisateurRepository extends JpaRepository<Utilisateur, Long> {
    Optional<Utilisateur> findByMatricule(String matricule);

    @Query("SELECT u FROM Utilisateur u WHERE u.role.libelle = :roleType AND u.actif = true")
    List<Utilisateur> findByRoleLibelleAndActifTrue(@Param("roleType") RoleType roleType);
}
