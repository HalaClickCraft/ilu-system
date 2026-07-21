package com.ilu.system.operateur;

import com.ilu.system.structure.PosteTravail;
import com.ilu.system.structure.PosteTravailRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for formation template management and formation tracking operations.
 */
@Service
@Transactional
public class FormationService {

    private final FormationTemplateRepository formationTemplateRepository;
    private final PosteTravailRepository posteTravailRepository;
    private final AffectationFormationRepository affectationFormationRepository;
    private final SuiviFormationJournalierRepository suiviFormationJournalierRepository;

    public FormationService(FormationTemplateRepository formationTemplateRepository,
                            PosteTravailRepository posteTravailRepository,
                            AffectationFormationRepository affectationFormationRepository,
                            SuiviFormationJournalierRepository suiviFormationJournalierRepository) {
        this.formationTemplateRepository = formationTemplateRepository;
        this.posteTravailRepository = posteTravailRepository;
        this.affectationFormationRepository = affectationFormationRepository;
        this.suiviFormationJournalierRepository = suiviFormationJournalierRepository;
    }

    /**
     * Create or update a formation template for a Poste.
     */
    public FormationTemplate createOrUpdateTemplate(Long posteId, Integer cadenceObjectif, String qualiteObjectifTexte, String creePar) {
        PosteTravail poste = posteTravailRepository.findById(posteId)
                .orElseThrow(() -> new RuntimeException("Poste not found with id: " + posteId));

        Optional<FormationTemplate> existingTemplate = formationTemplateRepository.findByPoste(poste);
        
        FormationTemplate template;
        if (existingTemplate.isPresent()) {
            template = existingTemplate.get();
            template.setCadenceObjectif(cadenceObjectif);
            template.setQualiteObjectifTexte(qualiteObjectifTexte);
            template.setModifiePar(creePar);
        } else {
            template = new FormationTemplate(poste, cadenceObjectif, qualiteObjectifTexte, creePar);
        }

        return formationTemplateRepository.save(template);
    }

    /**
     * Get formation template by Poste ID.
     */
    public FormationTemplate getTemplateByPosteId(Long posteId) {
        return formationTemplateRepository.findByPosteIdPoste(posteId)
                .orElseThrow(() -> new RuntimeException("Formation template not found for poste: " + posteId));
    }

    /**
     * Get all formation templates.
     */
    public List<FormationTemplate> getAllTemplates() {
        return formationTemplateRepository.findAll();
    }

    /**
     * Get formation details by affectation ID.
     */
    public FormationDetailsDto getFormationDetails(Long affectationId) {
        AffectationFormation affectation = affectationFormationRepository.findById(affectationId)
                .orElseThrow(() -> new RuntimeException("Affectation not found with id: " + affectationId));

        FormationDetailsDto details = new FormationDetailsDto(
                affectation.getIdAffectation(),
                affectation.getOperateur().getMatricule(),
                affectation.getOperateur().getNom(),
                affectation.getOperateur().getPrenom(),
                affectation.getPoste().getIdPoste(),
                affectation.getPoste().getNom(),
                affectation.getProjet().getIdProjet(),
                affectation.getProjet().getNom(),
                affectation.getPoste().getCadenceObjectif(),
                affectation.getQualiteObjectif(),
                affectation.isEstAffectationPrimaire(),
                affectation.getStatut(),
                affectation.getDateDebut(),
                affectation.getDateEvaluationPrevue()
        );

        // Get daily trackings
        List<SuiviFormationJournalier> journalEntries = suiviFormationJournalierRepository.findByAffectation(affectation);
        List<DailyTrackingDto> dailyTrackings = journalEntries.stream()
                .map(entry -> new DailyTrackingDto(
                        entry.getIdSuivi(),
                        entry.getJour(),
                        entry.getCadenceRealisee(),
                        entry.getNbDefauts(),
                        entry.getRemarques()
                ))
                .sorted(Comparator.comparingInt(DailyTrackingDto::getJour))
                .collect(Collectors.toList());

        details.setDailyTrackings(dailyTrackings);

        // Calculate statistics
        FormationStatisticsDto stats = calculateStatistics(dailyTrackings, affectation.getQualiteObjectif());
        details.setStatistics(stats);

        return details;
    }

    /**
     * Get chart data for a formation.
     */
    public ChartDataDto getChartData(Long affectationId) {
        AffectationFormation affectation = affectationFormationRepository.findById(affectationId)
                .orElseThrow(() -> new RuntimeException("Affectation not found with id: " + affectationId));

        List<SuiviFormationJournalier> journalEntries = suiviFormationJournalierRepository.findByAffectation(affectation);

        // Create day labels (J1 to J12)
        List<Integer> labels = new ArrayList<>();
        for (int i = 1; i <= 12; i++) {
            labels.add(i);
        }

        // Create datasets
        List<Integer> cadenceObjectifData = new ArrayList<>();
        List<Integer> cadenceRealiseeData = new ArrayList<>();

        final Integer cadenceObjectif = affectation.getPoste().getCadenceObjectif();

        for (int day = 1; day <= 12; day++) {
            cadenceObjectifData.add(cadenceObjectif);

            final int currentDay = day;
            Optional<SuiviFormationJournalier> entry = journalEntries.stream()
                    .filter(e -> e.getJour().equals(currentDay))
                    .findFirst();

            cadenceRealiseeData.add(entry.map(SuiviFormationJournalier::getCadenceRealisee).orElse(null));
        }

        // Create chart datasets
        Map<String, Object> cadenceObjectifDataset = new HashMap<>();
        cadenceObjectifDataset.put("label", "Cadence objectif du poste");
        cadenceObjectifDataset.put("data", cadenceObjectifData);
        cadenceObjectifDataset.put("borderColor", "#00a86b");
        cadenceObjectifDataset.put("backgroundColor", "rgba(0, 168, 107, 0.1)");
        cadenceObjectifDataset.put("borderWidth", 2);
        cadenceObjectifDataset.put("tension", 0.4);

        Map<String, Object> cadenceRealiseeDataset = new HashMap<>();
        cadenceRealiseeDataset.put("label", "Cadence réalisée");
        cadenceRealiseeDataset.put("data", cadenceRealiseeData);
        cadenceRealiseeDataset.put("borderColor", "#0066cc");
        cadenceRealiseeDataset.put("backgroundColor", "rgba(0, 102, 204, 0.1)");
        cadenceRealiseeDataset.put("borderWidth", 2);
        cadenceRealiseeDataset.put("tension", 0.4);

        return new ChartDataDto(labels, cadenceObjectifDataset, cadenceRealiseeDataset);
    }

    /**
     * Calculate statistics for formation.
     */
    private FormationStatisticsDto calculateStatistics(List<DailyTrackingDto> dailyTrackings, String qualiteObjectif) {
        if (dailyTrackings.isEmpty()) {
            return new FormationStatisticsDto(0.0, 0, 0, 0.0, false);
        }

        List<Integer> cadencesWithData = dailyTrackings.stream()
                .filter(d -> d.getCadenceRealisee() != null)
                .map(DailyTrackingDto::getCadenceRealisee)
                .collect(Collectors.toList());

        Double cadenceMoyenne = cadencesWithData.isEmpty() ? 0.0 : 
                cadencesWithData.stream()
                        .mapToDouble(Integer::doubleValue)
                        .average()
                        .orElse(0.0);

        Integer totalDefauts = dailyTrackings.stream()
                .mapToInt(d -> d.getNbDefauts() != null ? d.getNbDefauts() : 0)
                .sum();

        Integer daysWithData = (int) cadencesWithData.size();
        Double percentageOfDaysWithData = (daysWithData * 100.0) / 12.0;

        // Quality objective is met if total defects <= expected limit
        // Assuming qualiteObjectif format like "< 7 defects in 12 days"
        boolean qualityMet = parseQualityObjective(qualiteObjectif, totalDefauts);

        return new FormationStatisticsDto(cadenceMoyenne, totalDefauts, daysWithData, percentageOfDaysWithData, qualityMet);
    }

    /**
     * Parse quality objective and check if it's met.
     * Expected format: "< 7 defects in 12 days" or similar
     */
    private boolean parseQualityObjective(String qualiteObjectif, Integer totalDefauts) {
        if (qualiteObjectif == null || qualiteObjectif.isEmpty()) {
            return true; // No objective = considered met
        }

        try {
            // Extract number from quality objective text
            String[] parts = qualiteObjectif.split("\\D+");
            for (String part : parts) {
                if (!part.isEmpty()) {
                    int limit = Integer.parseInt(part);
                    return totalDefauts <= limit;
                }
            }
        } catch (Exception e) {
            // If parsing fails, consider objective met
            return true;
        }

        return true;
    }

    /**
     * Record daily cadence and defects for a formation.
     */
    public DailyTrackingDto recordDailyTracking(Long affectationId, Integer jour, Integer cadenceRealisee, Integer nbDefauts, String remarques, String saisieePar) {
        AffectationFormation affectation = affectationFormationRepository.findById(affectationId)
                .orElseThrow(() -> new RuntimeException("Affectation not found with id: " + affectationId));

        Optional<SuiviFormationJournalier> existingEntry = suiviFormationJournalierRepository
                .findByAffectationAndJour(affectation, jour);

        SuiviFormationJournalier entry;
        if (existingEntry.isPresent()) {
            entry = existingEntry.get();
            entry.setCadenceRealisee(cadenceRealisee);
            entry.setNbDefauts(nbDefauts != null ? nbDefauts : 0);
            entry.setRemarques(remarques);
        } else {
            entry = new SuiviFormationJournalier(affectation, jour);
            entry.setCadenceRealisee(cadenceRealisee);
            entry.setNbDefauts(nbDefauts != null ? nbDefauts : 0);
            entry.setRemarques(remarques);
        }

        entry = suiviFormationJournalierRepository.save(entry);

        return new DailyTrackingDto(entry.getIdSuivi(), entry.getJour(), entry.getCadenceRealisee(), entry.getNbDefauts(), entry.getRemarques());
    }
}
