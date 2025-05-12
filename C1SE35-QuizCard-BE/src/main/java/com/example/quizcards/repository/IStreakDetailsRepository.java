package com.example.quizcards.repository;

import com.example.quizcards.dto.StreakStatus;
import com.example.quizcards.entities.AppUser;
import com.example.quizcards.entities.StreakDetails;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface IStreakDetailsRepository extends JpaRepository<StreakDetails, Long> {
    Optional<StreakDetails> findByUserAndDateLearned(AppUser user, LocalDate dateLearned);

    boolean existsByUserAndDateLearned(AppUser user, LocalDate dateLearned);

    List<StreakDetails> findByUserAndDateLearnedBetween(AppUser user, LocalDate startDate, LocalDate endDate);

    @Query(value = """
                select new com.example.quizcards.dto.StreakStatus(sd.dateLearned)
                from StreakDetails sd
                where sd.user.userId = :userId
                and function('MONTH', sd.dateLearned) = :month
                and function('YEAR', sd.dateLearned) = :year
            """)
    List<StreakStatus> getLearnedByMonthAndYear(@Param("userId") Long userId,
                                                @Param("month") int month,
                                                @Param("year") int year);

    @Query(value = """
                select new com.example.quizcards.dto.StreakStatus(sd.dateLearned)
                from StreakDetails sd
                where sd.user.userId = :userId
            """)
    List<StreakStatus> findAllStreakStatusByUserId(@Param("userId") Long userId);

    @Query(value = """
                select new com.example.quizcards.dto.StreakStatus(sd.dateLearned)
                from StreakDetails sd
                where sd.user.userId = :userId
                and sd.dateLearned between :startDate and :endDate
            """,
            countQuery = """
                select count(sd.id)
                from StreakDetails sd
                where sd.user.userId = :userId
                and sd.dateLearned between :startDate and :endDate
            """)
    Page<StreakStatus> getLearnedByDateRange(@Param("userId") Long userId,
                                             @Param("startDate") LocalDate startDate,
                                             @Param("endDate") LocalDate endDate,
                                             Pageable pageable);
}
