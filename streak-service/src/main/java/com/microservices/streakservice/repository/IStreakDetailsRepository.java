package com.microservices.streakservice.repository;

import com.microservices.streakservice.dto.StreakStatus;
import com.microservices.streakservice.model.StreakDetails;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;

@Repository
public interface IStreakDetailsRepository extends R2dbcRepository<StreakDetails, Long> {
    Mono<StreakDetails> findByUserIdAndDateLearned(Long userId, LocalDate dateLearned);

    Mono<Boolean> existsByUserIdAndDateLearned(Long userId, LocalDate dateLearned);

    Flux<StreakDetails> findByUserIdAndDateLearnedBetween(Long userId, LocalDate startDate, LocalDate endDate);

    @Query("""
            SELECT
              date_learned,
              -999 as day_rank 
            FROM streak_details
            WHERE user_id = :userId
              AND MONTH(date_learned) = :month
              AND YEAR(date_learned) = :year
            """)
    Flux<StreakStatus> getLearnedByMonthAndYear(Long userId, int month, int year);

    @Query("""
            SELECT
              date_learned,
              -999 as day_rank
            FROM streak_details
            WHERE user_id = :userId
            """)
    Flux<StreakStatus> findAllStreakStatusByUserId(Long userId);

    /**
     * Lấy page manually: dùng LIMIT/OFFSET cho MySQL
     */
    @Query("""
            SELECT
              date_learned,
              -999 as day_rank
            FROM streak_details
            WHERE user_id = :userId
              AND date_learned BETWEEN :startDate AND :endDate
            ORDER BY date_learned ASC
            LIMIT :#{#pageable.pageSize} OFFSET :#{#pageable.offset}
            """)
    Flux<StreakStatus> getLearnedByDateRange(
            Long userId,
            LocalDate startDate,
            LocalDate endDate,
            Pageable pageable
    );

    @Query("""
            SELECT 
              COUNT(*) 
            FROM streak_details
            WHERE user_id = :userId
              AND date_learned BETWEEN :startDate AND :endDate
            """)
    Mono<Long> countByUserIdAndDateLearnedBetween(
            Long userId,
            LocalDate startDate,
            LocalDate endDate
    );
}
