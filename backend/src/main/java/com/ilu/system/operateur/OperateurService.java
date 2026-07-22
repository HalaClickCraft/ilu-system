package com.ilu.system.operateur;

import com.ilu.system.auth.Utilisateur;
import com.ilu.system.auth.UtilisateurRepository;
import com.ilu.system.structure.PosteTravail;
import com.ilu.system.structure.PosteTravailRepository;
import com.ilu.system.structure.ProjectMemberRepository;
import com.ilu.system.structure.Project;
import com.ilu.system.structure.ProjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class OperateurService {

    private final OperateurRepository operateurRepository;
    private final EquipeRepository equipeRepository;
    private final PosteTravailRepository posteTravailRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final FormationPosteRepository formationPosteRepository;
    private final ProjectRepository projectRepository;
    private final AffectationFormationRepository affectationFormationRepository;
    private final SuiviFormationJournalierRepository suiviFormationJournalierRepository;
    private final FormationTemplateRepository formationTemplateRepository;

    @Autowired
    public OperateurService(OperateurRepository operateurRepository,
                            EquipeRepository equipeRepository,
                            PosteTravailRepository posteTravailRepository,
                            UtilisateurRepository utilisateurRepository,
                            ProjectMemberRepository projectMemberRepository,
                            FormationPosteRepository formationPosteRepository,
                            AffectationFormationRepository affectationFormationRepository,
                            SuiviFormationJournalierRepository suiviFormationJournalierRepository,
                            ProjectRepository projectRepository,
                            FormationTemplateRepository formationTemplateRepository) {
        this.operateurRepository = operateurRepository;
        this.equipeRepository = equipeRepository;
        this.posteTravailRepository = posteTravailRepository;
        this.utilisateurRepository = utilisateurRepository;
        this.projectMemberRepository = projectMemberRepository;
        this.formationPosteRepository = formationPosteRepository;
        this.affectationFormationRepository = affectationFormationRepository;
        this.suiviFormationJournalierRepository = suiviFormationJournalierRepository;
        this.projectRepository = projectRepository;
        this.formationTemplateRepository = formationTemplateRepository;
    }

    @Transactional(readOnly = true)
    public List<Operateur> getAllOperators() {
        return operateurRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Operateur> getTeamOperators(String chefMatricule) {
        Utilisateur user = utilisateurRepository.findByMatricule(chefMatricule)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Utilisateur chef introuvable avec matricule: " + chefMatricule));
        List<Long> projetIds = equipeRepository.findByChef_Id(user.getId()).stream()
                .map(Equipe::getProjet)
                .filter(java.util.Objects::nonNull)
                .map(projet -> projet.getIdProjet())
                .toList();
        return projetIds.isEmpty() ? List.of() : operateurRepository.findByPosteAffecte_Zone_Projet_IdProjetIn(projetIds);
    }

    @Transactional(readOnly = true)
    public List<Equipe> getTeamsByChef(String chefMatricule) {
        Utilisateur user = utilisateurRepository.findByMatricule(chefMatricule)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Utilisateur chef introuvable avec matricule: " + chefMatricule));
        return equipeRepository.findByChef_Id(user.getId());
    }

    @Transactional(readOnly = true)
    public List<Equipe> getAllTeams() {
        return equipeRepository.findAll();
    }

    @Transactional
    public Operateur createOperator(CreateOperateurRequest request) {
        if (operateurRepository.existsById(request.getMatricule())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Un opérateur avec le matricule " + request.getMatricule() + " existe déjà.");
        }

        Operateur operateur = new Operateur();
        operateur.setMatricule(request.getMatricule());
        operateur.setNom(request.getNom());
        operateur.setPrenom(request.getPrenom());
        operateur.setFonctionnalite(request.getFonctionnalite());

        if (request.getDateEmbauche() != null && !request.getDateEmbauche().isBlank()) {
            operateur.setDateEmbauche(LocalDate.parse(request.getDateEmbauche()));
        } else {
            operateur.setDateEmbauche(LocalDate.now());
        }

        if (request.getDateSortie() != null && !request.getDateSortie().isBlank()) {
            operateur.setDateSortie(LocalDate.parse(request.getDateSortie()));
        }

        // Une création est toujours une nouvelle recrue. Son poste courant reste NULL
        // jusqu'à la première affectation de formation.
        operateur.setStatut("NOUVELLE_RECRUE");
        operateur.setFormationRework(request.isFormationRework());

        if (request.getPosteId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Le premier poste d'affectation est obligatoire.");
        }
        PosteTravail poste = posteTravailRepository.findById(request.getPosteId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Poste de travail introuvable avec ID: " + request.getPosteId()));
        operateur.setPosteAffecte(poste);
        Operateur savedOperateur = operateurRepository.save(operateur);
        formationPosteRepository.save(FormationPoste.enFormation(savedOperateur, poste));
        return savedOperateur;
    }

 @Transactional
    public Operateur updateStatus(String matricule, String statut) {
        Operateur operateur = operateurRepository.findById(matricule)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Opérateur introuvable avec le matricule: " + matricule));
        operateur.setStatut(statut);
        
        if ("Sorti".equalsIgnoreCase(statut) && operateur.getDateSortie() == null) {
            operateur.setDateSortie(LocalDate.now());
        } else if (!"Sorti".equalsIgnoreCase(statut)) {
            operateur.setDateSortie(null);
        }
        
        return operateurRepository.save(operateur);
    }

    private static final java.util.Set<String> MOTIFS_ABSENCE_VALIDES = java.util.Set.of("MALADIE", "ACCOUCHEMENT");

    /**
     * Marque un opérateur en arrêt maladie ou accouchement (arrêt de 3 mois).
     * Réservé au rôle RH.
     */
    @Transactional
    public Operateur marquerAbsence(String matricule, String motif) {
        Operateur operateur = operateurRepository.findById(matricule)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Opérateur introuvable avec le matricule: " + matricule));

        String motifNormalise = motif == null ? null : motif.trim().toUpperCase();
        if (motifNormalise == null || !MOTIFS_ABSENCE_VALIDES.contains(motifNormalise)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Le motif d'absence doit être MALADIE ou ACCOUCHEMENT.");
        }

        operateur.setStatut("ABSENT");
        operateur.setMotifAbsence(motifNormalise);
        operateur.setDateDebutAbsence(LocalDate.now());
        operateur.setDateReprisePrevue(LocalDate.now().plusMonths(3));
        return operateurRepository.save(operateur);
    }

    /**
     * Marque la reprise (retour) d'un opérateur absent pour maladie ou accouchement.
     * Accessible au Chef d'équipe et au RH.
     */
    @Transactional
    public Operateur marquerReprise(String matricule) {
        Operateur operateur = operateurRepository.findById(matricule)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Opérateur introuvable avec le matricule: " + matricule));

        if (!"ABSENT".equalsIgnoreCase(operateur.getStatut())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cet opérateur n'est pas actuellement en arrêt maladie/accouchement.");
        }

        operateur.setStatut("Actif");
        operateur.setMotifAbsence(null);
        operateur.setDateDebutAbsence(null);
        operateur.setDateReprisePrevue(null);
        return operateurRepository.save(operateur);
    }

    @Transactional
    public Operateur assignToWorkstation(String matricule, Long posteId) {
        Operateur operateur = operateurRepository.findById(matricule)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Opérateur introuvable avec le matricule: " + matricule));

        if (posteId == null) {
            operateur.setPosteAffecte(null);
        } else {
            PosteTravail poste = posteTravailRepository.findById(posteId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Poste de travail introuvable avec ID: " + posteId));
            operateur.setPosteAffecte(poste);
            formationPosteRepository.findByOperateur_MatriculeAndPoste_IdPoste(matricule, posteId)
                    .orElseGet(() -> formationPosteRepository.save(
                            FormationPoste.enFormation(operateur, poste)));
        }

        return operateurRepository.save(operateur);
    }

    /** Les responsables sont calculés depuis poste -> zone -> projet, jamais saisis manuellement. */
    @Transactional(readOnly = true)
    public List<EncadrementDto> getEncadrement(String matricule) {
        Operateur operateur = operateurRepository.findById(matricule)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Opérateur introuvable avec le matricule: " + matricule));
        if (operateur.getPosteAffecte() == null || operateur.getPosteAffecte().getZone() == null
                || operateur.getPosteAffecte().getZone().getProjet() == null) {
            return List.of();
        }
        Long projetId = operateur.getPosteAffecte().getZone().getProjet().getIdProjet();
        List<EncadrementDto> encadrement = projectMemberRepository.findByProjet_IdProjet(projetId).stream()
                .map(EncadrementDto::from)
                .collect(java.util.stream.Collectors.toList());
        equipeRepository.findByProjet_IdProjet(projetId).stream()
                .map(Equipe::getChef)
                .filter(java.util.Objects::nonNull)
                .filter(chef -> encadrement.stream().noneMatch(item -> item.utilisateurId().equals(chef.getId())))
                .forEach(chef -> encadrement.add(0, new EncadrementDto(chef.getId(), chef.getMatricule(), chef.getNom(), "CHEF_EQUIPE")));
        return encadrement;
    }

    @Transactional(readOnly = true)
    public List<FormationPoste> getFormations(String matricule) {
        if (!operateurRepository.existsById(matricule)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Opérateur introuvable avec le matricule: " + matricule);
        }
        return formationPosteRepository.findByOperateur_Matricule(matricule);
    }

    /**
     * Initialize a training pipeline for an operator on a specific workstation.
     * Only Superviseur/RH/Admin can call this.
     * Creates an AffectationFormation record and links to FormationTemplate if exists.
     */
    @Transactional
    public AffectationFormation initializeTraining(String operateurMatricule, Long posteId, Long projetId, String creePar) {
        Operateur operateur = operateurRepository.findById(operateurMatricule)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Opérateur introuvable: " + operateurMatricule));
        
        PosteTravail poste = posteTravailRepository.findById(posteId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Poste introuvable: " + posteId));
        
        Project projet = projectRepository.findById(projetId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Projet introuvable: " + projetId));
        
        Utilisateur user = utilisateurRepository.findByMatricule(creePar)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Utilisateur créateur introuvable: " + creePar));
        
        // Check if already exists
        java.util.Optional<AffectationFormation> existing = affectationFormationRepository.findByOperateur_PosteAndProjet(operateurMatricule, posteId, projetId);
        if (existing.isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Une formation existe déjà pour cet opérateur sur ce poste dans ce projet.");
        }
        
        // Determine if this is primary or secondary
        java.util.Optional<AffectationFormation> primaryAssignment = affectationFormationRepository.findPrimaryAssignmentByOperateur(operateurMatricule);
        boolean isPrimary = primaryAssignment.isEmpty();
        
        AffectationFormation af = isPrimary 
            ? AffectationFormation.enFormationPrimaire(operateur, poste, projet, user)
            : AffectationFormation.enFormationSecondaire(operateur, poste, projet, user);
        
        // Get formation template and set quality objective if exists
        java.util.Optional<FormationTemplate> template = formationTemplateRepository.findByPoste(poste);
        if (template.isPresent()) {
            af.setQualiteObjectif(template.get().getQualiteObjectifTexte());
        }
        
        return affectationFormationRepository.save(af);
    }

    /**
     * Get all training assignments for an operator.
     */
    @Transactional(readOnly = true)
    public List<AffectationFormation> getOperatorTrainingAssignments(String operateurMatricule) {
        return affectationFormationRepository.findByOperateur_Matricule(operateurMatricule);
    }

    /**
     * Get all training assignments for a project.
     * Scoped access: Chef d'Équipe only sees their own project; others see globally.
     */
    @Transactional(readOnly = true)
    public List<AffectationFormation> getProjectTrainingAssignments(Long projetId) {
        return affectationFormationRepository.findByProjet_IdProjet(projetId);
    }

    /**
     * Get all training assignments for a project filtered by status.
     */
    @Transactional(readOnly = true)
    public List<AffectationFormation> getProjectTrainingsByStatus(Long projetId, String statut) {
        return affectationFormationRepository.findByProjet_IdProjet_AndStatut(projetId, statut);
    }

    /**
     * Add a daily journal entry to a training assignment.
     * Only Chef d'Équipe assigned to that project can add.
     */
    @Transactional
    public SuiviFormationJournalier addDailyJournalEntry(Long affectationId, Integer jour, Integer cadenceRealisee, Integer nbDefauts, String remarques, String saisieParMatricule) {
        AffectationFormation af = affectationFormationRepository.findById(affectationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Affectation introuvable: " + affectationId));
        
        Utilisateur saisieePar = utilisateurRepository.findByMatricule(saisieParMatricule)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Utilisateur saisisseur introuvable: " + saisieParMatricule));
        
        // Validate jour is 1-12
        if (jour == null || jour < 1 || jour > 12) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Le jour doit être entre 1 et 12.");
        }
        
        // Check if entry already exists for this day
        java.util.Optional<SuiviFormationJournalier> existing = suiviFormationJournalierRepository.findByAffectationAndJour(affectationId, jour);
        if (existing.isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Une entrée existe déjà pour le jour " + jour + ".");
        }
        
        SuiviFormationJournalier entry = new SuiviFormationJournalier(af, jour);
        entry.setCadenceRealisee(cadenceRealisee);
        entry.setNbDefauts(nbDefauts != null ? nbDefauts : 0);
        entry.setRemarques(remarques);
        entry.setSaisieePar(saisieePar);
        
        return suiviFormationJournalierRepository.save(entry);
    }

    /**
     * Get all daily journal entries for a training assignment.
     */
    @Transactional(readOnly = true)
    public List<SuiviFormationJournalier> getTrainingJournal(Long affectationId) {
        return suiviFormationJournalierRepository.findByAffectation_IdAffectation(affectationId);
    }

    /**
     * Update a daily journal entry.
     */
    @Transactional
    public SuiviFormationJournalier updateDailyJournalEntry(Long journalId, Integer cadenceRealisee, Integer nbDefauts, String remarques) {
        SuiviFormationJournalier entry = suiviFormationJournalierRepository.findById(journalId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Entrée journal introuvable: " + journalId));
        
        if (cadenceRealisee != null) {
            entry.setCadenceRealisee(cadenceRealisee);
        }
        if (nbDefauts != null) {
            entry.setNbDefauts(nbDefauts);
        }
        if (remarques != null) {
            entry.setRemarques(remarques);
        }
        
        return suiviFormationJournalierRepository.save(entry);
    }

    /**
     * Complete/evaluate a training assignment.
     */
    @Transactional
    public AffectationFormation completeTrainingAssignment(Long affectationId, String newStatus) {
        AffectationFormation af = affectationFormationRepository.findById(affectationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Affectation introuvable: " + affectationId));
        
        if (!newStatus.matches("EVALUEE|VALIDEE|ECHOUEE")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Statut invalide: " + newStatus);
        }
        
        af.setStatut(newStatus);
        affectationFormationRepository.save(af);

        // L'affectation avait déjà créé la ligne dans la matrice (OPERATEUR_POSTE)
        // avec statut = EN_FORMATION. Ici on la met à jour avec le résultat réel de
        // l'évaluation, qui arrive plus tard (souvent après les 12 jours de suivi).
        formationPosteRepository.findByOperateur_MatriculeAndPoste_IdPoste(
                af.getOperateur().getMatricule(), af.getPoste().getIdPoste()
        ).ifPresent(fp -> {
            fp.setStatut(newStatus.equals("VALIDEE") ? "CONFIRME"
                    : newStatus.equals("ECHOUEE") ? "ECHEC"
                    : "EN_EVALUATION");
            fp.setDateEvaluationReelle(LocalDate.now());
            formationPosteRepository.save(fp);
        });

        return af;
    }

    /**
     * Get trainings for Chef's team (role-scoped view).
     * Returns training data with progress info.
     */
    @Transactional(readOnly = true)
    public java.util.List<java.util.Map<String, Object>> getTeamTrainings(String username) {
        Utilisateur user = utilisateurRepository.findByMatricule(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Utilisateur: " + username));
        
        if (!"CHEF_EQUIPE".equals(user.getRole().getLibelle().toString())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Accès non autorisé");
        }
        
        // Get trainings for this chef's team
        List<AffectationFormation> trainings = affectationFormationRepository.findByChefEquipe(user.getId());
        
        return trainings.stream().map(t -> {
            java.util.Map<String, Object> map = new java.util.HashMap<>();
            map.put("idAffectation", t.getIdAffectation());
            map.put("operateurNom", t.getOperateur().getNom());
            map.put("operateurMatricule", t.getOperateur().getMatricule());
            map.put("posteNom", t.getPoste().getNom());
            map.put("projetNom", t.getProjet().getNom());
            map.put("statut", t.getStatut());
            map.put("estAffectationPrimaire", t.isEstAffectationPrimaire());
            map.put("dateDebut", t.getDateDebut());
            
            // Calculate progress
            long daysLogged = suiviFormationJournalierRepository.findByAffectation_IdAffectation(t.getIdAffectation()).stream()
                    .map(SuiviFormationJournalier::getJour).max(Integer::compareTo).orElse(0);
            map.put("dernierJourSaisi", daysLogged);
            
            return map;
        }).toList();
    }

    /**
     * Get company-wide training statistics for HR/Admin dashboard.
     */
    @Transactional(readOnly = true)
    public java.util.Map<String, Object> getCompanyTrainingStatistics() {
        List<AffectationFormation> allTrainings = affectationFormationRepository.findAll();
        
        long totalTrainings = allTrainings.size();
        long enFormation = allTrainings.stream().filter(t -> "EN_FORMATION".equals(t.getStatut())).count();
        long validees = allTrainings.stream().filter(t -> "VALIDEE".equals(t.getStatut())).count();
        long enAttente = allTrainings.stream().filter(t -> "EN_ATTENTE".equals(t.getStatut())).count();
        
        long completionRate = totalTrainings > 0 ? (validees * 100) / totalTrainings : 0;
        
        // Status breakdown
        java.util.Map<String, Long> byStatus = new java.util.HashMap<>();
        byStatus.put("EN_FORMATION", enFormation);
        byStatus.put("VALIDEE", validees);
        byStatus.put("EN_ATTENTE", enAttente);
        
        // Overall stats
        java.util.Map<String, Object> overall = new java.util.HashMap<>();
        overall.put("totalTrainings", totalTrainings);
        overall.put("enFormation", enFormation);
        overall.put("validees", validees);
        overall.put("completionRate", completionRate);
        
        // All trainings with details
        java.util.List<java.util.Map<String, Object>> trainings = allTrainings.stream().map(t -> {
            java.util.Map<String, Object> map = new java.util.HashMap<>();
            map.put("idAffectation", t.getIdAffectation());
            map.put("operateurNom", t.getOperateur().getNom());
            map.put("operateurMatricule", t.getOperateur().getMatricule());
            map.put("posteNom", t.getPoste().getNom());
            map.put("projetNom", t.getProjet().getNom());
            map.put("chefEquipeNom", "Chef Team"); // TODO: Get from project_member
            map.put("statut", t.getStatut());
            map.put("dateDebut", t.getDateDebut());
            
            // Calculate progress
            long daysLogged = suiviFormationJournalierRepository.findByAffectation_IdAffectation(t.getIdAffectation()).stream()
                    .map(SuiviFormationJournalier::getJour).max(Integer::compareTo).orElse(0);
            map.put("dernierJourSaisi", daysLogged);
            
            return map;
        }).toList();
        
        java.util.Map<String, Object> result = new java.util.HashMap<>();
        result.put("overall", overall);
        result.put("byStatus", byStatus);
        result.put("trainings", trainings);
        
        return result;
    }
}