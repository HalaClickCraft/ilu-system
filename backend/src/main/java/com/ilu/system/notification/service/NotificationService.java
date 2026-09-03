package com.ilu.system.notification.service;

import com.ilu.system.auth.entity.User;
import com.ilu.system.auth.repository.UserRepository;
import com.ilu.system.notification.entity.Notification;
import com.ilu.system.notification.entity.NotificationSent;
import com.ilu.system.notification.repository.NotificationRepository;
import com.ilu.system.notification.repository.NotificationSentRepository;
import com.ilu.system.recyclage.entity.RecyclagePlanning;
import com.ilu.system.recyclage.repository.RecyclagePlanningRepository;
import com.ilu.system.operator.repository.TeamRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ilu.system.operator.entity.Operator;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.ilu.system.notification.entity.Notification.NotificationType;
import static com.ilu.system.notification.entity.Notification.RecipientType;
import static com.ilu.system.recyclage.entity.RecyclagePlanning.PlanningStatus;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationSentRepository notificationSentRepository;
    private final RecyclagePlanningRepository recyclagePlanningRepository;
    private final UserRepository userRepository;
    private final TeamRepository teamRepository;
    private final EmailService emailService;
    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    public NotificationService(NotificationRepository notificationRepository,
                               NotificationSentRepository notificationSentRepository,
                               RecyclagePlanningRepository recyclagePlanningRepository,
                               UserRepository userRepository,
                               TeamRepository teamRepository,
                               EmailService emailService) {
        this.notificationRepository = notificationRepository;
        this.notificationSentRepository = notificationSentRepository;
        this.recyclagePlanningRepository = recyclagePlanningRepository;
        this.userRepository = userRepository;
        this.teamRepository = teamRepository;
        this.emailService = emailService;
    }

    @Scheduled(cron = "0 0 8 * * *")
    @Transactional
    public Map<String, Object> checkAndSendNotifications() {
        List<RecyclagePlanning> planifieePlannings = recyclagePlanningRepository.findByStatus(PlanningStatus.PLANIFIEE);
        LocalDate today = LocalDate.now();
        int createdCount = 0;
        final int targetDaysBefore = 10;

        // Filter plannings due in exactly 10 days for Annual Recyclage or Nouvelle Recrue S2
        List<RecyclagePlanning> duePlannings = new ArrayList<>();
        for (RecyclagePlanning planning : planifieePlannings) {
            if (planning.getScheduledDate() == null || planning.getOperator() == null || planning.getWorkstation() == null) {
                continue;
            }

            boolean isTargetRecyclage = planning.getType() == RecyclagePlanning.PlanningType.EVALUATION_ANNUELLE_MOIS_1
                    || planning.getType() == RecyclagePlanning.PlanningType.EVALUATION_ANNUELLE_MOIS_7
                    || planning.getType() == RecyclagePlanning.PlanningType.RECYCLAGE
                    || planning.getType() == RecyclagePlanning.PlanningType.RECYCLAGE_NOUVELLE_RECRUE;

            if (!isTargetRecyclage) {
                continue;
            }

            long daysRemaining = ChronoUnit.DAYS.between(today, planning.getScheduledDate());
            if (daysRemaining == targetDaysBefore) {
                boolean alreadySent = notificationSentRepository.existsByPlanningIdAndDaysBefore(planning.getId(), targetDaysBefore);
                if (!alreadySent) {
                    duePlannings.add(planning);
                }
            }
        }

        // Segment due plannings into the 5 Official ILU Recyclage Cases:
        // Case 1 & 2: Recyclage Annuel (Opérateurs déjà en poste - S1 Janvier / S2 Juillet)
        List<RecyclagePlanning> annualPlannings = duePlannings.stream()
                .filter(p -> p.getSource() == RecyclagePlanning.PlanningSource.ANNUELLE 
                        || p.getType() == RecyclagePlanning.PlanningType.EVALUATION_ANNUELLE_MOIS_1 
                        || p.getType() == RecyclagePlanning.PlanningType.EVALUATION_ANNUELLE_MOIS_7)
                .toList();

        // Case 3: Recyclage Nouvelle Recrue (2ème Semestre - J+6 mois)
        List<RecyclagePlanning> recruitPlannings = duePlannings.stream()
                .filter(p -> p.getSource() == RecyclagePlanning.PlanningSource.NOUVELLE_RECRUE 
                        || p.getType() == RecyclagePlanning.PlanningType.RECYCLAGE_NOUVELLE_RECRUE)
                .toList();

        // Case 4: Recyclage Reprise après Absence (> 30 jours)
        List<RecyclagePlanning> returnAbsencePlannings = duePlannings.stream()
                .filter(p -> p.getSource() == RecyclagePlanning.PlanningSource.REPRISE_ABSENCE)
                .toList();

        // Case 5: Recyclage Ponctuel / Manuel (Demandé par Chef d'Équipe)
        List<RecyclagePlanning> manualPlannings = duePlannings.stream()
                .filter(p -> p.getSource() == RecyclagePlanning.PlanningSource.CHEF_EQUIPE)
                .toList();

        List<User> hrUsers = usersWithRole("RH");

        // ===== CAS 1 & 2: RECYCLAGE ANNUEL (Opérateurs déjà en poste) -> 1 SEUL E-MAIL GLOBAL DE CAMPAGNE =====
        if (!annualPlannings.isEmpty()) {
            String campaignDateStr = annualPlannings.get(0).getScheduledDate().toString();
            Map<Operator, List<RecyclagePlanning>> annualByOp = annualPlannings.stream()
                    .collect(Collectors.groupingBy(RecyclagePlanning::getOperator));

            String inAppMessage = "Recyclage Annuel dans 10 jours pour les opérateurs déjà en poste (Date prévue : " + campaignDateStr + ")";

            StringBuilder emailBuilder = new StringBuilder();
            emailBuilder.append("Bonjour,\n\n");
            emailBuilder.append("Rappel de campagne de Recyclage Annuel à J-10 :\n");
            emailBuilder.append("Recyclage Annuel dans 10 jours pour les opérateurs déjà en poste (Date prévue : ").append(campaignDateStr).append(")\n\n");
            emailBuilder.append("• Nombre d'opérateurs concernés : ").append(annualByOp.size()).append(" opérateur(s)\n");
            emailBuilder.append("• Nombre total d'évaluations postes : ").append(annualPlannings.size()).append(" évaluation(s)\n\n");
            emailBuilder.append("Détail des opérateurs et postes de travail :\n");

            for (Map.Entry<Operator, List<RecyclagePlanning>> entry : annualByOp.entrySet()) {
                Operator op = entry.getKey();
                String opName = (op.getLastName() != null ? op.getLastName() : "") + " " + (op.getFirstName() != null ? op.getFirstName() : "");
                String mat = op.getEmployeeId() != null ? op.getEmployeeId() : "-";
                String wsList = entry.getValue().stream().map(p -> p.getWorkstation().getName()).filter(w -> w != null && !w.isBlank()).distinct().collect(Collectors.joining(", "));
                emailBuilder.append("  • ").append(opName).append(" (Matricule : ").append(mat).append(") → ").append(wsList).append("\n");
            }

            emailBuilder.append("\nMerci de bien vouloir coordonner avec les chefs d'équipe et les agents qualité pour la planification des sessions.");

            String emailBody = emailBuilder.toString();
            String emailSubject = "[Système ILU] Campagne Recyclage Annuel (J-10) - Opérateurs déjà en poste";

            for (User hrUser : hrUsers) {
                if (hrUser.getEmail() != null && !hrUser.getEmail().isBlank()) {
                    emailService.sendEmail(hrUser.getEmail(), emailSubject, emailBody);
                } else {
                    emailService.sendEmail(null, emailSubject, emailBody);
                }

                Notification notif = new Notification();
                notif.setRecipientId(hrUser.getId());
                notif.setRecipientType(RecipientType.HR);
                notif.setType(NotificationType.RECYCLAGE_10J);
                notif.setMessage(inAppMessage);
                notif.setRead(false);
                notif.setEmailSent(true);
                notif.setRelatedPlanningId(annualPlannings.get(0).getId());
                notificationRepository.save(notif);
                createdCount++;
            }

            for (RecyclagePlanning p : annualPlannings) {
                NotificationSent sentRecord = new NotificationSent();
                sentRecord.setPlanningId(p.getId());
                sentRecord.setDaysBefore(targetDaysBefore);
                sentRecord.setNotificationType(NotificationType.RECYCLAGE_10J);
                notificationSentRepository.save(sentRecord);
            }
        }

        // ===== CAS 3: NOUVELLE RECRUE (2ème Semestre) -> 1 E-mail par recrue groupant tous ses postes =====
        if (!recruitPlannings.isEmpty()) {
            createdCount += sendGroupedOperatorNotifications(recruitPlannings, "Recyclage Nouvelle Recrue (2ème Semestre)", hrUsers, targetDaysBefore);
        }

        // ===== CAS 4: REPRISE APRÈS ABSENCE (> 30j) -> 1 E-mail par opérateur groupant tous ses postes =====
        if (!returnAbsencePlannings.isEmpty()) {
            createdCount += sendGroupedOperatorNotifications(returnAbsencePlannings, "Recyclage Reprise après Absence", hrUsers, targetDaysBefore);
        }

        // ===== CAS 5: RECYCLAGE PONCTUEL / CHEF D'ÉQUIPE -> 1 E-mail par opérateur groupant tous ses postes =====
        if (!manualPlannings.isEmpty()) {
            createdCount += sendGroupedOperatorNotifications(manualPlannings, "Recyclage Ponctuel (Chef d'Équipe)", hrUsers, targetDaysBefore);
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("notificationsCreated", createdCount);
        result.put("annualPlanningsCount", annualPlannings.size());
        result.put("recruitPlanningsCount", recruitPlannings.size());
        result.put("returnAbsencePlanningsCount", returnAbsencePlannings.size());
        result.put("manualPlanningsCount", manualPlannings.size());
        result.put("date", today.toString());
        return result;
    }

    private int sendGroupedOperatorNotifications(List<RecyclagePlanning> plannings, String categoryTitle, List<User> hrUsers, int targetDaysBefore) {
        int count = 0;
        Map<Operator, List<RecyclagePlanning>> byOp = plannings.stream()
                .collect(Collectors.groupingBy(RecyclagePlanning::getOperator));

        for (Map.Entry<Operator, List<RecyclagePlanning>> entry : byOp.entrySet()) {
            Operator op = entry.getKey();
            List<RecyclagePlanning> opPlannings = entry.getValue();

            String operatorName = (op.getLastName() != null ? op.getLastName() : "") + " " + (op.getFirstName() != null ? op.getFirstName() : "");
            String matricule = op.getEmployeeId() != null ? op.getEmployeeId() : "-";
            String scheduledDateStr = opPlannings.get(0).getScheduledDate().toString();

            List<String> wsNames = opPlannings.stream()
                    .map(p -> p.getWorkstation().getName())
                    .filter(name -> name != null && !name.isBlank())
                    .distinct()
                    .toList();
            String workstationsJoined = String.join(", ", wsNames);

            String inAppMessage = categoryTitle + " dans 10 jours pour " + operatorName + " sur le(s) poste(s) : " + workstationsJoined + " (Date prévue : " + scheduledDateStr + ")";

            String emailBody = "Bonjour,\n\n" +
                    "Rappel d'échéance à J-10 pour l'opérateur :\n" +
                    "• Opérateur : " + operatorName + " (Matricule : " + matricule + ")\n" +
                    "• Motif / Type : " + categoryTitle + "\n" +
                    "• Date prévue : " + scheduledDateStr + "\n" +
                    "• Postes de travail concernés (" + wsNames.size() + ") :\n" +
                    wsNames.stream().map(w -> "   - " + w).collect(Collectors.joining("\n")) + "\n\n" +
                    "Merci de bien vouloir planifier l'évaluation de recyclage correspondante.";

            String emailSubject = "[Système ILU] " + categoryTitle + " (J-10) - " + operatorName;

            for (User hrUser : hrUsers) {
                if (hrUser.getEmail() != null && !hrUser.getEmail().isBlank()) {
                    emailService.sendEmail(hrUser.getEmail(), emailSubject, emailBody);
                } else {
                    emailService.sendEmail(null, emailSubject, emailBody);
                }

                Notification notif = new Notification();
                notif.setRecipientId(hrUser.getId());
                notif.setRecipientType(RecipientType.HR);
                notif.setType(NotificationType.RECYCLAGE_10J);
                notif.setMessage(inAppMessage);
                notif.setRead(false);
                notif.setEmailSent(true);
                notif.setRelatedOperatorId(op.getId());
                notif.setRelatedPlanningId(opPlannings.get(0).getId());
                notificationRepository.save(notif);
                count++;
            }

            for (RecyclagePlanning p : opPlannings) {
                NotificationSent sentRecord = new NotificationSent();
                sentRecord.setPlanningId(p.getId());
                sentRecord.setDaysBefore(targetDaysBefore);
                sentRecord.setNotificationType(NotificationType.RECYCLAGE_10J);
                notificationSentRepository.save(sentRecord);
            }
        }
        return count;
    }

    private int createPlanningNotifications(RecyclagePlanning planning, NotificationType type, String message,
                                            RecipientType recipientType, List<User> recipients) {
        int count = 0;
        for (User recipient : recipients) {
            Notification notification = baseNotification(recipient, recipientType, type, message);
            notification.setRelatedPlanningId(planning.getId());
            notification.setRelatedOperatorId(planning.getOperator().getId());
            notificationRepository.save(notification);
            count++;
        }
        return count;
    }

    private Notification baseNotification(User recipient, RecipientType recipientType, NotificationType type, String message) {
        Notification notification = new Notification();
        notification.setRecipientId(recipient.getId());
        notification.setRecipientType(recipientType);
        notification.setType(type);
        notification.setMessage(message);
        notification.setRead(false);

        boolean sent = false;
        String subject = (type == NotificationType.RECYCLAGE_10J) 
                ? "[Système ILU] Alerte Recyclage Annuel (J-10)" 
                : "[Système ILU] Notification: " + type.name();

        if (recipient != null && recipient.getEmail() != null && !recipient.getEmail().isBlank()) {
            sent = emailService.sendEmail(recipient.getEmail(), subject, message);
        } else {
            // Attempt sending using system test recipient if configured
            sent = emailService.sendEmail(null, subject, message);
        }

        notification.setEmailSent(sent);
        return notification;
    }

    private List<User> projectTeamLeaders(RecyclagePlanning planning) {
        if (planning.getProjectId() == null) return usersWithRole("CHEF_EQUIPE");
        List<String> employeeIds = teamRepository.findByProjectId(planning.getProjectId()).stream()
                .map(team -> team.getTeamLeaderEmployeeId())
                .filter(id -> id != null && !id.isBlank())
                .toList();
        List<User> leaders = userRepository.findAll().stream()
                .filter(User::getActive)
                .filter(user -> employeeIds.contains(user.getEmployeeId()))
                .toList();
        return leaders.isEmpty() ? usersWithRole("CHEF_EQUIPE") : leaders;
    }

    private List<User> usersWithRole(String role) {
        return userRepository.findAll().stream()
                .filter(User::getActive)
                .filter(user -> user.getRoles().stream().anyMatch(item -> role.equals(item.getLabel())))
                .toList();
    }

    public List<Map<String, Object>> getNotificationsForUser(Long userId) {
        List<Notification> notifications = notificationRepository.findByRecipientIdOrderByCreatedAtDesc(userId);
        List<Map<String, Object>> result = new ArrayList<>();
        for (Notification notification : notifications) {
            final Long notifId = notification.getId();
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", notifId);
            map.put("recipientId", notification.getRecipientId());
            map.put("recipientType", notification.getRecipientType().name());
            map.put("type", notification.getType().name());
            map.put("message", notification.getMessage());
            map.put("relatedPlanningId", notification.getRelatedPlanningId());
            map.put("relatedOperatorId", notification.getRelatedOperatorId());
            map.put("read", notification.getRead());
            map.put("emailSent", notification.getEmailSent());
            map.put("createdAt", notification.getCreatedAt() != null ? notification.getCreatedAt().format(dateTimeFormatter) : null);
            result.add(map);
        }
        return result;
    }

    public long getUnreadCount(Long userId) {
        return notificationRepository.countByRecipientIdAndReadFalse(userId);
    }

    @Transactional
    public void markAsRead(Long notificationId, Long recipientId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found with id: " + notificationId));
        if (!recipientId.equals(notification.getRecipientId())) {
            throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.FORBIDDEN,
                    "Cette notification ne vous appartient pas");
        }
        notification.setRead(true);
        notificationRepository.save(notification);
    }

    @Transactional
    public void markAllAsRead(Long userId) {
        List<Notification> unread = notificationRepository.findByRecipientIdAndReadFalse(userId);
        for (Notification notification : unread) {
            notification.setRead(true);
            notificationRepository.save(notification);
        }
    }

    @Transactional
    public void deleteNotification(Long notificationId, Long recipientId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND,
                        "Notification introuvable avec l'id: " + notificationId));
        if (!recipientId.equals(notification.getRecipientId())) {
            throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.FORBIDDEN,
                    "Cette notification ne vous appartient pas");
        }
        notificationRepository.delete(notification);
    }

    @Transactional
    public void deleteAllNotifications(Long recipientId) {
        notificationRepository.deleteByRecipientId(recipientId);
    }

    @Transactional
    public void createAbsenceNotification(Long operatorId, String operatorName, NotificationType type) {
        String message;
        if (type == NotificationType.ABSENCE_DEBUT) {
            message = "Début d'absence pour " + operatorName;
        } else if (type == NotificationType.ABSENCE_REPRISE) {
            message = "Reprise après absence pour " + operatorName;
        } else {
            return;
        }

        for (User user : usersWithRole("CHEF_EQUIPE")) {
            Notification notification = baseNotification(user, RecipientType.CHEF_EQUIPE, type, message);
            notification.setRelatedOperatorId(operatorId);
            notificationRepository.save(notification);
        }
    }

    @Transactional
    public void createDepartureNotification(Long operatorId, String operatorName) {
        String message = "Départ de " + operatorName;

        for (User user : usersWithRole("CHEF_EQUIPE")) {
            Notification notification = baseNotification(user, RecipientType.CHEF_EQUIPE, NotificationType.DEPART, message);
            notification.setRelatedOperatorId(operatorId);
            notificationRepository.save(notification);
        }

        // Also notify HR about departures
        for (User user : usersWithRole("RH")) {
            Notification notification = baseNotification(user, RecipientType.HR, NotificationType.DEPART, message);
            notification.setRelatedOperatorId(operatorId);
            notificationRepository.save(notification);
        }
    }

    // FIX 3: RecyclageService.startEvaluation() flips a planning to EN_COURS but never told
    // anyone else it happened. Mirrors createDepartureNotification: notify chefs d'équipe
    // (so peers/other shifts see it right away instead of waiting for a page refresh) and HR.
    @Transactional
    public void createRecyclageStartedNotification(Long planningId, Long operatorId, String operatorName, String workstationName) {
        String message = "Recyclage démarré pour " + operatorName + " sur " + workstationName;

        for (User user : usersWithRole("CHEF_EQUIPE")) {
            Notification notification = baseNotification(user, RecipientType.CHEF_EQUIPE, NotificationType.RECYCLAGE_DEMARRE, message);
            notification.setRelatedPlanningId(planningId);
            notification.setRelatedOperatorId(operatorId);
            notificationRepository.save(notification);
        }

        for (User user : usersWithRole("RH")) {
            Notification notification = baseNotification(user, RecipientType.HR, NotificationType.RECYCLAGE_DEMARRE, message);
            notification.setRelatedPlanningId(planningId);
            notification.setRelatedOperatorId(operatorId);
            notificationRepository.save(notification);
        }
    }

    public static NotificationType absenceStartType() { return NotificationType.ABSENCE_DEBUT; }
    public static NotificationType absenceReturnType() { return NotificationType.ABSENCE_REPRISE; }
}
