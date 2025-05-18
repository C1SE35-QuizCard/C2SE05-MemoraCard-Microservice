package com.microservices.streakscheduleservice.service;

import io.temporal.workflow.SignalMethod;
import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

import java.time.LocalDate;

@WorkflowInterface
public interface UserDailyNotiWf {
//    @WorkflowMethod
//    void run(long uid, int offsetSec);

    @WorkflowMethod
    void runWithInit(long uid,
                     int offsetSec,
                     boolean initDataAgain,
                     String lastDateLearned,   // truyền vào đây
                     Long streakCount);

    @SignalMethod
    void markStudied(String uid, LocalDate newLocalDate, Long newStreakCount);

    @SignalMethod
    void updateTz(String newTz);
}
