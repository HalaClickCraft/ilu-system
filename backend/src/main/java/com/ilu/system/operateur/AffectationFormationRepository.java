package com.ilu.system.operateur;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AffectationFormationRepository extends JpaRepository<AffectationFormation, Long> {
    
    @Query("SELECT af FROM AffectationFormation af WHERE af.operateur.matricule = :matricule ORDER BY af.dateDebut DESC")
    List<AffectationFormation> findByOperateur_Matricule(@Param("matricule") String matricule);
    
    @Query("SELECT af FROM AffectationFormation af WHERE af.operateur.matricule = :matricule AND af.estAffectationPrimaire = true")
    Optional<AffectationFormation> findPrimaryAssignmentByOperateur(@Param("matricule") String matricule);
    
    @Query("SELECT af FROM AffectationFormation af WHERE af.operateur.matricule = :matricule AND af.estAffectationPrimaire = false")
    List<AffectationFormation> findSecondaryAssignmentsByOperateur(@Param("matricule") String matricule);
    
    @Query("SELECT af FROM AffectationFormation af WHERE af.projet.idProjet = :projetId")
    List<AffectationFormation> findByProjet_IdProjet(@Param("projetId") Long projetId);
    
    @Query("SELECT af FROM AffectationFormation af WHERE af.projet.idProjet = :projetId AND af.statut = :statut")
    List<AffectationFormation> findByProjet_IdProjet_AndStatut(@Param("projetId") Long projetId, @Param("statut") String statut);
    
    @Query("SELECT af FROM AffectationFormation af WHERE af.operateur.matricule = :matricule AND af.poste.idPoste = :posteId AND af.projet.idProjet = :projetId")
    Optional<AffectationFormation> findByOperateur_PosteAndProjet(@Param("matricule") String matricule, @Param("posteId") Long posteId, @Param("projetId") Long projetId);
    
    // For Chef d'Équipe scoped queries: get assignments only from their assigned project
    @Query("SELECT DISTINCT af FROM AffectationFormation af " +
           "WHERE af.projet.idProjet = :projetId")
    List<AffectationFormation> findByProjectAndChef(@Param("projetId") Long projetId, @Param("userId") Long userId);
    
    @Query("SELECT af FROM AffectationFormation af WHERE af.dateEvaluationPrevue <= :date AND af.statut = 'EN_FORMATION'")
    List<AffectationFormation> findOverdueTrainings(@Param("date") LocalDate date);
    
    @Query("SELECT af FROM AffectationFormation af " +
           "WHERE af.projet.idProjet IN (" +
           "  SELECT pm.projet.idProjet FROM ProjectMember pm " +
           "  WHERE pm.utilisateur.id = :userId" +
           ")")
    List<AffectationFormation> findByChefEquipe(@Param("userId") Long userId);
}
