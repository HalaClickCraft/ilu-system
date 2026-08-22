package com.ilu.system.operator.controller;

import com.ilu.system.operator.dto.DailyBatchEntryDto;
import com.ilu.system.operator.dto.DailyTrackingDto;
import com.ilu.system.operator.dto.FormationDetailsDto;
import com.ilu.system.operator.dto.FormationStatisticsDto;
import com.ilu.system.operator.entity.DailyFormationTracking;
import com.ilu.system.operator.entity.WorkstationFormation;
import com.ilu.system.operator.service.TrainingService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/training")
public class TrainingController {
    private final TrainingService trainingService;

    public TrainingController(TrainingService trainingService) {
        this.trainingService = trainingService;
    }

    @PostMapping("/formations")
    public ResponseEntity<List<WorkstationFormation>> createFormations(@RequestParam Long workstationId,
                                                                         @RequestParam List<Long> operatorIds,
                                                                         Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                trainingService.createFormations(workstationId, operatorIds, authentication.getName(), roles(authentication)));
    }

    @GetMapping("/available-structure")
    public ResponseEntity<List<Map<String, Object>>> availableStructure(Authentication authentication) {
        return ResponseEntity.ok(trainingService.getAvailableStructure(authentication.getName(), roles(authentication)));
    }

    @GetMapping("/formations")
    public ResponseEntity<List<FormationDetailsDto>> listAll(Authentication authentication) {
        return ResponseEntity.ok(trainingService.listAllFormations(authentication.getName(), roles(authentication)));
    }

    @GetMapping("/formations/{id}")
    public ResponseEntity<FormationDetailsDto> getDetail(@PathVariable Long id) {
        return ResponseEntity.ok(trainingService.getFormationDetail(id));
    }

    @GetMapping("/formations/{id}/tracking")
    public ResponseEntity<List<Map<String, Object>>> getTracking(@PathVariable Long id) {
        List<DailyFormationTracking> tracking = trainingService.getFormationTracking(id);
        List<Map<String, Object>> result = tracking.stream().map(t -> {
            Map<String, Object> m = new java.util.LinkedHashMap<>();
            m.put("id", t.getId());
            m.put("trackingDate", t.getTrackingDate() != null ? t.getTrackingDate().toString() : null);
            m.put("dayNumber", t.getDayNumber());
            m.put("actualCadence", t.getActualCadence());
            m.put("defects", t.getDefects());
            m.put("cadenceSubmittedBy", t.getCadenceSubmittedBy());
            m.put("defectsSubmittedBy", t.getDefectsSubmittedBy());
            m.put("dailyLevel", t.getDailyLevel());
            m.put("comment", t.getComment());
            m.put("supervisor", t.getSupervisor());
            return m;
        }).collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    @PostMapping("/formations/{id}/tracking")
    public ResponseEntity<Map<String, Object>> addTracking(@PathVariable Long id, @RequestBody DailyTrackingDto dto,
                                                               Authentication authentication) {
        DailyFormationTracking saved = trainingService.addDailyTracking(id, dto, authentication.getName(), roles(authentication));
        return ResponseEntity.status(HttpStatus.CREATED).body(toTrackingMap(saved));
    }

    @PostMapping("/formations/{id}/batch-save")
    public ResponseEntity<List<Map<String, Object>>> batchSave(@PathVariable Long id,
                                                                    @RequestBody Map<String, List<DailyTrackingDto>> payload,
                                                                    Authentication authentication) {
        List<DailyFormationTracking> saved = trainingService.batchSaveDaily(id, payload.get("days"),
                authentication.getName(), roles(authentication));
        return ResponseEntity.ok(saved.stream().map(this::toTrackingMap).collect(Collectors.toList()));
    }

    @PostMapping("/daily-batch")
    public ResponseEntity<List<Map<String, Object>>> dailyBatch(
            @RequestBody Map<String, List<DailyBatchEntryDto>> payload, Authentication authentication) {
        List<DailyFormationTracking> saved = trainingService.saveDailyBatch(payload.get("entries"),
                authentication.getName(), roles(authentication));
        return ResponseEntity.ok(saved.stream().map(this::toTrackingMap).collect(Collectors.toList()));
    }

    @PostMapping("/formations/{id}/auto-evaluate")
    public ResponseEntity<Map<String, Object>> autoEvaluate(@PathVariable Long id, Authentication authentication) {
        return ResponseEntity.ok(trainingService.autoEvaluate(id, roles(authentication)));
    }

    @GetMapping("/formations/{id}/chart-data")
    public ResponseEntity<Map<String, Object>> getChartData(@PathVariable Long id) {
        return ResponseEntity.ok(trainingService.getChartData(id));
    }

    @PutMapping("/formations/{id}/quality-objective")
    public ResponseEntity<Map<String, Object>> setQualityObjective(@PathVariable Long id,
                                                                     @RequestBody Map<String, Integer> body,
                                                                     Authentication authentication) {
        if (!roles(authentication).contains("AGENT_QUALITE")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(trainingService.updateQualityObjective(id, body.get("qualityObjective")));
    }

    @PutMapping("/workstations/{workstationId}/quality-objective")
    public ResponseEntity<Map<String, Object>> setWorkstationQualityObjective(@PathVariable Long workstationId,
                                                                                @RequestBody Map<String, Integer> body,
                                                                                Authentication authentication) {
        if (!roles(authentication).contains("AGENT_QUALITE")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(trainingService.updateWorkstationQualityObjective(workstationId, body.get("qualityObjective")));
    }

    @PostMapping("/formations/{id}/reset")
    public ResponseEntity<Void> resetFormation(@PathVariable Long id, Authentication authentication) {
        trainingService.resetFormation(id, authentication.getName(), roles(authentication));
        return ResponseEntity.ok().build();
    }

    @GetMapping("/statistics")
    public ResponseEntity<FormationStatisticsDto> getStatistics() {
        return ResponseEntity.ok(trainingService.getStatistics());
    }

    private Map<String, Object> toTrackingMap(DailyFormationTracking t) {
        Map<String, Object> m = new java.util.LinkedHashMap<>();
        m.put("id", t.getId());
        m.put("trackingDate", t.getTrackingDate() != null ? t.getTrackingDate().toString() : null);
        m.put("dayNumber", t.getDayNumber());
        m.put("actualCadence", t.getActualCadence());
        m.put("defects", t.getDefects());
        m.put("cadenceSubmittedBy", t.getCadenceSubmittedBy());
        m.put("defectsSubmittedBy", t.getDefectsSubmittedBy());
        m.put("dailyLevel", t.getDailyLevel());
        m.put("comment", t.getComment());
        m.put("supervisor", t.getSupervisor());
        return m;
    }

    private Set<String> roles(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return Collections.emptySet();
        }
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .map(authority -> authority.startsWith("ROLE_") ? authority.substring(5) : authority)
                .collect(Collectors.toSet());
    }

}
