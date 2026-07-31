package com.ilu.system.operator.controller;
import com.ilu.system.operator.dto.DailyTrackingDto;
import com.ilu.system.operator.dto.FormationDetailsDto;
import com.ilu.system.operator.dto.FormationStatisticsDto;
import com.ilu.system.operator.entity.DailyFormationTracking;
import com.ilu.system.operator.entity.WorkstationFormation;
import com.ilu.system.operator.service.TrainingService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
@RestController
@RequestMapping("/api/training")
public class TrainingController {
    private final TrainingService trainingService;
    public TrainingController(TrainingService trainingService) { this.trainingService = trainingService; }
    @PostMapping("/formations")
    public ResponseEntity<List<WorkstationFormation>> createFormations(@RequestParam Long workstationId, @RequestParam List<Long> operatorIds) {
        return ResponseEntity.status(HttpStatus.CREATED).body(trainingService.createFormations(workstationId, operatorIds));
    }
    @GetMapping("/formations")
    public ResponseEntity<List<FormationDetailsDto>> listAll() { return ResponseEntity.ok(trainingService.listAllFormations()); }
    @GetMapping("/formations/{id}")
    public ResponseEntity<FormationDetailsDto> getDetail(@PathVariable Long id) { return ResponseEntity.ok(trainingService.getFormationDetail(id)); }
    @GetMapping("/formations/{id}/tracking")
    public ResponseEntity<List<DailyFormationTracking>> getTracking(@PathVariable Long id) { return ResponseEntity.ok(trainingService.getFormationTracking(id)); }
    @PostMapping("/formations/{id}/tracking")
    public ResponseEntity<DailyFormationTracking> addTracking(@PathVariable Long id, @RequestBody DailyTrackingDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(trainingService.addDailyTracking(id, dto));
    }
    /**
     * Batch save all 12 days at once.
     * Body: { "days": [ { "dayNumber": 1, "actualCadence": 100, "defects": 2 }, ... ] }
     */
    @PostMapping("/formations/{id}/batch-save")
    public ResponseEntity<List<DailyFormationTracking>> batchSave(@PathVariable Long id, @RequestBody Map<String, List<DailyTrackingDto>> payload) {
        List<DailyTrackingDto> days = payload.get("days");
        return ResponseEntity.ok(trainingService.batchSaveDaily(id, days));
    }
    /**
     * Auto-evaluate: calculates moyenne + total defects, returns pass/fail with details.
     */
    @PostMapping("/formations/{id}/auto-evaluate")
    public ResponseEntity<Map<String, Object>> autoEvaluate(@PathVariable Long id) {
        return ResponseEntity.ok(trainingService.autoEvaluate(id));
    }
    /**
     * Chart data endpoint for the growth diagram.
     */
    @GetMapping("/formations/{id}/chart-data")
    public ResponseEntity<Map<String, Object>> getChartData(@PathVariable Long id) {
        return ResponseEntity.ok(trainingService.getChartData(id));
    }
    @PostMapping("/formations/{id}/reset")
    public ResponseEntity<Void> resetFormation(@PathVariable Long id) { trainingService.resetFormation(id); return ResponseEntity.ok().build(); }
    @GetMapping("/statistics")
    public ResponseEntity<FormationStatisticsDto> getStatistics() { return ResponseEntity.ok(trainingService.getStatistics()); }
}
