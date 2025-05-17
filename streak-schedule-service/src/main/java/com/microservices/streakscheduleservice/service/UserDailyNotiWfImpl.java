package com.microservices.scheduleservice.service;

import com.microservices.scheduleservice.utils.UtilsFn;
import io.temporal.activity.ActivityOptions;
import io.temporal.client.WorkflowOptions;
import io.temporal.common.RetryOptions;
import io.temporal.workflow.Workflow;
import org.springframework.stereotype.Service;
import reactor.util.function.Tuple2;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;

@Service
public class UserDailyNotiWfImpl implements UserDailyNotiWf {
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

    @Override
    public void run(long uid, int offsetSec) {
        runWithInit(uid, offsetSec, true, null, null);
    }

    @Override
    public void runWithInit(long uid, int offsetSec, boolean initDataAgain, String lastDateLearned, Long streakCount) {
        this.userId = String.valueOf(uid);
        this.offsetSec = offsetSec;

        if (lastDateLearned != null && !lastDateLearned.isBlank()) {
            this.lastStudyLocal = LocalDate.parse(lastDateLearned);
            this.streakCount = streakCount;
        }

        if (initDataAgain) {
            if (lastStudyLocal == null) {
                Tuple2<LocalDate, Long> init = initStub.init(uid);
                if (!init.getT1().isEqual(LocalDate.MIN)) {
                    lastStudyLocal = init.getT1();
                    this.streakCount = init.getT2();
                }
            }
        }

        while (true) {
            Instant fire = UtilsFn.nextXhXm(this.offsetSec, 21, 0);
            Workflow.sleep(Duration.between(Instant.now(), fire));

            LocalDate today     = fire.atOffset(ZoneOffset.ofTotalSeconds(offsetSec)).toLocalDate();
            LocalDate yesterday = today.minusDays(1);

            if (lastStudyLocal != null && lastStudyLocal.isBefore(today.minusDays(3))) {
                // Stop sending notifications
                break;
            }

            boolean send = yesterday.equals(lastStudyLocal) && !today.equals(lastStudyLocal);

            if (send) {

            }

            Workflow.sleep(Duration.ofMinutes(1));
        }
    }

    @Override
    public void markStudied(long uid, LocalDate newLocalDate, Long newStreakCount) {
        lastStudyLocal = newLocalDate;
        streakCount = newStreakCount;
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
        Long currentStreak = streakCount != null
                ? streakCount
                : 0L;

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
