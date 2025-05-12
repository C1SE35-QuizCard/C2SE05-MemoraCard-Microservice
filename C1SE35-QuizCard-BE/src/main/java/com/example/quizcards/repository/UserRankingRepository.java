package com.example.quizcards.repository;

import com.example.quizcards.entities.UserRanking;
import com.example.quizcards.entities.UserRankingId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Repository
public interface UserRankingRepository extends JpaRepository<UserRanking, UserRankingId> {

    @Modifying
    @Transactional
    @Query(value = """
        INSERT INTO user_rankings (room_id, question_id, user_id, is_correct, completion_time, question_ranking, total_correct, total_time)
        SELECT 
            kq.room_id,
            kq.id AS question_id,
            kans.user_id,
            kans.is_correct,
            kans.completion_time,
            RANK() OVER (
                PARTITION BY kq.id 
                ORDER BY kans.is_correct DESC, kans.completion_time ASC
            ) AS question_ranking,
            CASE WHEN kans.is_correct = 1 THEN 1 ELSE 0 END AS total_correct,
            kans.completion_time AS total_time
        FROM 
            ka_questions kq
        JOIN ka_answers kans ON kans.ka_question_id = kq.id
        WHERE 
            kq.room_id = :roomId
        ON DUPLICATE KEY UPDATE 
            question_ranking = VALUES(question_ranking),
            total_correct = total_correct + VALUES(total_correct),
            total_time = total_time + VALUES(total_time);
    """, nativeQuery = true)
    void insertData(@Param("roomId") Long roomId);

    @Query(value = """
        SELECT\s
            au.user_id,
            au.user_name,
            au.avatar,
            ur.room_id,
            COUNT(DISTINCT ur.question_id) AS total_questions,\s
            SUM(CASE WHEN ur.is_correct = 1 THEN 1 ELSE 0 END) AS total_correct,\s
            SUM(ur.completion_time) AS total_time,
            RANK() OVER (
                PARTITION BY ur.room_id\s
                ORDER BY\s
                    SUM(CASE WHEN ur.is_correct = 1 THEN 1 ELSE 0 END) DESC,\s
                    SUM(ur.completion_time) ASC\s
            ) AS total_ranking
        FROM\s
            user_rankings ur
        JOIN
        	ka_players kp on ur.user_id = kp.id
        JOIN
        	app_users au on kp.user_id = au.user_id
        WHERE\s
            ur.room_id = :roomId
        GROUP BY\s
            ur.user_id, ur.room_id
        ORDER BY\s
            total_ranking ASC;
    """, nativeQuery = true)
    List<Map<String, Object>> getRanking(@Param("roomId")Long roomId);
}
