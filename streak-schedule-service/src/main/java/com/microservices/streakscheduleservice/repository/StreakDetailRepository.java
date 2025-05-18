package com.microservices.streakscheduleservice.repository;

import com.microservices.streakscheduleservice.model.StreakDetails;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;

@Repository
public interface StreakDetailRepository extends R2dbcRepository<StreakDetails, Long> {
    Mono<StreakDetails> findByUserIdAndDateLearned(Long userId, java.time.LocalDate dateLearned);

    Mono<java.lang.Boolean>
    existsByUserIdAndDateLearned(Long userId, java.time.LocalDate dateLearned);

    Flux<StreakDetails>
    findByUserIdAndDateLearnedBetween(Long userId,
                                      java.time.LocalDate start,
                                      java.time.LocalDate end);


    @Query("""
            SELECT date_learned
            FROM streak_details
            WHERE user_id = :uid
            ORDER BY date_learned DESC
            LIMIT 1
        """)
    Mono<LocalDate> lastStudyDate(Long uid);
}