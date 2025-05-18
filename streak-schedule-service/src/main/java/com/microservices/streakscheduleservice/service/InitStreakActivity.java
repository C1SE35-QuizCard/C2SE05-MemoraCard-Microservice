package com.microservices.streakscheduleservice.service;

import com.microservices.streakscheduleservice.dto.InitStreakResultDTO;
import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;
import reactor.util.function.Tuple2;

import java.time.LocalDate;

@ActivityInterface
public interface InitStreakActivity {
    @ActivityMethod
    InitStreakResultDTO init(long userId);
}
