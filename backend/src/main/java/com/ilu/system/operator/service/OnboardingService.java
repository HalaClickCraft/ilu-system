package com.ilu.system.operator.service;

import com.ilu.system.operator.dto.*;
import com.ilu.system.operator.entity.OnboardingModule;
import com.ilu.system.operator.entity.OperatorOnboarding;
import com.ilu.system.operator.entity.Operator;
import com.ilu.system.operator.repository.OnboardingModuleRepository;
import com.ilu.system.operator.repository.OperatorOnboardingRepository;
import com.ilu.system.operator.repository.OperatorRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class OnboardingService {

    private final OnboardingModuleRepository moduleRepo;
    private final OperatorOnboardingRepository operatorOnboardingRepo;
    private final OperatorRepository operatorRepository;

    // Role → Department mapping
    // CHEF_EQUIPE = Production, RH = RH, RESP_HSE = HSE, RESP_QUALITE = Qualite
    // DEPT_PROCESS, DEPT_MAINTENANCE, DEPT_DGT_MANUFACTURING = new roles
    public static final Map<String, String> DEPT_ROLE_MAP = new LinkedHashMap<>();
    static {
        DEPT_ROLE_MAP.put("CHEF_EQUIPE", "Production");
        DEPT_ROLE_MAP.put("RH", "RH");
        DEPT_ROLE_MAP.put("RESP_HSE", "HSE");
        DEPT_ROLE_MAP.put("RESP_QUALITE", "Qualité");
        DEPT_ROLE_MAP.put("AGENT_QUALITE", "Qualité");
        DEPT_ROLE_MAP.put("DEPT_PROCESS", "Process");
        DEPT_ROLE_MAP.put("DEPT_MAINTENANCE", "Maintenance");
        DEPT_ROLE_MAP.put("DEPT_DGT_MANUFACTURING", "DGT Manufacturing");
    }

    public static final Set<String> FULL_ACCESS_ROLES = new HashSet<>(Arrays.asList("ADMIN"));
    public static final Set<String> READ_ONLY_ALL_ROLES = new HashSet<>(Arrays.asList("SUPERVISEUR"));

    public OnboardingService(OnboardingModuleRepository moduleRepo,
                             OperatorOnboardingRepository operatorOnboardingRepo,
                             OperatorRepository operatorRepository) {
        this.moduleRepo = moduleRepo;
        this.operatorOnboardingRepo = operatorOnboardingRepo;
        this.operatorRepository = operatorRepository;
    }

    public Set<String> getAccessibleDepartments(Set<String> userRoles) {
        for (String role : userRoles) {
            if (FULL_ACCESS_ROLES.contains(role) || READ_ONLY_ALL_ROLES.contains(role)) {
                return new HashSet<>(DEPT_ROLE_MAP.values());
            }
        }
        Set<String> departments = new LinkedHashSet<>();
        for (String role : userRoles) {
            String dept = DEPT_ROLE_MAP.get(role);
            if (dept != null) departments.add(dept);
        }
        return departments;
    }

    public boolean canValidateDepartment(Set<String> userRoles, String department) {
        for (String role : userRoles) {
            if (FULL_ACCESS_ROLES.contains(role)) return true;
            if (READ_ONLY_ALL_ROLES.contains(role)) continue;
            String mappedDept = DEPT_ROLE_MAP.get(role);
            if (mappedDept != null && mappedDept.equals(department)) return true;
        }
        return false;
    }

    // ==================== SEED MODULES ====================

    @Transactional
    public void seedModules() {
        if (moduleRepo.count() > 0) return;

        List<OnboardingModule> modules = new ArrayList<>();

        modules.add(new OnboardingModule("Présentation RH", "RH", 1));
        modules.add(new OnboardingModule("Code de conduite", "RH", 2));
        modules.add(new OnboardingModule("Réglement interne", "RH", 3));
        modules.add(new OnboardingModule("Cybersecurity", "RH", 4));

        modules.add(new OnboardingModule("EHS Induction", "HSE", 1));
        modules.add(new OnboardingModule("6NN", "HSE", 2));
        modules.add(new OnboardingModule("Fire awareness", "HSE", 3));
        modules.add(new OnboardingModule("LOTO", "HSE", 4));
        modules.add(new OnboardingModule("Stop 5", "HSE", 5));

        modules.add(new OnboardingModule("Standard Work Instructions", "Process", 1));
        modules.add(new OnboardingModule("Finition", "Process", 2));
        modules.add(new OnboardingModule("Assemblage", "Process", 3));
        modules.add(new OnboardingModule("HLT", "Process", 4));
        modules.add(new OnboardingModule("POKE YOKE", "Process", 5));
        modules.add(new OnboardingModule("Soudeur Robot", "Process", 6));
        modules.add(new OnboardingModule("DASIP", "Process", 7));

        modules.add(new OnboardingModule("Mode escalade", "Qualité", 1));
        modules.add(new OnboardingModule("Traitement non conformités", "Qualité", 2));
        modules.add(new OnboardingModule("Fiches SATO", "Qualité", 3));
        modules.add(new OnboardingModule("Modules qualité", "Qualité", 4));
        modules.add(new OnboardingModule("SR", "Qualité", 5));

        modules.add(new OnboardingModule("TPM", "Maintenance", 1));
        modules.add(new OnboardingModule("5S", "Production", 1));
        modules.add(new OnboardingModule("PES", "DGT Manufacturing", 1));

        moduleRepo.saveAll(modules);
    }

    // ==================== GET ALL MODULES GROUPED ====================

    public List<DepartmentOnboardingStatusDto> getAllModulesGrouped(Set<String> userRoles) {
        Set<String> accessibleDepts = getAccessibleDepartments(userRoles);
        List<OnboardingModule> allModules = moduleRepo.findAllByOrderByDepartmentAscDisplayOrderAsc();

        List<OnboardingModule> filtered = allModules.stream()
                .filter(m -> accessibleDepts.contains(m.getDepartment()))
                .collect(Collectors.toList());

        Map<String, List<OnboardingModule>> grouped = filtered.stream()
                .collect(Collectors.groupingBy(OnboardingModule::getDepartment,
                        LinkedHashMap::new, Collectors.toList()));

        List<DepartmentOnboardingStatusDto> result = new ArrayList<>();
        for (Map.Entry<String, List<OnboardingModule>> entry : grouped.entrySet()) {
            DepartmentOnboardingStatusDto dept = new DepartmentOnboardingStatusDto();
            dept.setDepartment(entry.getKey());
            dept.setTotalModules(entry.getValue().size());
            dept.setModules(entry.getValue().stream().map(this::toModuleDto).collect(Collectors.toList()));
            dept.setEditable(canValidateDepartment(userRoles, entry.getKey()));
            result.add(dept);
        }
        return result;
    }

    // ==================== OPERATOR ONBOARDING STATUS ====================

    public List<DepartmentOnboardingStatusDto> getOperatorOnboardingStatus(Long operatorId, Set<String> userRoles) {
        Set<String> accessibleDepts = getAccessibleDepartments(userRoles);
        List<OnboardingModule> allModules = moduleRepo.findAllByOrderByDepartmentAscDisplayOrderAsc();
        List<OperatorOnboarding> operatorRecords = operatorOnboardingRepo.findByOperatorId(operatorId);

        allModules = allModules.stream()
                .filter(m -> accessibleDepts.contains(m.getDepartment()))
                .collect(Collectors.toList());

        Map<Long, OperatorOnboarding> recordMap = operatorRecords.stream()
                .collect(Collectors.toMap(OperatorOnboarding::getModuleId, r -> r, (a, b) -> a));

        Map<String, List<OnboardingModule>> grouped = allModules.stream()
                .collect(Collectors.groupingBy(OnboardingModule::getDepartment,
                        LinkedHashMap::new, Collectors.toList()));

        List<DepartmentOnboardingStatusDto> result = new ArrayList<>();
        for (Map.Entry<String, List<OnboardingModule>> entry : grouped.entrySet()) {
            DepartmentOnboardingStatusDto dept = new DepartmentOnboardingStatusDto();
            dept.setDepartment(entry.getKey());
            dept.setEditable(canValidateDepartment(userRoles, entry.getKey()));

            List<Long> moduleIds = entry.getValue().stream().map(OnboardingModule::getId).collect(Collectors.toList());
            long completedCount = operatorOnboardingRepo.countCompletedByOperatorIdAndModuleIds(operatorId, moduleIds);

            dept.setTotalModules(entry.getValue().size());
            dept.setCompletedModules(completedCount);
            dept.setCompletionPercentage(entry.getValue().size() > 0
                    ? Math.round((double) completedCount / entry.getValue().size() * 100.0 * 10.0) / 10.0
                    : 0.0);

            List<OnboardingModuleDto> moduleDtos = new ArrayList<>();
            for (OnboardingModule m : entry.getValue()) {
                OnboardingModuleDto dto = toModuleDto(m);
                OperatorOnboarding record = recordMap.get(m.getId());
                if (record != null) {
                    dto.setCompleted(record.getCompleted());
                    dto.setCompletedDate(record.getCompletedDate() != null ? record.getCompletedDate().toString() : null);
                    dto.setValidatedBy(record.getValidatedBy());
                    dto.setComment(record.getComment());
                }
                moduleDtos.add(dto);
            }
            dept.setModules(moduleDtos);
            result.add(dept);
        }
        return result;
    }

    // ==================== VALIDATE MODULE ====================

    @Transactional
    public OnboardingModuleDto validateModule(
            Long operatorId,
            Long moduleId,
            ValidateModuleRequest request,
            Set<String> userRoles,
            String validatedBy) {
        OnboardingModule module = moduleRepo.findById(moduleId)
                .orElseThrow(() -> new RuntimeException("Module non trouvé: " + moduleId));

        if (Boolean.TRUE.equals(request.getCompleted())) {
            if (!canValidateDepartment(userRoles, module.getDepartment())) {
                throw new RuntimeException("Accès refusé: vous ne pouvez pas valider les modules du département " + module.getDepartment());
            }
        }

        OperatorOnboarding record = operatorOnboardingRepo
                .findByOperatorIdAndModuleId(operatorId, moduleId)
                .orElse(new OperatorOnboarding(operatorId, moduleId));

        if (Boolean.TRUE.equals(request.getCompleted())) {
            record.setCompleted(true);
            record.setCompletedDate(LocalDate.now());
            record.setValidatedBy(validatedBy);
        } else {
            record.setCompleted(false);
            record.setCompletedDate(null);
            record.setValidatedBy(null);
        }
        record.setComment(request.getComment());

        OperatorOnboarding saved = operatorOnboardingRepo.save(record);

        OnboardingModuleDto dto = toModuleDto(module);
        dto.setCompleted(saved.getCompleted());
        dto.setCompletedDate(saved.getCompletedDate() != null ? saved.getCompletedDate().toString() : null);
        dto.setValidatedBy(saved.getValidatedBy());
        dto.setComment(saved.getComment());
        return dto;
    }

    // ==================== OPERATOR PROGRESS ====================

    public Map<String, Object> getOperatorProgress(Long operatorId, Set<String> userRoles) {
        Set<String> accessibleDepts = getAccessibleDepartments(userRoles);
        List<OnboardingModule> allModules = moduleRepo.findAllByOrderByDepartmentAscDisplayOrderAsc();
        List<OnboardingModule> filtered = allModules.stream()
                .filter(m -> accessibleDepts.contains(m.getDepartment()))
                .collect(Collectors.toList());

        long totalModules = filtered.size();
        List<Long> allModuleIds = filtered.stream().map(OnboardingModule::getId).collect(Collectors.toList());
        long completedModules = allModuleIds.isEmpty() ? 0 : operatorOnboardingRepo.countCompletedByOperatorIdAndModuleIds(operatorId, allModuleIds);

        double percentage = totalModules > 0
                ? Math.round((double) completedModules / totalModules * 100.0 * 10.0) / 10.0
                : 0.0;

        Map<String, Object> progress = new LinkedHashMap<>();
        progress.put("operatorId", operatorId);
        progress.put("totalModules", totalModules);
        progress.put("completedModules", completedModules);
        progress.put("remainingModules", totalModules - completedModules);
        progress.put("completionPercentage", percentage);
        progress.put("departmentComplete", completedModules == totalModules && totalModules > 0);
        progress.put("onboardingComplete", isOnboardingComplete(operatorId));

        List<String> departments = new ArrayList<>(accessibleDepts);
        List<Map<String, Object>> deptBreakdown = new ArrayList<>();
        for (String dept : departments) {
            List<OnboardingModule> deptModules = moduleRepo.findByDepartmentOrderByDisplayOrderAsc(dept);
            List<Long> moduleIds = deptModules.stream().map(OnboardingModule::getId).collect(Collectors.toList());
            long deptCompleted = moduleIds.isEmpty() ? 0 : operatorOnboardingRepo.countCompletedByOperatorIdAndModuleIds(operatorId, moduleIds);

            Map<String, Object> deptMap = new LinkedHashMap<>();
            deptMap.put("department", dept);
            deptMap.put("total", deptModules.size());
            deptMap.put("completed", deptCompleted);
            deptMap.put("percentage", deptModules.size() > 0
                    ? Math.round((double) deptCompleted / deptModules.size() * 100.0 * 10.0) / 10.0
                    : 0.0);
            deptMap.put("departmentComplete", deptCompleted == deptModules.size());
            deptMap.put("editable", canValidateDepartment(userRoles, dept));
            deptBreakdown.add(deptMap);
        }
        progress.put("departments", deptBreakdown);

        return progress;
    }

    // ==================== ALL OPERATORS SUMMARY (matrix table) ====================

    public List<OperatorOnboardingSummaryDto> getAllOperatorsSummary(Set<String> userRoles) {
        Set<String> accessibleDepts = getAccessibleDepartments(userRoles);
        List<OnboardingModule> allModules = moduleRepo.findAllByOrderByDepartmentAscDisplayOrderAsc();
        List<Operator> allOperators = operatorRepository.findAll();

        // Group module IDs by department (only accessible ones)
        Map<String, List<Long>> deptModuleIds = new LinkedHashMap<>();
        for (OnboardingModule m : allModules) {
            if (!accessibleDepts.contains(m.getDepartment())) continue;
            deptModuleIds.computeIfAbsent(m.getDepartment(), k -> new ArrayList<>()).add(m.getId());
        }

        // Collect all accessible module IDs
        Set<Long> accessibleModuleIds = new HashSet<>();
        for (List<Long> ids : deptModuleIds.values()) {
            accessibleModuleIds.addAll(ids);
        }
        long totalModuleCount = accessibleModuleIds.size();

        // Get ALL onboarding records
        List<OperatorOnboarding> allRecords = operatorOnboardingRepo.findAll();
        Map<Long, Set<Long>> completedByOperator = new HashMap<>();
        for (OperatorOnboarding record : allRecords) {
            if (Boolean.TRUE.equals(record.getCompleted())) {
                completedByOperator.computeIfAbsent(record.getOperatorId(), k -> new HashSet<>())
                        .add(record.getModuleId());
            }
        }

        List<OperatorOnboardingSummaryDto> result = new ArrayList<>();
        for (Operator op : allOperators) {
            OperatorOnboardingSummaryDto dto = new OperatorOnboardingSummaryDto();
            dto.setOperatorId(op.getId());
            dto.setFirstName(op.getFirstName());
            dto.setLastName(op.getLastName());
            dto.setMatricule(op.getEmployeeId());
            dto.setTotalModules(totalModuleCount);

            Set<Long> opCompleted = completedByOperator.getOrDefault(op.getId(), Collections.emptySet());
            long completed = opCompleted.stream().filter(accessibleModuleIds::contains).count();

            dto.setCompletedModules(completed);
            dto.setCompletionPercentage(totalModuleCount > 0
                    ? Math.round((double) completed / totalModuleCount * 100.0 * 10.0) / 10.0
                    : 0.0);
            dto.setOnboardingComplete(completed == totalModuleCount && totalModuleCount > 0);

            Map<String, OperatorOnboardingSummaryDto.DepartmentProgress> deptProgress = new LinkedHashMap<>();
            for (Map.Entry<String, List<Long>> entry : deptModuleIds.entrySet()) {
                long deptTotal = entry.getValue().size();
                long deptCompleted = entry.getValue().stream().filter(opCompleted::contains).count();
                deptProgress.put(entry.getKey(), new OperatorOnboardingSummaryDto.DepartmentProgress(deptTotal, deptCompleted));
            }
            dto.setDepartmentProgress(deptProgress);

            result.add(dto);
        }

        return result;
    }

    // ==================== HISTORY ====================

    public Map<String, Object> getOnboardingHistory(Set<String> userRoles) {
        List<OperatorOnboardingSummaryDto> all = getAllOperatorsSummary(userRoles);

        List<OperatorOnboardingSummaryDto> completed = all.stream()
                .filter(OperatorOnboardingSummaryDto::isOnboardingComplete)
                .collect(Collectors.toList());

        List<OperatorOnboardingSummaryDto> pending = all.stream()
                .filter(d -> !d.isOnboardingComplete())
                .collect(Collectors.toList());

        Map<String, Object> history = new LinkedHashMap<>();
        history.put("totalOperators", all.size());
        history.put("completedCount", completed.size());
        history.put("pendingCount", pending.size());
        history.put("completedOperators", completed);
        history.put("pendingOperators", pending);
        return history;
    }

    // ==================== CHECK COMPLETION (for formation system) ====================

    public boolean isOnboardingComplete(Long operatorId) {
        Set<Long> moduleIds = moduleRepo.findAll().stream()
                .map(OnboardingModule::getId)
                .collect(Collectors.toSet());
        Set<Long> completedModuleIds = operatorOnboardingRepo.findByOperatorId(operatorId).stream()
                .filter(record -> Boolean.TRUE.equals(record.getCompleted()))
                .map(OperatorOnboarding::getModuleId)
                .collect(Collectors.toSet());
        return !moduleIds.isEmpty() && completedModuleIds.containsAll(moduleIds);
    }

    public Map<Long, Boolean> getOnboardingStatusForOperators(List<Long> operatorIds) {
        Map<Long, Boolean> result = new LinkedHashMap<>();
        long totalModules = moduleRepo.count();
        for (Long opId : operatorIds) {
            if (totalModules == 0) {
                result.put(opId, false);
            } else {
                long completed = operatorOnboardingRepo.countCompletedByOperatorId(opId);
                result.put(opId, completed >= totalModules);
            }
        }
        return result;
    }

    // ==================== HELPER ====================

    private OnboardingModuleDto toModuleDto(OnboardingModule module) {
        OnboardingModuleDto dto = new OnboardingModuleDto();
        dto.setId(module.getId());
        dto.setName(module.getName());
        dto.setDepartment(module.getDepartment());
        dto.setDisplayOrder(module.getDisplayOrder());
        dto.setDescription(module.getDescription());
        dto.setCompleted(false);
        return dto;
    }
}