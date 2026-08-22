package com.ilu.system.recyclage.controller;

import com.ilu.system.recyclage.service.RecyclageService;
import com.ilu.system.security.AccessControlService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/recyclage")
@CrossOrigin(origins = "*")
public class RecyclageController {

    private final RecyclageService recyclageService;
    private final AccessControlService accessControlService;

    public RecyclageController(RecyclageService recyclageService, AccessControlService accessControlService) {
        this.recyclageService = recyclageService;
        this.accessControlService = accessControlService;
    }

    @GetMapping("/planning")
    public List<Map<String, Object>> getAllPlanning(
            @RequestParam(required = false) Long projectId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long operatorId,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String search) {
        return recyclageService.getAllPlanning(projectId, status, operatorId, type, search);
    }

    @PostMapping("/planning/generate-annual")
    public Map<String, Object> generateAnnualEvaluations(@RequestParam int year) {
        return recyclageService.generateAnnualEvaluations(year);
    }

    @PostMapping("/planning/new-hire/{operatorId}")
    public Map<String, Object> generateNewHirePlanning(@PathVariable Long operatorId) {
        return recyclageService.generateNewHirePlanning(operatorId);
    }

    @PostMapping("/planning/return-from-absence/{operatorId}")
    public Map<String, Object> generateReturnFromAbsence(@PathVariable Long operatorId,
                                                          @RequestParam(required = false) String returnDate) {
        LocalDate date = returnDate != null && !returnDate.isBlank() ? LocalDate.parse(returnDate) : LocalDate.now();
        return recyclageService.generateReturnFromAbsence(operatorId, date);
    }

    @PostMapping("/planning/manual")
    public Map<String, Object> createManualRecyclage(@RequestBody Map<String, Object> body, Authentication authentication) {
        Long operatorId = Long.valueOf(body.get("operatorId").toString());
        Long workstationId = Long.valueOf(body.get("workstationId").toString());
        accessControlService.requireRecyclageManagement(authentication, recyclageService.getWorkstationProjectId(workstationId));
        LocalDate scheduledDate = body.get("scheduledDate") != null && !body.get("scheduledDate").toString().isBlank()
                ? LocalDate.parse(body.get("scheduledDate").toString()) : LocalDate.now();
        return recyclageService.createManualRecyclage(operatorId, workstationId, scheduledDate);
    }

    @PostMapping("/planning/{id}/start-evaluation")
    public Map<String, Object> startEvaluation(@PathVariable Long id, Authentication authentication) {
        accessControlService.requireRecyclageManagement(authentication, recyclageService.getPlanningProjectId(id));
        return recyclageService.startEvaluation(id);
    }

    @PutMapping("/planning/{id}/complete")
    public Map<String, Object> completePlanning(
            @PathVariable Long id,
            @RequestBody Map<String, Object> body, Authentication authentication) {
        accessControlService.requireRecyclageManagement(authentication, recyclageService.getPlanningProjectId(id));
        String niveauObtenu = (String) body.get("niveauObtenu");
        Long evaluationSessionId = body.get("evaluationSessionId") != null
                ? Long.valueOf(body.get("evaluationSessionId").toString()) : null;
        return recyclageService.completePlanning(id, niveauObtenu, evaluationSessionId);
    }

    @PutMapping("/planning/{id}/cancel")
    public Map<String, Object> cancelPlanning(@PathVariable Long id, Authentication authentication) {
        accessControlService.requireRecyclageManagement(authentication, recyclageService.getPlanningProjectId(id));
        return recyclageService.cancelPlanning(id);
    }

    @GetMapping("/calendar")
    public List<Map<String, Object>> getPlanningByMonth(
            @RequestParam int month,
            @RequestParam int year,
            @RequestParam(required = false) Long projectId) {
        return recyclageService.getPlanningByMonth(month, year, projectId);
    }

    @GetMapping("/upcoming")
    public List<Map<String, Object>> getUpcomingRecyclages(
            @RequestParam(defaultValue = "30") int daysAhead,
            @RequestParam(required = false) Long projectId) {
        return recyclageService.getUpcomingRecyclages(daysAhead, projectId);
    }
}
