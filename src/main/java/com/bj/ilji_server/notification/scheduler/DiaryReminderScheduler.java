package com.bj.ilji_server.notification.scheduler;

import com.bj.ilji_server.ilog.repository.ILogRepository;
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
import java.time.format.DateTimeFormatter; // Added import
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Component
@RequiredArgsConstructor
public class DiaryReminderScheduler {

    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final ILogRepository iLogRepository; // ILogRepository 주입

    /** 매일 20:00 (Asia/Seoul) — 오늘 일기를 쓰지 않은 유저에게만 리마인더 발송 */
    @Scheduled(cron = "0 0 20 * * *", zone = "Asia/Seoul")
    public void sendDailyDiaryReminder() {
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));
        long yyyymmdd = today.getYear() * 10000L + today.getMonthValue() * 100L + today.getDayOfMonth();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd"); // Added line
        String formattedDate = today.format(formatter); // Added line

        var userIds = userRepository.findAllIds();
        AtomicInteger sentCount = new AtomicInteger(0); // 전송 카운트

        for (Long uid : userIds) {
            // 오늘 날짜로 작성된 일기가 있는지 확인
            boolean hasWrittenToday = iLogRepository.findByUserProfileUserIdAndLogDate(uid, today).isPresent();

            // 일기를 아직 작성하지 않은 경우에만 알림 생성
            if (!hasWrittenToday) {
                Notification n = new Notification();
                n.setRecipientId(uid);
                n.setType(NotificationType.DIARY_REMINDER);
                n.setEntityType(EntityType.DIARY);
                n.setMessageTitle("(" + formattedDate + ") Time to write your daily log!"); // Modified line
                // 링크는 프론트에서 처리하므로 제거
                n.setIdempotencyKey(IdempotencyKey.instant(uid, NotificationType.DIARY_REMINDER, EntityType.DIARY, yyyymmdd));
                notificationService.create(n);
                sentCount.getAndIncrement();
            }
        }
        log.info("[DiaryReminderScheduler] Sent {} diary reminders for {} (Total users: {})", sentCount.get(), today, userIds.size());
    }
}
