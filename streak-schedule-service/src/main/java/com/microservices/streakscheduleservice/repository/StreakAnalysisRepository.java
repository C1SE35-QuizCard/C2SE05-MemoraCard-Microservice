package com.microservices.scheduleservice.repository;

import com.microservices.scheduleservice.model.StreakAnalysis;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface StreakAnalysisRepository extends R2dbcRepository<StreakAnalysis, Long> {
    Mono<StreakAnalysis> findByUserId(Long userId);
}