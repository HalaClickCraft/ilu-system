package com.ilu.system.absence.controller;

import com.ilu.system.absence.service.AbsenceService;
import com.ilu.system.security.AccessControlService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/absence")
@CrossOrigin(origins = "*")
public class AbsenceController {

    private final AbsenceService absenceService;
    private final AccessControlService accessControlService;

    public AbsenceController(AbsenceService absenceService, AccessControlService accessControlService) {
        this.absenceService = absenceService;
        this.accessControlService = accessControlService;
    }

    @GetMapping
    public List<Map<String, Object>> getAllAbsences() {
        return absenceService.getAllAbsences();
    }

    @GetMapping("/active")
    public List<Map<String, Object>> getActiveAbsences() {
        return absenceService.getActiveAbsences();
    }

    @PostMapping("/mark-absent")
    public Map<String, Object> markAbsent(@RequestBody Map<String, Object> body, Authentication authentication) {
        Long operatorId = Long.valueOf(body.get("operatorId").toString());
        accessControlService.requireAbsenceManagement(authentication, operatorId);
        LocalDate startDate = LocalDate.parse(body.get("startDate").toString());
        LocalDate expectedReturnDate = body.get("expectedReturnDate") != null
                ? LocalDate.parse(body.get("expectedReturnDate").toString()) : null;
        return absenceService.markAbsent(operatorId, startDate, expectedReturnDate);
    }

    @PostMapping("/mark-return")
    public Map<String, Object> markReturn(@RequestBody Map<String, Object> body, Authentication authentication) {
        Long operatorId = Long.valueOf(body.get("operatorId").toString());
        accessControlService.requireAbsenceManagement(authentication, operatorId);
        LocalDate returnDate = LocalDate.parse(body.get("returnDate").toString());
        return absenceService.markReturn(operatorId, returnDate);
    }

    @PostMapping("/mark-departure")
    public Map<String, Object> markDeparture(@RequestBody Map<String, Object> body, Authentication authentication) {
        Long operatorId = Long.valueOf(body.get("operatorId").toString());
        accessControlService.requireAbsenceManagement(authentication, operatorId);
        LocalDate exitDate = LocalDate.parse(body.get("exitDate").toString());
        return absenceService.markDeparture(operatorId, exitDate);
    }

    @GetMapping("/check/{operatorId}")
    public Map<String, Object> isOperatorAbsent(@PathVariable Long operatorId) {
        boolean absent = absenceService.isOperatorAbsent(operatorId);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("operatorId", operatorId);
        result.put("isAbsent", absent);
        return result;
    }
}
