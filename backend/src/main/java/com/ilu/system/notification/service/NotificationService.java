package com.ilu.system.notification.service;

import com.ilu.system.auth.entity.User;
import com.ilu.system.auth.repository.UserRepository;
import com.ilu.system.notification.entity.Notification;
import com.ilu.system.notification.entity.NotificationSent;
import com.ilu.system.notification.repository.NotificationRepository;
import com.ilu.system.notification.repository.NotificationSentRepository;
import com.ilu.system.recyclage.entity.RecyclagePlanning;
import com.ilu.system.recyclage.repository.RecyclagePlanningRepository;
import com.ilu.system.structure.entity.ProjectMember;
import com.ilu.system.structure.repository.ProjectMemberRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.ilu.system.notification.entity.Notification.NotificationType;
import static com.ilu.system.notification.entity.Notification.RecipientType;
import static com.ilu.system.recyclage.entity.RecyclagePlanning.PlanningStatus;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationSentRepository notificationSentRepository;
    private final RecyclagePlanningRepository recyclagePlanningRepository;
    private final UserRepository userRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    public NotificationService(NotificationRepository notificationRepository,
                               NotificationSentRepository notificationSentRepository,
                               RecyclagePlanningRepository recyclagePlanningRepository,
                               UserRepository userRepository,
                               ProjectMemberRepository projectMemberRepository) {
        this.notificationRepository = notificationRepository;
        this.notificationSentRepository = notificationSentRepository;
        this.recyclagePlanningRepository = recyclagePlanningRepository;
        this.userRepository = userRepository;
        this.projectMemberRepository = projectMemberRepository;
    }

    @Scheduled(cron = "0 0 8 * * *")
    @Transactional
    public Map<String, Object> checkAndSendNotifications() {
        List<RecyclagePlanning> planifieePlannings = recyclagePlanningRepository.findByStatus(PlanningStatus.PLANIFIEE);
        LocalDate today = LocalDate.now();

        // Notification schedule: days -> notification type
        Map<Integer, NotificationType> schedule = new LinkedHashMap<>();
        schedule.put(30, NotificationType.RECYCLAGE_30J);
        schedule.put(20, NotificationType.RECYCLAGE_20J);
        schedule.put(15, NotificationType.RECYCLAGE_15J);
        schedule.put(10, NotificationType.RECYCLAGE_10J);

        int createdCount = 0;

        for (RecyclagePlanning planning : planifieePlannings) {
            final Long planningId = planning.getId();
            long daysRemaining = ChronoUnit.DAYS.between(today, planning.getScheduledDate());

            for (Map.Entry<Integer, NotificationType> entry : schedule.entrySet()) {
                final int daysBefore = entry.getKey();
                final NotificationType notifType = entry.getValue();

                if (daysRemaining == daysBefore) {
                    // Check if already sent
                    boolean alreadySent = notificationSentRepository.existsByPlanningIdAndDaysBefore(planningId, daysBefore);
                    if (alreadySent) {
                        continue;
                    }

                    final String operatorName = planning.getOperator().getLastName() + " " + planning.getOperator().getFirstName();
                    final String workstationName = planning.getWorkstation().getName();
                    final String chefMessage = "Recyclage dans " + daysBefore + " jours pour " + operatorName + " sur " + workstationName;

                    createdCount += createPlanningNotifications(planning, notifType, chefMessage,
                            RecipientType.CHEF_EQUIPE, projectTeamLeaders(planning));

                    // At 10 days, also notify HR (recipientId=2 as placeholder)
                    if (daysBefore == 10) {
                        final String hrMessage = "Recyclage dans " + daysBefore + " jours pour " + operatorName + " sur " + workstationName;
                        createdCount += createPlanningNotifications(planning, notifType, hrMessage,
                                RecipientType.HR, usersWithRole("RH"));
                    }

                    // Record in NotificationSent to prevent duplicates
                    NotificationSent sentRecord = new NotificationSent();
                    sentRecord.setPlanningId(planningId);
                    sentRecord.setDaysBefore(daysBefore);
                    sentRecord.setNotificationType(notifType);
                    notificationSentRepository.save(sentRecord);
                }
            }
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("notificationsCreated", createdCount);
        result.put("date", today.toString());
        return result;
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
        notification.setEmailSent(false);
        return notification;
    }

    private List<User> projectTeamLeaders(RecyclagePlanning planning) {
        if (planning.getProjectId() == null) return usersWithRole("CHEF_EQUIPE");
        List<String> employeeIds = projectMemberRepository.findByProjectId(planning.getProjectId()).stream()
                .filter(member -> member.getProjectRole() == ProjectMember.ProjectRole.TEAM_LEADER)
                .map(ProjectMember::getEmployeeId).toList();
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
