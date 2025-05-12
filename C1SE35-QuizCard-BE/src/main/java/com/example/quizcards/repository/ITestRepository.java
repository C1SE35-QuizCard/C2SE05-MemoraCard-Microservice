package com.example.quizcards.repository;

import com.example.quizcards.dto.ITestDTO;
import com.example.quizcards.entities.AppUser;
import com.example.quizcards.entities.Test;
import jakarta.transaction.Transactional;
import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalTime;
import java.util.List;

public interface ITestRepository extends JpaRepository<Test, Long> {
    @Modifying
    @Transactional
    @Query(value = """
            insert into test(test_mode_id, set_id, user_id, total_question, goal_score, remaining_time, is_testing, created_at)
            values (:test_mode_id, :set_id, :user_id, :total_question, :goal_score, :remaining_time, true, now())
            """, nativeQuery = true)
    void createTest(@Param("test_mode_id") Long testModeId,
                    @Param("set_id") Long SetId,
                    @Param("user_id") Long UserId,
                    @Param("total_question") int totalQuestion,
                    @Param("goal_score") int goalScore,
                    @Param("remaining_time") LocalTime remainingTime);

    @Modifying
    @Transactional
    @Query(value = """
            delete from test
            where test_id = :test_id
            """, nativeQuery = true)
    void deleteTest(@Param("test_id") Long testId);

    @Query(value = """
            select * from test
            where test_id = :test_id
            """, nativeQuery = true)
    List<ITestDTO> getTestById(@Param("test_id") Long testId);

    @Query(value = """
            select * from test
            where test_id = :test_id
            """, nativeQuery = true)
    Test findTestId(@Param("test_id") Long testId);

    @Query(value = """
            select count(1)
            from test t
            where t.test_id = :test_id
            """, nativeQuery = true)
    Integer countTestsById(@Param("test_id") Long testId);

    List<Test> findByUser(AppUser user);
}
