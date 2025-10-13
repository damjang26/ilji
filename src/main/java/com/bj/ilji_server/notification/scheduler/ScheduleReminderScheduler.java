package com.bj.ilji_server.notification.scheduler;

import com.bj.ilji_server.notification.packing.NotificationComposer;
import com.bj.ilji_server.schedule.entity.Schedule;
import com.bj.ilji_server.schedule.repository.ScheduleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ScheduleReminderScheduler {

    private final ScheduleRepository scheduleRepository;
    private final NotificationComposer notificationComposer;

    @Scheduled(cron = "0 * * * * *") // 1분마다 실행
    @Transactional
    public void sendScheduleReminders() {
        // 1. 알림이 설정됐지만 아직 발송되지 않은 모든 일정을 가져옵니다.
        List<Schedule> schedulesToCheck = scheduleRepository.findSchedulesToRemind();

        LocalDateTime now = LocalDateTime.now();

        for (Schedule schedule : schedulesToCheck) {
            // 2. 각 일정의 실제 알림 시간을 계산합니다.
            LocalDateTime reminderTime = schedule.getStartTime().minusMinutes(schedule.getReminderMinutesBefore());

            // 3. 계산된 알림 시간이 현재 시간(분 단위)과 일치하는지 확인합니다.
            // (예: 10:30:00 ~ 10:30:59 사이에 속하는지)
            if (reminderTime.getYear() == now.getYear() &&
                reminderTime.getMonth() == now.getMonth() &&
                reminderTime.getDayOfMonth() == now.getDayOfMonth() &&
                reminderTime.getHour() == now.getHour() &&
                reminderTime.getMinute() == now.getMinute()) {

                try {
                    // 4. 조건이 맞으면 알림 발송 로직을 실행합니다.
                    log.info("Sending reminder for schedule: {}", schedule.getId());
                    notificationComposer.scheduleItemReminder(schedule);
                    schedule.setReminderSent(true);
                    // save는 @Transactional에 의해 트랜잭션 종료 시 자동으로 처리됩니다.
                } catch (Exception e) {
                    log.error("Error sending reminder for schedule: " + schedule.getId(), e);
                }
            }
        }
    }
}
