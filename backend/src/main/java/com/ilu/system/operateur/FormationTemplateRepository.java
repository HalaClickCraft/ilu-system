package com.ilu.system.operateur;

import com.ilu.system.structure.PosteTravail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FormationTemplateRepository extends JpaRepository<FormationTemplate, Long> {
    Optional<FormationTemplate> findByPoste(PosteTravail poste);
    Optional<FormationTemplate> findByPosteIdPoste(Long posteId);
}
