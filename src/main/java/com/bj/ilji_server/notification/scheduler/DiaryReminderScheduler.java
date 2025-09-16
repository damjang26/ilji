package com.bj.ilji_server.notification.scheduler;

import com.bj.ilji_server.notification.service.NotificationService;
import com.bj.ilji_server.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;


import com.bj.ilji_server.notification.entity.Notification;
import com.bj.ilji_server.notification.type.NotificationType;
import com.bj.ilji_server.notification.type.EntityType;
import com.bj.ilji_server.notification.type.IdempotencyKey;
import java.time.LocalDate;
import java.time.ZoneId;

@Slf4j
@Component
@RequiredArgsConstructor
public class DiaryReminderScheduler {

    private final UserRepository userRepository;
    private final NotificationService notificationService;

    /** 매일 08:00 (Asia/Seoul) — Part 4에서 실제 알림 생성 로직 채움 */
    @Scheduled(cron = "0 0 20 * * *", zone = "Asia/Seoul")
    public void sendDailyDiaryReminder() {
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));
        long yyyymmdd = today.getYear() * 10000L + today.getMonthValue() * 100L + today.getDayOfMonth();

        var userIds = userRepository.findAllIds();
        for (Long uid : userIds) {
            Notification n = new Notification();
            n.setRecipientId(uid);
            n.setType(NotificationType.DIARY_REMINDER);
            n.setEntityType(EntityType.DIARY);
            n.setMessageTitle("오늘의 일기를 작성해보세요!");
            n.setLinkUrl("/ilog");
            n.setIdempotencyKey(IdempotencyKey.instant(uid, NotificationType.DIARY_REMINDER, EntityType.DIARY, yyyymmdd));
            notificationService.create(n);
        }
        log.info("[DiaryReminderScheduler] sent {} diary reminders for {}", userIds.size(), today);
    }
}
