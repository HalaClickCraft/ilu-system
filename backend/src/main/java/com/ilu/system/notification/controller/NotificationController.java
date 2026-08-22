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

    public NotificationController(NotificationService notificationService, AccessControlService accessControlService) {
        this.notificationService = notificationService;
        this.accessControlService = accessControlService;
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
}
