package com.bj.ilji_server.notification.repository;

import com.bj.ilji_server.notification.entity.Notification;
import com.bj.ilji_server.notification.type.NotificationStatus;
import com.bj.ilji_server.notification.type.NotificationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import java.util.Optional;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    @Query("""
        SELECT n FROM Notification n
         WHERE n.recipientId = :recipientId
           AND (:status = 'ALL' OR n.status = :status)
         ORDER BY n.createdAt DESC
    """)
    Page<Notification> findPage(Long recipientId, String status, Pageable pageable);

    long countByRecipientIdAndStatus(Long recipientId, NotificationStatus status);

    Optional<Notification> findByIdAndRecipientId(Long id, Long recipientId);

    // ★ 포지셔널 파라미터 버전 (named param 사용 안 함)
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
       UPDATE Notification n
          SET n.status = ?2
        WHERE n.recipientId = ?1
          AND n.status = ?3
    """)
    int markAllRead(Long recipientId, NotificationStatus read, NotificationStatus neu);



    // 최근 FOLLOW_REQUEST 알림 1건 조회
    Optional<Notification> findFirstByRecipientIdAndSenderIdAndTypeOrderByCreatedAtDesc(
            Long recipientId, Long senderId, NotificationType type);

    Optional<Notification> findByIdempotencyKey(String idempotencyKey);

}
