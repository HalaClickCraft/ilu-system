package com.ilu.system.absence.service;

import com.ilu.system.absence.entity.Absence;
import com.ilu.system.absence.repository.AbsenceRepository;
import com.ilu.system.operator.entity.Operator;
import com.ilu.system.operator.repository.OperatorRepository;
import com.ilu.system.recyclage.entity.RecyclagePlanning;
import com.ilu.system.recyclage.repository.RecyclagePlanningRepository;
import com.ilu.system.recyclage.service.RecyclageService;
import com.ilu.system.notification.service.NotificationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.ilu.system.absence.entity.Absence.AbsenceStatus;
import static com.ilu.system.recyclage.entity.RecyclagePlanning.PlanningStatus;

@Service
public class AbsenceService {

    private final AbsenceRepository absenceRepository;
    private final OperatorRepository operatorRepository;
    private final RecyclagePlanningRepository recyclagePlanningRepository;
    private final RecyclageService recyclageService;
    private final NotificationService notificationService;
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public AbsenceService(AbsenceRepository absenceRepository,
                          OperatorRepository operatorRepository,
                          RecyclagePlanningRepository recyclagePlanningRepository,
                          RecyclageService recyclageService,
                          NotificationService notificationService) {
        this.absenceRepository = absenceRepository;
        this.operatorRepository = operatorRepository;
        this.recyclagePlanningRepository = recyclagePlanningRepository;
        this.recyclageService = recyclageService;
        this.notificationService = notificationService;
    }

    @Transactional
    public Map<String, Object> markAbsent(Long operatorId, LocalDate startDate, LocalDate expectedReturnDate) {
        Operator operator = operatorRepository.findById(operatorId)
                .orElseThrow(() -> new RuntimeException("Operator not found with id: " + operatorId));

        boolean alreadyAbsent = absenceRepository.existsByOperator_IdAndStatus(operatorId, AbsenceStatus.EN_COURS);
        if (alreadyAbsent) {
            Map<String, Object> error = new LinkedHashMap<>();
            error.put("error", "Operator is already marked as absent");
            error.put("operatorId", operatorId);
            return error;
        }

        Absence absence = new Absence();
        absence.setOperator(operator);
        absence.setStartDate(startDate);
        absence.setExpectedReturnDate(expectedReturnDate);
        absence.setStatus(AbsenceStatus.EN_COURS);
        absenceRepository.save(absence);

        // Deactivate operator
        operator.setActive(false);
        operatorRepository.save(operator);
        notificationService.createAbsenceNotification(operatorId,
                operator.getLastName() + " " + operator.getFirstName(), NotificationService.absenceStartType());

        // Cancel all PLANIFIEE plannings
        List<RecyclagePlanning> planifieePlannings = recyclagePlanningRepository.findByOperator_IdAndStatus(operatorId, PlanningStatus.PLANIFIEE);
        for (RecyclagePlanning planning : planifieePlannings) {
            planning.setStatus(PlanningStatus.ANNULEE);
            recyclagePlanningRepository.save(planning);
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("message", "Operator marked as absent");
        result.put("operatorId", operatorId);
        result.put("startDate", startDate.format(dateFormatter));
        result.put("expectedReturnDate", expectedReturnDate != null ? expectedReturnDate.format(dateFormatter) : null);
        result.put("cancelledPlannings", planifieePlannings.size());
        return result;
    }

    @Transactional
    public Map<String, Object> markReturn(Long operatorId, LocalDate returnDate) {
        Absence absence = absenceRepository.findByOperator_IdAndStatus(operatorId, AbsenceStatus.EN_COURS)
                .orElseThrow(() -> new RuntimeException("No active absence found for operator id: " + operatorId));

        absence.setActualReturnDate(returnDate);
        absence.setStatus(AbsenceStatus.TERMINEE);
        absenceRepository.save(absence);

        // Reactivate operator
        Operator operator = operatorRepository.findById(operatorId)
                .orElseThrow(() -> new RuntimeException("Operator not found with id: " + operatorId));
        operator.setActive(true);
        operatorRepository.save(operator);

        // Generate return-from-absence recyclage plannings
        Map<String, Object> recyclageResult = recyclageService.generateReturnFromAbsence(operatorId, returnDate);
        notificationService.createAbsenceNotification(operatorId,
                operator.getLastName() + " " + operator.getFirstName(), NotificationService.absenceReturnType());

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("message", "Operator returned from absence");
        result.put("operatorId", operatorId);
        result.put("returnDate", returnDate.format(dateFormatter));
        result.put("recyclagePlanningsCreated", recyclageResult.get("created"));
        return result;
    }

    @Transactional
    public Map<String, Object> markDeparture(Long operatorId, LocalDate exitDate) {
        Operator operator = operatorRepository.findById(operatorId)
                .orElseThrow(() -> new RuntimeException("Operator not found with id: " + operatorId));

        operator.setActive(false);
        operator.setExitDate(exitDate);
        operatorRepository.save(operator);
        notificationService.createDepartureNotification(operatorId,
                operator.getLastName() + " " + operator.getFirstName());

        // Cancel ALL PLANIFIEE plannings
        List<RecyclagePlanning> planifieePlannings = recyclagePlanningRepository.findByOperator_IdAndStatus(operatorId, PlanningStatus.PLANIFIEE);
        for (RecyclagePlanning planning : planifieePlannings) {
            planning.setStatus(PlanningStatus.ANNULEE);
            recyclagePlanningRepository.save(planning);
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("message", "Operator departure recorded");
        result.put("operatorId", operatorId);
        result.put("exitDate", exitDate.format(dateFormatter));
        result.put("cancelledPlannings", planifieePlannings.size());
        return result;
    }

    public List<Map<String, Object>> getActiveAbsences() {
        List<Absence> absences = absenceRepository.findByStatus(AbsenceStatus.EN_COURS);
        List<Map<String, Object>> result = new ArrayList<>();
        for (Absence absence : absences) {
            final Long absenceId = absence.getId();
            final Long opId = absence.getOperator().getId();
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", absenceId);
            map.put("operatorId", opId);
            map.put("operatorName", absence.getOperator().getLastName() + " " + absence.getOperator().getFirstName());
            map.put("employeeId", absence.getOperator().getEmployeeId());
            map.put("startDate", absence.getStartDate().format(dateFormatter));
            map.put("expectedReturnDate", absence.getExpectedReturnDate() != null ? absence.getExpectedReturnDate().format(dateFormatter) : null);
            map.put("status", absence.getStatus().name());
            map.put("createdAt", absence.getCreatedAt() != null ? absence.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) : null);
            result.add(map);
        }
        return result;
    }

    public List<Map<String, Object>> getAllAbsences() {
        List<Absence> absences = absenceRepository.findAll();
        List<Map<String, Object>> result = new ArrayList<>();
        for (Absence absence : absences) {
            final Long absenceId = absence.getId();
            final Long opId = absence.getOperator().getId();
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", absenceId);
            map.put("operatorId", opId);
            map.put("operatorName", absence.getOperator().getLastName() + " " + absence.getOperator().getFirstName());
            map.put("employeeId", absence.getOperator().getEmployeeId());
            map.put("startDate", absence.getStartDate().format(dateFormatter));
            map.put("expectedReturnDate", absence.getExpectedReturnDate() != null ? absence.getExpectedReturnDate().format(dateFormatter) : null);
            map.put("actualReturnDate", absence.getActualReturnDate() != null ? absence.getActualReturnDate().format(dateFormatter) : null);
            map.put("status", absence.getStatus().name());
            map.put("createdAt", absence.getCreatedAt() != null ? absence.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) : null);
            result.add(map);
        }
        return result;
    }

    public boolean isOperatorAbsent(Long operatorId) {
        return absenceRepository.existsByOperator_IdAndStatus(operatorId, AbsenceStatus.EN_COURS);
    }
}
