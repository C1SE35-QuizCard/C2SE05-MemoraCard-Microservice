package com.example.quizcards.helpers.SchedulerExecutorHelpers;

import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@Component
public class ScheduleHelpersImpl implements IScheduleHelpers {
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(10);

    @Override
    public ScheduledFuture<?> addScheduleTasks(Runnable runnable, LocalDateTime endTime) {
        long delay = Duration.between(LocalDateTime.now(), endTime).toMillis();
        return scheduler.schedule(runnable, delay, TimeUnit.MILLISECONDS);
    }
}
