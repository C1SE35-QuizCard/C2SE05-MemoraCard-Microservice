package com.microservices.streakservice.repository;

import com.microservices.streakservice.model.StreakAnalysis;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface IStreakAnalysisRepository extends R2dbcRepository<StreakAnalysis, Long> {
    @Query("SELECT * FROM streak_analysis WHERE user_id = :userId LIMIT 1")
    Mono<StreakAnalysis> findByUserId(Long userId);
}
