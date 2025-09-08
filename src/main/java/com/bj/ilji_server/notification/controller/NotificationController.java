package com.bj.ilji_server.notification.controller;

import com.bj.ilji_server.notification.dto.NotificationResponse;
import com.bj.ilji_server.notification.service.NotificationService;
import com.bj.ilji_server.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public ResponseEntity<List<NotificationResponse>> getNotifications(@AuthenticationPrincipal User currentUser) {
        List<NotificationResponse> notifications = notificationService.getNotifications(currentUser);
        return ResponseEntity.ok(notifications);
    }

    @PostMapping("/{notificationId}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable Long notificationId, @AuthenticationPrincipal User currentUser) {
        notificationService.markNotificationAsRead(notificationId, currentUser);
        return ResponseEntity.ok().build();
    }
}
