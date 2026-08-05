package com.ilu.system.operator.controller;

import com.ilu.system.operator.dto.*;
import com.ilu.system.operator.service.OnboardingService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/onboarding")
@CrossOrigin(origins = "*")
public class OnboardingController {

    private final OnboardingService onboardingService;

    public OnboardingController(OnboardingService onboardingService) {
        this.onboardingService = onboardingService;
    }

    private Set<String> getUserRoles(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return Collections.emptySet();
        }
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .map(auth -> auth.startsWith("ROLE_") ? auth.substring(5) : auth)
                .collect(Collectors.toSet());
    }

    @PostMapping("/seed")
    public String seedModules() {
        onboardingService.seedModules();
        return "Modules seeded successfully";
    }

    @GetMapping("/modules")
    public List<DepartmentOnboardingStatusDto> getAllModules(Authentication authentication) {
        return onboardingService.getAllModulesGrouped(getUserRoles(authentication));
    }

    @GetMapping("/operators/{operatorId}/status")
    public List<DepartmentOnboardingStatusDto> getOperatorStatus(
            @PathVariable Long operatorId,
            Authentication authentication) {
        return onboardingService.getOperatorOnboardingStatus(operatorId, getUserRoles(authentication));
    }

    @PutMapping("/operators/{operatorId}/modules/{moduleId}")
    public OnboardingModuleDto validateModule(
            @PathVariable Long operatorId,
            @PathVariable Long moduleId,
            @RequestBody ValidateModuleRequest request,
            Authentication authentication) {
            return onboardingService.validateModule(
                    operatorId,
                    moduleId,
                    request,
                    getUserRoles(authentication),
                    authentication.getName());
    }

    @GetMapping("/operators/{operatorId}/progress")
    public Map<String, Object> getOperatorProgress(
            @PathVariable Long operatorId,
            Authentication authentication) {
        return onboardingService.getOperatorProgress(operatorId, getUserRoles(authentication));
    }

    @GetMapping("/operators/{operatorId}/complete")
    public Map<String, Object> checkOnboardingComplete(@PathVariable Long operatorId) {
        boolean complete = onboardingService.isOnboardingComplete(operatorId);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("operatorId", operatorId);
        result.put("onboardingComplete", complete);
        return result;
    }

    @PostMapping("/operators/batch-complete")
    public Map<Long, Boolean> batchCheckOnboardingComplete(@RequestBody List<Long> operatorIds) {
        return onboardingService.getOnboardingStatusForOperators(operatorIds);
    }

    @GetMapping("/operators-summary")
    public List<OperatorOnboardingSummaryDto> getAllOperatorsSummary(Authentication authentication) {
        return onboardingService.getAllOperatorsSummary(getUserRoles(authentication));
    }

    @GetMapping("/history")
    public Map<String, Object> getOnboardingHistory(Authentication authentication) {
        return onboardingService.getOnboardingHistory(getUserRoles(authentication));
    }
}