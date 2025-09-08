package com.bj.ilji_server.notification.type;

import org.apache.commons.codec.digest.DigestUtils;

/**
 * 알림 중복 방지 키 생성 유틸리티
 * - 같은 입력 → 항상 같은 해시값(64자 HEX)
 * - notifications.idempotency_key UNIQUE 제약과 함께 사용
 */
public final class IdempotencyKey {

    private IdempotencyKey() {}

    /**
     * 알림 중복 방지 키 생성
     * @param recipientId  수신자 ID
     * @param type         알림 타입
     * @param entityType   엔티티 타입
     * @param entityId     엔티티 ID (없으면 null)
     * @param bucket       버킷(일일 요약 등 구분용, 없으면 "")
     * @return SHA-256 HEX 문자열
     */
    public static String of(
            long recipientId,
            NotificationType type,
            EntityType entityType,
            Long entityId,
            String bucket
    ) {
        String raw = recipientId + "|" + type + "|" + entityType + "|" +
                (entityId == null ? "" : entityId) + "|" +
                (bucket == null ? "" : bucket);
        return DigestUtils.sha256Hex(raw);
    }

    /** 즉시형 알림(댓글/좋아요 등) → bucket 없음 */
    public static String instant(long recipientId, NotificationType type, EntityType entityType, Long entityId) {
        return of(recipientId, type, entityType, entityId, "");
    }

    /** 일일 요약 알림(일정, 일기) → yyyy-MM-dd 버킷 */
    public static String daily(long recipientId, NotificationType type, EntityType entityType, Long entityId, java.time.LocalDate day) {
        return of(recipientId, type, entityType, entityId, day == null ? "" : day.toString());
    }
}
