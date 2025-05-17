package com.microservices.scheduleservice.service;

import com.microservices.scheduleservice.dto.UserWithStreakAnalysisDTO;
import com.microservices.scheduleservice.model.AppUser;
import io.temporal.activity.ActivityInterface;
import reactor.core.publisher.Flux;

import java.util.List;

@ActivityInterface
public interface UserService {
    List<AppUser> byIds(List<Long> ids);

    Flux<UserWithStreakAnalysisDTO> getUsersWithStreakAnalysis(List<Long> userIds);
}
