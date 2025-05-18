package com.microservices.streakscheduleservice.repository;

import com.microservices.streakscheduleservice.model.StreakAnalysis;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface StreakAnalysisRepository extends R2dbcRepository<StreakAnalysis, Long> {
    Mono<StreakAnalysis> findByUserId(Long userId);
}