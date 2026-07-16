package com.ilu.system.structure;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PosteTravailRepository extends JpaRepository<PosteTravail, Long> {
    boolean existsByNomAndZone_Projet_IdProjet(String nom, Long projectId);
}
