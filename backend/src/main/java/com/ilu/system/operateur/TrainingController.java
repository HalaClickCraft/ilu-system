package com.ilu.system.operateur;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST endpoints for multi-station training management (Gestion des formations multi-postes).
 * Authorization:
 * - POST /api/formations/initialize: Superviseur/RH/Admin only
 * - GET /api/formations: Available to all authenticated users
 * - POST /api/formations/{id}/journal: Chef d'Équipe (for their project only) + Superviseur/RH/Admin
 * - PUT /api/formations/journal/{id}: Chef d'Équipe (for their project only) + Superviseur/RH/Admin
 */
@RestController
@RequestMapping("/api/formations")
public class TrainingController {

    private final OperateurService operateurService;
    private final FormationService formationService;

    public TrainingController(OperateurService operateurService, FormationService formationService) {
        this.operateurService = operateurService;
        this.formationService = formationService;
    }

    /**
     * Initialize a new training assignment for an operator on a workstation.
     * Chef d'Équipe can add for their team, Superviseur/RH/Admin can add for anyone.
     * POST /api/formations/initialize
     * Body: { operateurMatricule, posteId, projetId }
     */
    @PostMapping("/initialize")
    @PreAuthorize("hasAnyRole('CHEF_EQUIPE', 'SUPERVISEUR', 'RH', 'ADMIN')")
    public AffectationFormation initializeTraining(
            @RequestBody Map<String, Object> payload,
            @AuthenticationPrincipal UserDetails userDetails) {
        String operateurMatricule = (String) payload.get("operateurMatricule");
        Long posteId = Long.valueOf(payload.get("posteId").toString());
        Long projetId = Long.valueOf(payload.get("projetId").toString());

        return operateurService.initializeTraining(operateurMatricule, posteId, projetId, userDetails.getUsername());
    }

    /**
     * Get all training assignments for a specific operator.
     * GET /api/formations/operateur/{matricule}
     */
    @GetMapping("/operateur/{matricule}")
    public List<AffectationFormation> getOperatorTrainingAssignments(@PathVariable String matricule) {
        return operateurService.getOperatorTrainingAssignments(matricule);
    }

    /**
     * Get all training assignments for a specific project.
     * GET /api/formations/projet/{projetId}
     */
    @GetMapping("/projet/{projetId}")
    public List<AffectationFormation> getProjectTrainingAssignments(@PathVariable Long projetId) {
        return operateurService.getProjectTrainingAssignments(projetId);
    }

    /**
     * Get all training assignments for a project filtered by status.
     * GET /api/formations/projet/{projetId}/statut/{statut}
     */
    @GetMapping("/projet/{projetId}/statut/{statut}")
    public List<AffectationFormation> getProjectTrainingsByStatus(
            @PathVariable Long projetId,
            @PathVariable String statut) {
        return operateurService.getProjectTrainingsByStatus(projetId, statut);
    }

    /**
     * Add a daily journal entry to a training assignment.
     * POST /api/formations/{affectationId}/journal
     * Body: { jour, cadenceRealisee, nbDefauts, remarques }
     * Available to: Chef d'Équipe (for their project only) + Superviseur/RH/Admin
     */
    @PostMapping("/{affectationId}/journal")
    @PreAuthorize("hasAnyRole('CHEF_EQUIPE', 'SUPERVISEUR', 'RH', 'ADMIN')")
    public SuiviFormationJournalier addDailyJournalEntry(
            @PathVariable Long affectationId,
            @RequestBody Map<String, Object> payload,
            @AuthenticationPrincipal UserDetails userDetails) {
        Integer jour = ((Number) payload.get("jour")).intValue();
        Integer cadenceRealisee = payload.get("cadenceRealisee") != null ? ((Number) payload.get("cadenceRealisee")).intValue() : null;
        Integer nbDefauts = payload.get("nbDefauts") != null ? ((Number) payload.get("nbDefauts")).intValue() : 0;
        String remarques = (String) payload.get("remarques");
        
        return operateurService.addDailyJournalEntry(affectationId, jour, cadenceRealisee, nbDefauts, remarques, userDetails.getUsername());
    }

    /**
     * Get all daily journal entries for a training assignment.
     * GET /api/formations/{affectationId}/journal
     */
    @GetMapping("/{affectationId}/journal")
    public List<SuiviFormationJournalier> getTrainingJournal(@PathVariable Long affectationId) {
        return operateurService.getTrainingJournal(affectationId);
    }

    /**
     * Update a daily journal entry.
     * PUT /api/formations/journal/{journalId}
     * Body: { cadenceRealisee, nbDefauts, remarques }
     */
    @PutMapping("/journal/{journalId}")
    @PreAuthorize("hasAnyRole('CHEF_EQUIPE', 'SUPERVISEUR', 'RH', 'ADMIN')")
    public SuiviFormationJournalier updateDailyJournalEntry(
            @PathVariable Long journalId,
            @RequestBody Map<String, Object> payload) {
        Integer cadenceRealisee = payload.get("cadenceRealisee") != null ? ((Number) payload.get("cadenceRealisee")).intValue() : null;
        Integer nbDefauts = payload.get("nbDefauts") != null ? ((Number) payload.get("nbDefauts")).intValue() : null;
        String remarques = (String) payload.get("remarques");
        
        return operateurService.updateDailyJournalEntry(journalId, cadenceRealisee, nbDefauts, remarques);
    }

    /**
     * Complete/evaluate a training assignment.
     * PUT /api/formations/{affectationId}/complete
     * Body: { statut: "EVALUEE" | "VALIDEE" | "ECHOUEE" }
     * Only Superviseur/RH/Admin
     */
    @PutMapping("/{affectationId}/complete")
    @PreAuthorize("hasAnyRole('SUPERVISEUR', 'RH', 'ADMIN')")
    public AffectationFormation completeTrainingAssignment(
            @PathVariable Long affectationId,
            @RequestBody Map<String, Object> payload) {
        String newStatus = (String) payload.get("statut");
        return operateurService.completeTrainingAssignment(affectationId, newStatus);
    }

    /**
     * Get trainings for Chef's team (role-scoped view).
     * GET /api/formations/mon-equipe
     * Chef d'Équipe sees only their team's trainings
     */
    @GetMapping("/mon-equipe")
    @PreAuthorize("hasAnyRole('CHEF_EQUIPE', 'RH', 'ADMIN', 'SUPERVISEUR')")
    public List<Map<String, Object>> getTeamTrainings(@AuthenticationPrincipal UserDetails userDetails) {
        return operateurService.getTeamTrainings(userDetails.getUsername());
    }

    /**
     * Get company-wide training statistics for HR/Admin/Superviseur dashboard.
     * GET /api/formations/stats
     * HR, Admin, and Superviseur only
     */
    @GetMapping("/stats")
    @PreAuthorize("hasAnyRole('RH', 'ADMIN', 'SUPERVISEUR')")
    public Map<String, Object> getTrainingStatistics() {
        return operateurService.getCompanyTrainingStatistics();
    }

    // ===== New Endpoints for Formation Tracking System =====

    /**
     * Create or update a formation template for a Poste.
     * POST /api/formations/templates
     * Body: { posteId, cadenceObjectif, qualiteObjectifTexte }
     * Only Superviseur/RH/Admin
     */
    @PostMapping("/templates")
    @PreAuthorize("hasAnyRole('SUPERVISEUR', 'RH', 'ADMIN')")
    public FormationTemplate createOrUpdateTemplate(
            @RequestBody Map<String, Object> payload,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long posteId = Long.valueOf(payload.get("posteId").toString());
        Integer cadenceObjectif = ((Number) payload.get("cadenceObjectif")).intValue();
        String qualiteObjectifTexte = (String) payload.get("qualiteObjectifTexte");

        return formationService.createOrUpdateTemplate(posteId, cadenceObjectif, qualiteObjectifTexte, userDetails.getUsername());
    }

    /**
     * Get formation template by Poste ID.
     * GET /api/formations/templates/{posteId}
     */
    @GetMapping("/templates/{posteId}")
    public FormationTemplate getTemplateByPosteId(@PathVariable Long posteId) {
        return formationService.getTemplateByPosteId(posteId);
    }

    /**
     * Get all formation templates.
     * GET /api/formations/templates
     */
    @GetMapping("/templates")
    public List<FormationTemplate> getAllTemplates() {
        return formationService.getAllTemplates();
    }

    /**
     * Get formation details with all tracking data.
     * GET /api/formations/{affectationId}/details
     */
    @GetMapping("/{affectationId}/details")
    public FormationDetailsDto getFormationDetails(@PathVariable Long affectationId) {
        return formationService.getFormationDetails(affectationId);
    }

    /**
     * Get chart data for a formation (Cadence réalisée vs Cadence objectif).
     * GET /api/formations/{affectationId}/chart-data
     */
    @GetMapping("/{affectationId}/chart-data")
    public ChartDataDto getChartData(@PathVariable Long affectationId) {
        return formationService.getChartData(affectationId);
    }

    /**
     * Record daily cadence and defects for a formation.
     * PUT /api/formations/{affectationId}/daily/{day}
     * Body: { cadenceRealisee, nbDefauts, remarques }
     * Available to: Chef d'Équipe + Superviseur/RH/Admin
     */
    @PutMapping("/{affectationId}/daily/{day}")
    @PreAuthorize("hasAnyRole('CHEF_EQUIPE', 'SUPERVISEUR', 'RH', 'ADMIN')")
    public DailyTrackingDto recordDailyTracking(
            @PathVariable Long affectationId,
            @PathVariable Integer day,
            @RequestBody Map<String, Object> payload,
            @AuthenticationPrincipal UserDetails userDetails) {
        Integer cadenceRealisee = payload.get("cadenceRealisee") != null ? ((Number) payload.get("cadenceRealisee")).intValue() : null;
        Integer nbDefauts = payload.get("nbDefauts") != null ? ((Number) payload.get("nbDefauts")).intValue() : 0;
        String remarques = (String) payload.get("remarques");

        return formationService.recordDailyTracking(affectationId, day, cadenceRealisee, nbDefauts, remarques, userDetails.getUsername());
    }
}