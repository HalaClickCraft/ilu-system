package com.ilu.system.notification.controller;

import com.ilu.system.notification.service.NotificationService;
import com.ilu.system.security.AccessControlService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@CrossOrigin(origins = "*")
public class NotificationController {

    private final NotificationService notificationService;
    private final AccessControlService accessControlService;
    private final com.ilu.system.notification.service.EmailService emailService;

    public NotificationController(NotificationService notificationService,
                                AccessControlService accessControlService,
                                com.ilu.system.notification.service.EmailService emailService) {
        this.notificationService = notificationService;
        this.accessControlService = accessControlService;
        this.emailService = emailService;
    }

    @GetMapping
    public List<Map<String, Object>> getNotificationsForUser(Authentication authentication) {
        return notificationService.getNotificationsForUser(accessControlService.currentUser(authentication).getId());
    }

    @GetMapping("/unread-count")
    public Map<String, Object> getUnreadCount(Authentication authentication) {
        Long userId = accessControlService.currentUser(authentication).getId();
        long count = notificationService.getUnreadCount(userId);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("userId", userId);
        result.put("unreadCount", count);
        return result;
    }

    @PutMapping("/{id}/read")
    public Map<String, Object> markAsRead(@PathVariable Long id, Authentication authentication) {
        notificationService.markAsRead(id, accessControlService.currentUser(authentication).getId());
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("message", "Notification marked as read");
        result.put("notificationId", id);
        return result;
    }

    @PutMapping("/read-all")
    public Map<String, Object> markAllAsRead(Authentication authentication) {
        Long userId = accessControlService.currentUser(authentication).getId();
        notificationService.markAllAsRead(userId);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("message", "All notifications marked as read");
        result.put("userId", userId);
        return result;
    }

    @DeleteMapping("/{id}")
    public Map<String, Object> deleteNotification(@PathVariable Long id, Authentication authentication) {
        Long userId = accessControlService.currentUser(authentication).getId();
        notificationService.deleteNotification(id, userId);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("message", "Notification deleted");
        result.put("notificationId", id);
        return result;
    }

    @DeleteMapping("/clear-all")
    public Map<String, Object> clearAllNotifications(Authentication authentication) {
        Long userId = accessControlService.currentUser(authentication).getId();
        notificationService.deleteAllNotifications(userId);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("message", "All notifications cleared");
        result.put("userId", userId);
        return result;
    }

    @PostMapping("/test-email")
    public Map<String, Object> sendTestEmail(@RequestParam(required = false) String email, Authentication authentication) {
        var user = accessControlService.currentUser(authentication);
        String targetEmail = (email != null && !email.isBlank()) ? email : (user != null ? user.getEmail() : null);
        boolean success = emailService.sendEmail(targetEmail, "[Système ILU] Test Email Notification RH",
                "Bonjour,\n\nCeci est un e-mail de test envoyé par le Système ILU OPmobility pour vérifier la bonne réception des notifications par e-mail.");
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("success", success);
        result.put("recipient", targetEmail != null ? targetEmail : "None configured");
        result.put("message", success ? "E-mail de test envoyé avec succès à " + targetEmail
                : "Avertissement: Impossible d'envoyer l'e-mail. Assurez-vous d'avoir configuré le serveur SMTP (ex: Gmail / Outlook) dans application.properties ou spécifié un e-mail de destinataire.");
        return result;
    }
}
