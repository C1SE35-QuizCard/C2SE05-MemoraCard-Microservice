package com.example.quizcards.helpers.SchedulerExecutorHelpers;

import java.time.LocalDateTime;
import java.util.concurrent.ScheduledFuture;

public interface IScheduleHelpers {
    ScheduledFuture<?> addScheduleTasks(Runnable runnable, LocalDateTime endTime);
}
