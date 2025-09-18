package com.bj.ilji_server.notification.packing;

import com.bj.ilji_server.notification.entity.Notification;
import com.bj.ilji_server.notification.service.NotificationService;
import com.bj.ilji_server.notification.type.EntityType;
import com.bj.ilji_server.notification.type.IdempotencyKey;
import com.bj.ilji_server.notification.type.NotificationType;
import com.bj.ilji_server.schedule.dto.ScheduleBrief;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class NotificationComposer {

    private final NotificationService notificationService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /** 댓글 생성 알림 */
    public void commentCreated(Long recipientId, Long actorId,
                               Long postId, Long commentId,
                               String actorName, String postTitle, String snippet) {

        Map<String, Object> meta = new HashMap<>();
        meta.put("actorId", actorId);
        meta.put("actorName", actorName);
        meta.put("postId", postId);
        meta.put("commentId", commentId);
        meta.put("postTitle", postTitle);
        meta.put("snippet", snippet);

        Notification n = new Notification();
        n.setRecipientId(recipientId);
        n.setSenderId(actorId);
        n.setType(NotificationType.COMMENT_CREATED);
        n.setEntityType(EntityType.COMMENT);
        n.setEntityId(commentId);
        n.setMessageTitle(actorName + " commented on your post");
        n.setMessageBody(snippet);
        n.setLinkUrl("/posts/" + postId + "?comment=" + commentId);
        n.setIdempotencyKey(IdempotencyKey.instant(
                recipientId, NotificationType.COMMENT_CREATED, EntityType.COMMENT, commentId));
        n.setMetaJson(writeJson(meta));

        notificationService.create(n);
    }

    /** 좋아요 알림 */
    public void likeCreated(Long recipientId, Long actorId,
                            Long postId, Long likeId, String actorName, String postTitle) {

        Map<String, Object> meta = new HashMap<>();
        meta.put("actorId", actorId);
        meta.put("actorName", actorName);
        meta.put("postId", postId);
        meta.put("likeId", likeId);
        meta.put("postTitle", postTitle);

        Notification n = new Notification();
        n.setRecipientId(recipientId);
        n.setSenderId(actorId);
        n.setType(NotificationType.LIKE_CREATED);
        n.setEntityType(EntityType.LIKE);
        n.setEntityId(likeId);
        n.setMessageTitle(actorName + " liked your post");
        n.setMessageBody(postTitle);
        n.setLinkUrl("/posts/" + postId);
        n.setIdempotencyKey(IdempotencyKey.instant(
                recipientId, NotificationType.LIKE_CREATED, EntityType.LIKE, likeId));
        n.setMetaJson(writeJson(meta));

        notificationService.create(n);
    }

    /** 팔로우 요청 알림 (주(week) 멱등키 + 7일 쿨다운 + REQUIRES_NEW save/flush) */
    public void followRequested(Long targetUserId, Long followerId, String followerName) {
        // 1) 7일 쿨다운
        if (notificationService.sentFollowNotifWithin(
                targetUserId, followerId, Duration.ofDays(7))) {
            return;
        }

        Map<String, Object> meta = Map.of("followerId", followerId, "followerName", followerName);

        Notification n = new Notification();
        n.setRecipientId(targetUserId);
        n.setSenderId(followerId);
        n.setType(NotificationType.FOLLOW_REQUEST);
        n.setEntityType(EntityType.FOLLOW);
        n.setMessageTitle(followerName + " sent you a follow request");
        n.setLinkUrl("/mypage/" + followerId);

        // 2) 주(ISO week, KST) 멱등키
        String key = IdempotencyKey.weeklyFollowRequestKey(targetUserId, followerId);
        n.setIdempotencyKey(key);
        n.setMetaJson(writeJson(meta));

        // 3) 저장 시도 (REQUIRES_NEW + flush로 즉시 UNIQUE 위반을 여기서 처리)
        try {
            notificationService.createAndFlushNewTx(n);
        } catch (org.springframework.dao.DataIntegrityViolationException ignored) {
            // 같은 주차에 이미 존재 → 무음 처리
        }
    }

    /** 팔로우 수락 알림 */
    public void followAccepted(Long followerUserId, Long followeeId, String followeeName) {
        Map<String, Object> meta = Map.of("followeeId", followeeId, "followeeName", followeeName);

        Notification n = new Notification();
        n.setRecipientId(followerUserId);
        n.setSenderId(followeeId);
        n.setType(NotificationType.FOLLOW_ACCEPTED);
        n.setEntityType(EntityType.FOLLOW);
        n.setMessageTitle(followeeName + " accepted your follow request");
        n.setLinkUrl("/mypage/" + followeeId);
        n.setIdempotencyKey(IdempotencyKey.instant(
                followerUserId, NotificationType.FOLLOW_ACCEPTED, EntityType.FOLLOW, followeeId));
        n.setMetaJson(writeJson(meta));

        notificationService.create(n);
    }

    /** 친구가 공개/친구공유 일기를 작성했을 때 (ILog를 DIARY로 취급) */
    public void friendDiaryCreated(Long recipientId, Long authorId,
                                   Long diaryId, String authorName, String dateIso) {

        Map<String, Object> meta = new HashMap<>();
        meta.put("authorId", authorId);
        meta.put("authorName", authorName);
        meta.put("diaryId", diaryId);
        meta.put("date", dateIso);

        // Parse dateIso and format it
        LocalDate logDate = LocalDate.parse(dateIso);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd");
        String formattedDate = logDate.format(formatter);

        Notification n = new Notification();
        n.setRecipientId(recipientId);
        n.setSenderId(authorId);
        n.setType(NotificationType.FRIEND_POST_CREATED);
        n.setEntityType(EntityType.DIARY); // ILog -> DIARY로 매핑
        n.setEntityId(diaryId);
        n.setMessageTitle(authorName + " wrote a new log (" + formattedDate + ")"); // Modified line
        n.setLinkUrl("/mypage/" + authorId);
        n.setIdempotencyKey(IdempotencyKey.instant(
                recipientId, NotificationType.FRIEND_POST_CREATED, EntityType.DIARY, diaryId));
        n.setMetaJson(writeJson(meta));

        notificationService.create(n);
    }

    /** 일정 요약 알림 (예: "오늘 일정 N개가 있습니다") */
    public void scheduleDailySummary(Long recipientId, LocalDate date,
                                     int totalCount, List<ScheduleBrief> topItems) {
        if (totalCount <= 0) return; // 잘못 호출되더라도 생성 방지

        // 유저별 '하루 1건' 멱등을 위해 날짜 기반 키 사용
        long yyyymmdd = date.getYear() * 10000L + date.getMonthValue() * 100L + date.getDayOfMonth();

        String title = "You have " + totalCount + " schedules today";
        String body  = renderScheduleLines(topItems);

        Notification n = new Notification();
        n.setRecipientId(recipientId);
        n.setType(NotificationType.SCHEDULE_DAILY_SUMMARY);
        n.setEntityType(EntityType.SCHEDULE);
        n.setMessageTitle(title);
        n.setMessageBody(body);
        n.setLinkUrl("/schedules?date=" + date); // 라우팅 규칙에 맞게 수정 가능

        n.setIdempotencyKey(IdempotencyKey.instant(
                recipientId, NotificationType.SCHEDULE_DAILY_SUMMARY, EntityType.SCHEDULE, yyyymmdd));

        // (옵션) 메타: 날짜/총개수/상단 몇 개의 텍스트 라인
        try {
            var meta = new java.util.HashMap<String, Object>();
            meta.put("date", date.toString());
            meta.put("total", totalCount);
            meta.put("top", topItems == null ? java.util.List.of()
                    : topItems.stream().limit(5).map(this::formatLine).toList());
            n.setMetaJson(objectMapper.writeValueAsString(meta));
        } catch (Exception ignore) { /* 본문만으로도 충분하니 무시 */ }

        notificationService.create(n);
    }

    /** 본문 줄 생성 */
    private String renderScheduleLines(List<ScheduleBrief> items) {
        if (items == null || items.isEmpty()) return "오늘 등록된 일정이 없습니다.";
        StringBuilder sb = new StringBuilder();
        for (ScheduleBrief it : items.stream().limit(5).toList()) {
            sb.append("- ").append(formatLine(it)).append('\n');
        }
        return sb.toString();
    }

    /** "HH:mm~HH:mm 제목" 형태(시간 없으면 제목만) */
    private String formatLine(ScheduleBrief it) {
        DateTimeFormatter f = DateTimeFormatter.ofPattern("HH:mm");
        var s = it.startsAt();
        var e = it.endsAt();
        if (s != null && e != null) {
            return s.format(f) + "~" + e.format(f) + " " + it.title();
        } else if (s != null) {
            return s.format(f) + " " + it.title();
        } else {
            return it.title();
        }
    }

    private String writeJson(Object o) {
        try { return objectMapper.writeValueAsString(o); }
        catch (JsonProcessingException e) { return "{}"; }
    }
}
