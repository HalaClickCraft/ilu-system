package com.ilu.system.structure;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ZoneRepository extends JpaRepository<Zone, Long> {
    boolean existsByNomAndProjet_IdProjet(String nom, Long projectId);
}
