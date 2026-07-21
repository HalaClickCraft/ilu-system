package com.ilu.system.operateur;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SuiviFormationJournalierRepository extends JpaRepository<SuiviFormationJournalier, Long> {
    
    @Query("SELECT sfj FROM SuiviFormationJournalier sfj WHERE sfj.affectation.idAffectation = :affectationId ORDER BY sfj.jour ASC")
    List<SuiviFormationJournalier> findByAffectation_IdAffectation(@Param("affectationId") Long affectationId);
    
    @Query("SELECT sfj FROM SuiviFormationJournalier sfj WHERE sfj.affectation = :affectation ORDER BY sfj.jour ASC")
    List<SuiviFormationJournalier> findByAffectation(@Param("affectation") AffectationFormation affectation);
    
    @Query("SELECT sfj FROM SuiviFormationJournalier sfj WHERE sfj.affectation.idAffectation = :affectationId AND sfj.jour = :jour")
    Optional<SuiviFormationJournalier> findByAffectationAndJour(@Param("affectationId") Long affectationId, @Param("jour") Integer jour);
    
    @Query("SELECT sfj FROM SuiviFormationJournalier sfj WHERE sfj.affectation = :affectation AND sfj.jour = :jour")
    Optional<SuiviFormationJournalier> findByAffectationAndJour(@Param("affectation") AffectationFormation affectation, @Param("jour") Integer jour);
    
    @Query("SELECT COUNT(sfj) FROM SuiviFormationJournalier sfj WHERE sfj.affectation.idAffectation = :affectationId")
    Integer countEntriesForAssignment(@Param("affectationId") Long affectationId);
    
    @Query("SELECT sfj FROM SuiviFormationJournalier sfj WHERE sfj.affectation.projet.idProjet = :projetId ORDER BY sfj.dateSaisie DESC")
    List<SuiviFormationJournalier> findByProjet(@Param("projetId") Long projetId);
}
