package com.ilu.system.operateur;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface EquipeRepository extends JpaRepository<Equipe, Long> {
    List<Equipe> findByChef_Id(Long chefId);
    List<Equipe> findByProjet_IdProjet(Long projectId);
}
