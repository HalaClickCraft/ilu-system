package com.ilu.system.operator.controller;
import com.ilu.system.operator.dto.DailyTrackingDto;
import com.ilu.system.operator.dto.FormationDetailsDto;
import com.ilu.system.operator.dto.FormationStatisticsDto;
import com.ilu.system.operator.entity.DailyFormationTracking;
import com.ilu.system.operator.entity.FormationAssignment;
import com.ilu.system.operator.entity.WorkstationFormation;
import com.ilu.system.operator.service.TrainingService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
@RestController
@RequestMapping("/api/training")
public class TrainingController {
    private final TrainingService trainingService;
    public TrainingController(TrainingService trainingService) { this.trainingService = trainingService; }
    @PostMapping("/formations")
    public ResponseEntity<WorkstationFormation> createFormation(@RequestParam Long operatorId, @RequestParam Long workstationId, @RequestParam(required = false) String targetLevel) {
        return ResponseEntity.status(HttpStatus.CREATED).body(trainingService.createFormation(operatorId, workstationId, targetLevel));
    }
    @GetMapping("/formations")
    public ResponseEntity<List<FormationDetailsDto>> listAll() { return ResponseEntity.ok(trainingService.listAllFormations()); }
    @GetMapping("/formations/{id}/tracking")
    public ResponseEntity<List<DailyFormationTracking>> getTracking(@PathVariable Long id) { return ResponseEntity.ok(trainingService.getFormationTracking(id)); }
    @PostMapping("/formations/{id}/tracking")
    public ResponseEntity<DailyFormationTracking> addTracking(@PathVariable Long id, @RequestBody DailyTrackingDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(trainingService.addDailyTracking(id, dto));
    }
    @PostMapping("/formations/{id}/tracking/cadence")
    public ResponseEntity<DailyFormationTracking> addCadence(@PathVariable Long id, @RequestBody DailyTrackingDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(trainingService.addCadence(id, dto));
    }
    @PostMapping("/formations/{id}/tracking/defauts")
    public ResponseEntity<DailyFormationTracking> addDefauts(@PathVariable Long id, @RequestBody DailyTrackingDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(trainingService.addDefauts(id, dto));
    }
    @PutMapping("/formations/{id}/complete")
    public ResponseEntity<Void> complete(@PathVariable Long id) { trainingService.completeFormation(id); return ResponseEntity.ok().build(); }
    @PostMapping("/assignments")
    public ResponseEntity<FormationAssignment> assignOperator(@RequestParam Long operatorId, @RequestParam Long workstationId, @RequestParam(required = false) Boolean isPrimary) {
        return ResponseEntity.status(HttpStatus.CREATED).body(trainingService.assignOperator(operatorId, workstationId, isPrimary));
    }
    @GetMapping("/statistics")
    public ResponseEntity<FormationStatisticsDto> getStatistics() { return ResponseEntity.ok(trainingService.getStatistics()); }
}