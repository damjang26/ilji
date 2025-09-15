package com.bj.ilji_server.notification.entity;

import com.bj.ilji_server.notification.type.EntityType;
import com.bj.ilji_server.notification.type.NotificationStatus;
import com.bj.ilji_server.notification.type.NotificationType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
        name = "notifications",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_notif_idem", columnNames = "idempotency_key")
        },
        indexes = {
                @Index(name = "ix_notif_recipient_created", columnList = "recipient_id, created_at DESC")
        }
)
@Getter
@Setter
@NoArgsConstructor
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Oracle 12c+ IDENTITY 지원
    private Long id;

    @Column(name = "recipient_id", nullable = false)
    private Long recipientId;

    @Column(name = "sender_id")
    private Long senderId;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 50)
    private NotificationType type;

    @Enumerated(EnumType.STRING)
    @Column(name = "entity_type", nullable = false, length = 50)
    private EntityType entityType;

    @Column(name = "entity_id")
    private Long entityId;

    @Column(name = "message_title", length = 255)
    private String messageTitle;

    @Lob
    @Column(name = "message_body")
    private String messageBody;

    @Column(name = "link_url", length = 1024)
    private String linkUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private NotificationStatus status = NotificationStatus.NEW;

    // DB 기본값: SYSTIMESTAMP. JPA insert 시 null이면 보완
    @Column(name = "created_at", nullable = false,
            columnDefinition = "TIMESTAMP DEFAULT SYSTIMESTAMP")
    private java.time.OffsetDateTime createdAt;

    @Column(name = "idempotency_key", nullable = false, length = 100)
    private String idempotencyKey;

    @Lob
    @Column(name = "meta_json")
    private String metaJson;

    @PrePersist
    void prePersist() {
        if (createdAt == null) {
            createdAt = java.time.OffsetDateTime.now(); // 서버 시간 보정
        }
        if (status == null) status = NotificationStatus.NEW;
    }
}
