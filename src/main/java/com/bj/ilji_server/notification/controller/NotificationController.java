package com.bj.ilji_server.notification.controller;

import com.bj.ilji_server.notification.dto.*;
import com.bj.ilji_server.notification.entity.Notification;
import com.bj.ilji_server.notification.service.NotificationService;
import com.bj.ilji_server.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService service;

    @GetMapping
    public NotificationListResponse list(
            @AuthenticationPrincipal User user,           // ★ Jwt 아님! User 주입
            @RequestParam(defaultValue = "ALL") String status,
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "20") int limit
    ) {
        Long userId = user.getId();                      // ★ 바로 user_id 사용
        Page<Notification> page = service.list(userId, status, offset, limit);
        List<NotificationItemDto> items = page.getContent().stream()
                .map(NotificationDtoMapper::toDto)
                .toList();
        return NotificationListResponse.builder()
                .items(items).total(page.getTotalElements()).offset(offset).limit(limit)
                .build();
    }

    @GetMapping("/unread-count")
    public UnreadCountResponse unreadCount(@AuthenticationPrincipal User user) {
        return UnreadCountResponse.builder()
                .count(service.unreadCount(user.getId()))
                .build();
    }

    @PatchMapping("/{id}/read")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void markRead(@AuthenticationPrincipal User user, @PathVariable Long id) {
        service.markRead(id, user.getId());
    }

    @PostMapping("/read-all")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void markAllRead(@AuthenticationPrincipal User user) {
        service.markAllRead(user.getId());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT) // 204
    public void deleteOne(
            @AuthenticationPrincipal User user,
            @PathVariable Long id
    ) {
        service.deleteOneForRecipient(id, user.getId());
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT) // 204
    public void deleteAll(@AuthenticationPrincipal User user) {
        service.deleteAllForRecipient(user.getId());
    }

}
