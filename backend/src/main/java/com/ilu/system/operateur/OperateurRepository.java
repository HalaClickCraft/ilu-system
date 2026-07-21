package com.ilu.system.operateur;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface OperateurRepository extends JpaRepository<Operateur, String> {
    List<Operateur> findByEquipe_IdEquipe(Long idEquipe);
    List<Operateur> findByEquipe_Chef_Id(Long chefId);
    List<Operateur> findByPosteAffecte_Zone_Projet_IdProjetIn(List<Long> projectIds);
}
