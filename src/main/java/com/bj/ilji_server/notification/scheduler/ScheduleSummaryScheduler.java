package com.bj.ilji_server.notification.scheduler;

import com.bj.ilji_server.notification.packing.NotificationComposer;
import com.bj.ilji_server.schedule.dto.ScheduleBrief;
import com.bj.ilji_server.schedule.service.ScheduleReader;
import com.bj.ilji_server.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ScheduleSummaryScheduler {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    private final UserRepository userRepository;
    private final ScheduleReader scheduleReader;
    private final NotificationComposer notificationComposer;

    /** 매일 08:00 KST 일정 요약 알림 */
    @Scheduled(cron = "0 0 8 * * *", zone = "Asia/Seoul")
    public void sendDailyScheduleSummary() {
        LocalDate today = LocalDate.now(KST);
        var userIds = userRepository.findAllIds();

        int sent = 0;
        for (Long uid : userIds) {
            int total = scheduleReader.countSchedulesForDate(uid, today);

            // ★ 일정이 없으면 아예 알림을 만들지 않음 (스킵)
            if (total <= 0) {
                continue;
            }

            // 상단 N개(예: 5개)만 본문에 표시
            List<ScheduleBrief> top = scheduleReader.getSchedulesForDate(uid, today, 5);

            // Composer에서 멱등키(yyyymmdd) 포함하여 생성
            notificationComposer.scheduleDailySummary(uid, today, total, top);
            sent++;
        }
        log.info("[ScheduleSummaryScheduler] sent {} summaries for {}", sent, today);
    }
}
