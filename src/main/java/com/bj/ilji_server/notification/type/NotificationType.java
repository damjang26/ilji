package com.bj.ilji_server.notification.type;

public enum NotificationType {
    SCHEDULE_DAILY_SUMMARY,   // 오늘 일정 요약
    DIARY_REMINDER,           // 일기 작성 리마인드
    FRIEND_POST_CREATED,      // 친구 공개/공유 포스트
    COMMENT_CREATED,          // 댓글 생성
    LIKE_CREATED,             // 좋아요 생성
    FOLLOW_REQUEST,           // 팔로우 요청
    FOLLOW_ACCEPTED           // 팔로우 수락
}
