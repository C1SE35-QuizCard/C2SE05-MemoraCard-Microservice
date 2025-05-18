package com.microservices.streakscheduleservice.service;


import com.microservices.streakscheduleservice.dto.UserWithStreakAnalysisDTO;
import com.microservices.streakscheduleservice.model.AppUser;
import com.microservices.streakscheduleservice.repository.AppUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final AppUserRepository repo;

    @Override
    public List<AppUser> byIds(List<Long> ids) {
        return repo.findAllById(ids).toStream().toList();
    }

    @Override
    public Flux<UserWithStreakAnalysisDTO> getUsersWithStreakAnalysis(List<Long> userIds) {
        return repo.findWithStreakAnalysisByIdsIn(userIds);
    }
}
