package com.microservices.streakscheduleservice.repository;

import com.microservices.streakscheduleservice.dto.UserWithStreakAnalysisDTO;
import com.microservices.streakscheduleservice.model.AppUser;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;

@Repository
public interface AppUserRepository extends R2dbcRepository<AppUser, Long> {
    Mono<AppUser> findByUserId(Long userId);

    Mono<AppUser> findByEmail(String email);

    Mono<AppUser> findByUsername(String username);

    @Query(value = """
                select  au.user_id as userId,
                        au.user_name as username,
                        au.user_tz as userTz,
                        sa.current_streak as currentStreak,
                        sa.longest_streak as maxStreak,
                        sa.last_updated as lastUpdated
                from app_users au
                join streak_analysis sa on au.user_id = sa.user_id
                where au.user_id in (:ids)
    """)
    Flux<UserWithStreakAnalysisDTO> findWithStreakAnalysisByIdsIn(Collection<Long> ids);

    @Query(value = """
                select  au.user_id as userId,
                        au.user_name as username,
                        au.user_tz as userTz,
                        sa.id as analysisId,
                        sa.current_streak as currentStreak,
                        sa.longest_streak as maxStreak,
                        sa.last_updated as lastUpdated
                from app_users au
                left join streak_analysis sa on au.user_id = sa.user_id
                where au.user_id = :userId
                limit 1
    """)
    Mono<UserWithStreakAnalysisDTO> findWithStreakAnalysisByUserId(Long userId);
}
