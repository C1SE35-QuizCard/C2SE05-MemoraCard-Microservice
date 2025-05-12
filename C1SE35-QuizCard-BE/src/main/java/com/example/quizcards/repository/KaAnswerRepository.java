package com.example.quizcards.repository;

import com.example.quizcards.entities.KaAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Repository
public interface KaAnswerRepository extends JpaRepository<KaAnswer, Long> {

    @Modifying
    @Transactional
    @Query(value = "INSERT INTO ka_answers (ka_question_id, answer_id, completion_time, is_correct, user_id) " +
            "VALUES (:questionId, :answerId,:completionTime,:isCorrect,:userId)", nativeQuery = true)
    void insertKaAnswer(Long questionId, Long answerId, BigDecimal completionTime, Boolean isCorrect, Long userId);
}
