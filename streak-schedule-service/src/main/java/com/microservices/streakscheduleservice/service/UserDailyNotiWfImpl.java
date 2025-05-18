package com.microservices.streakscheduleservice.service;

import com.microservices.dto.notification.StreakNotificationData;
import com.microservices.streakscheduleservice.dto.InitStreakResultDTO;
import com.microservices.streakscheduleservice.utils.UtilsFn;
import io.temporal.activity.ActivityOptions;
import io.temporal.common.RetryOptions;
import io.temporal.workflow.Workflow;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;


public class UserDailyNotiWfImpl implements UserDailyNotiWf {
    // init logger
    private static ActivityOptions actOps() {
        return ActivityOptions.newBuilder()
                .setStartToCloseTimeout(Duration.ofSeconds(60))
                .setRetryOptions(RetryOptions.newBuilder()
                        .setMaximumAttempts(3)
                        .setInitialInterval(Duration.ofSeconds(5))
                        .setBackoffCoefficient(2.0)
                        .build()
                )
                .build();
    }

    private final ISendNotification sender =
            Workflow.newActivityStub(
                    ISendNotification.class,
                    actOps()
            );

    private final InitStreakActivity initStub =
            Workflow.newActivityStub(
                    InitStreakActivity.class,
                    actOps()
            );

    private LocalDate lastStudyLocal;
    private Long streakCount;
    private String userId;
    private int offsetSec;

//    @Override
//    public void run(long uid, int offsetSec) {
//        runWithInit(uid, offsetSec, true, null, null);
//    }

    @Override
    public void runWithInit(long uid, int offsetSec, boolean initDataAgain, String lastDateLearned, Long streakCount) {
        this.userId = String.valueOf(uid);
        this.offsetSec = offsetSec;

        if (lastDateLearned != null && !lastDateLearned.isBlank()) {
            this.lastStudyLocal = LocalDate.parse(lastDateLearned);
            this.streakCount = streakCount;
        }

        if (initDataAgain) {
            if (this.lastStudyLocal == null) {
                System.out.println("Begin fetch data...");
                InitStreakResultDTO init = initStub.init(uid);
                if (!init.getDateLearned().isEqual(LocalDate.MIN)) {
                    this.lastStudyLocal = init.getDateLearned();
                    this.streakCount = init.getCurrentStreak();
                }
                System.out.println("End fetch data...");
            }
        }

        while (true) {
            System.out.println("Beginning loop...");
            Instant now = Instant.ofEpochMilli(Workflow.currentTimeMillis());
            Instant fire = UtilsFn.nextXhXm(now, this.offsetSec, 21, 0);
            Workflow.sleep(Duration.between(now, fire));
//            Instant tempNow = now.plusSeconds(65);
//            // get hours and minutes
//            int hours = tempNow.atOffset(ZoneOffset.ofTotalSeconds(this.offsetSec)).getHour();
//            int minutes = tempNow.atOffset(ZoneOffset.ofTotalSeconds(this.offsetSec)).getMinute();
//
//            Instant fire = UtilsFn.nextXhXm(now, this.offsetSec, hours, minutes);
//            Workflow.sleep(Duration.between(now, tempNow));

            Instant fired = Instant.ofEpochMilli(Workflow.currentTimeMillis());
            LocalDate today = fired.atOffset(ZoneOffset.ofTotalSeconds(offsetSec)).toLocalDate();
            LocalDate yesterday = today.minusDays(1);

            if (this.lastStudyLocal != null && this.lastStudyLocal.isBefore(today.minusDays(3))) {
                // Stop sending notifications
                break;
            }

            boolean send = yesterday.equals(this.lastStudyLocal) && !today.equals(this.lastStudyLocal);

            if (send) {
                StreakNotificationData sendNoti = UtilsFn.buildNotiMessage(
                        userId,
                        this.streakCount == null ? 0L : this.streakCount,
                        this.lastStudyLocal
                );
                System.out.println("Sent notification data");
                sender.sendStreakNotification(sendNoti);
            }
            StreakNotificationData sendNoti = UtilsFn.buildNotiMessage(
                    userId,
                    this.streakCount == null ? 0L : this.streakCount,
                    this.lastStudyLocal == null ? LocalDate.now() : this.lastStudyLocal
            );
            System.out.println("Sent notification data");
            System.out.println("Last study date: " + this.lastStudyLocal);
            System.out.println("Streak count: " + this.streakCount);
//            sender.sendStreakNotification(sendNoti);
            System.out.println("Done task, next beginning loop...");
        }
    }

    @Override
    public void markStudied(String uid, LocalDate newLocalDate, Long newStreakCount) {
        this.lastStudyLocal = newLocalDate;
        this.streakCount = newStreakCount;
    }

    @Override
    public void updateTz(String newTz) {
        if (newTz == null || newTz.isBlank()) return;
        Integer newOffset = UtilsFn.getOffsetInSecondsByUserTz(newTz);
        if (newOffset == null || newOffset == offsetSec) return;

        // Cập nhật offset hiện tại
        this.offsetSec = newOffset;

        // Chuyển LocalDate → String (ví dụ "2025-05-16"), và streakCount (Long)
        String lastDateStr = lastStudyLocal != null
                ? lastStudyLocal.toString()
                : null;
        Long currentStreak = streakCount;

        UserDailyNotiWf self = Workflow.newContinueAsNewStub(UserDailyNotiWf.class);

        // GỌI phương thức runWithInit với đủ thông tin cũ + offset mới
        self.runWithInit(
                Long.parseLong(userId),  // uid
                newOffset,               // offsetSec
                false,                   // initDataAgain = false (đã có dữ liệu)
                lastDateStr,             // lastDateLearned
                currentStreak            // streakCount
        );
    }
}
