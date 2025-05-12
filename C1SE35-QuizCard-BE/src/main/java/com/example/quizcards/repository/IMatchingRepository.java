package com.example.quizcards.repository;

import com.example.quizcards.entities.Matching;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface IMatchingRepository extends JpaRepository<Matching, Long> {

    @Query(value = "SELECT m.matching_id, m.card_id, m.round_number, " +
            "f.question, f.answer " +
            "FROM matchings m " +
            "JOIN flashcards f ON m.card_id = f.card_id",
            nativeQuery = true)
    List<Matching> findAllMatchingsWithBasicInfo();

    List<Matching> findByUser_UserId(Long userId);

    @Modifying
    @Transactional
    void deleteByUser_UserId(Long userId);

    @Transactional
    @Modifying
    @Query("UPDATE Matching m SET m.isCorrect = true WHERE m.matchingId = :matchingId")
    void updateIsCorrectByMatchingId(Long matchingId);
}