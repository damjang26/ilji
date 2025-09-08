package com.bj.ilji_server.notification.packing;

import com.bj.ilji_server.notification.entity.Notification;
import com.bj.ilji_server.notification.service.NotificationService;
import com.bj.ilji_server.notification.type.EntityType;
import com.bj.ilji_server.notification.type.NotificationType;
import com.bj.ilji_server.notification.type.IdempotencyKey;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.HashMap;
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
        n.setMessageTitle(actorName + "님이 댓글을 남겼어요");
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
        n.setMessageTitle(actorName + "님이 좋아요를 눌렀어요");
        n.setMessageBody(postTitle);
        n.setLinkUrl("/posts/" + postId);
        n.setIdempotencyKey(IdempotencyKey.instant(
                recipientId, NotificationType.LIKE_CREATED, EntityType.LIKE, likeId));
        n.setMetaJson(writeJson(meta));

        notificationService.create(n);
    }

    /** 팔로우 요청 알림 (7일 쿨다운 적용) */
    public void followRequested(Long targetUserId, Long followerId, String followerName) {
        // 최근 7일 내 같은 (followerId -> targetUserId) 팔로우 알림이 있었다면 스킵
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
        n.setMessageTitle(followerName + "님이 팔로우를 요청했어요");
        n.setLinkUrl("/profile/" + followerId);
        n.setIdempotencyKey(IdempotencyKey.instant(
                targetUserId, NotificationType.FOLLOW_REQUEST, EntityType.FOLLOW, followerId));
        n.setMetaJson(writeJson(meta));

        notificationService.create(n);
    }


    /** 팔로우 수락 알림 */
    public void followAccepted(Long followerUserId, Long followeeId, String followeeName) {
        Map<String, Object> meta = Map.of("followeeId", followeeId, "followeeName", followeeName);

        Notification n = new Notification();
        n.setRecipientId(followerUserId);
        n.setSenderId(followeeId);
        n.setType(NotificationType.FOLLOW_ACCEPTED);
        n.setEntityType(EntityType.FOLLOW);
        n.setMessageTitle(followeeName + "님이 팔로우를 수락했어요");
        n.setLinkUrl("/profile/" + followeeId);
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

        Notification n = new Notification();
        n.setRecipientId(recipientId);
        n.setSenderId(authorId);
        n.setType(NotificationType.FRIEND_POST_CREATED);
        n.setEntityType(EntityType.DIARY); // ILog -> DIARY로 매핑
        n.setEntityId(diaryId);
        n.setMessageTitle(authorName + "님이 일기를 작성했어요");
        n.setLinkUrl("/ilog/" + diaryId);
        n.setIdempotencyKey(IdempotencyKey.instant(
                recipientId, NotificationType.FRIEND_POST_CREATED, EntityType.DIARY, diaryId));
        n.setMetaJson(writeJson(meta));

        notificationService.create(n);
    }

    private String writeJson(Object o) {
        try { return objectMapper.writeValueAsString(o); }
        catch (JsonProcessingException e) { return "{}"; }
    }
}
