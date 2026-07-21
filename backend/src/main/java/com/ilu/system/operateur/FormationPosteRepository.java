package com.ilu.system.operateur;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FormationPosteRepository extends JpaRepository<FormationPoste, Long> {
    Optional<FormationPoste> findByOperateur_MatriculeAndPoste_IdPoste(String matricule, Long posteId);
    List<FormationPoste> findByOperateur_Matricule(String matricule);
}
