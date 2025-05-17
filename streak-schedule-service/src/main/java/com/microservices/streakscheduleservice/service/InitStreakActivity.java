package com.microservices.scheduleservice.service;

import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;
import reactor.util.function.Tuple2;

import java.time.LocalDate;

@ActivityInterface
public interface InitStreakActivity {
    @ActivityMethod
    Tuple2<LocalDate,Long> init(long userId);
}
