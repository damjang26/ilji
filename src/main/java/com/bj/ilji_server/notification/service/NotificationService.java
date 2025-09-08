package com.bj.ilji_server.notification.service;

import com.bj.ilji_server.notification.entity.Notification;
import com.bj.ilji_server.notification.type.NotificationStatus;
import com.bj.ilji_server.notification.repository.NotificationRepository;
import com.bj.ilji_server.notification.type.NotificationType;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository repository;

    @PersistenceContext
    private EntityManager em;

    /** 알림 생성 (idempotency_key UNIQUE 충돌 시 조용히 무시 = 멱등) */
    @Transactional
    public void create(Notification n) {
        try {
            repository.save(n);
        } catch (DataIntegrityViolationException e) {
            // idempotency_key UNIQUE 위반이면 중복 생성이므로 무시
        }
    }

    /** 목록 조회 (status = "ALL" 또는 "NEW"/"READ"/"ARCHIVED") */
    @Transactional(readOnly = true)
    public Page<Notification> list(Long userId, String status, int offset, int limit) {
        int safeLimit = Math.max(1, limit);
        int page = Math.max(0, offset / safeLimit);
        return repository.findPage(userId, status, PageRequest.of(page, safeLimit));
    }

    /** 미확인(NEW) 개수 */
    @Transactional(readOnly = true)
    public long unreadCount(Long userId) {
        return repository.countByRecipientIdAndStatus(userId, NotificationStatus.NEW);
    }

    /** 단건 읽음 처리 */
    @Transactional
    public void markRead(Long id, Long userId) {
        Notification n = repository.findByIdAndRecipientId(id, userId)
                .orElseThrow(() -> new IllegalArgumentException("Notification not found or not mine"));
        if (n.getStatus() == NotificationStatus.NEW) {
            n.setStatus(NotificationStatus.READ);
        }
    }

    /** 모두 읽음 처리 (벌크 UPDATE) */
    @Transactional
    public int markAllRead(Long userId) {
        return repository.markAllRead(userId, NotificationStatus.READ, NotificationStatus.NEW);
    }

    @Transactional(readOnly = true)
    public boolean sentFollowNotifWithin(Long recipientId, Long senderId, Duration window) {
        return repository
                .findFirstByRecipientIdAndSenderIdAndTypeOrderByCreatedAtDesc(
                        recipientId, senderId, NotificationType.FOLLOW_REQUEST)
                .map(n -> n.getCreatedAt().isAfter(java.time.OffsetDateTime.now().minus(window)))
                .orElse(false);
    }


}
