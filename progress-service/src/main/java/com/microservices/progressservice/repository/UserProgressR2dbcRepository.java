package com.microservices.progressservice.repository;

import com.microservices.progressservice.dto.response.IFlashcardProgressDTO;
import com.microservices.progressservice.dto.response.IProgressAnalysisDTO;
import com.microservices.progressservice.dto.response.IProgressDTO;
import com.microservices.progressservice.model.UserProgress;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Set;

@Repository
public interface UserProgressR2dbcRepository
        extends R2dbcRepository<UserProgress, Long> {
    // ==== Flashcards progress by set ====
    // Flashcards progress by set (no version)
    @Deprecated
    @Query("""
        SELECT
           f.set_id,
           s.title,
           a.avatar,
           a.user_name,
           f.card_id,
           f.question,
           f.answer,
           up.progress_type,
           up.marked_for_attention,
           f.image_url,
            f.video_url
       FROM flashcards f
       JOIN set_flashcards s ON s.set_id = f.set_id
       JOIN app_users a     ON a.user_id = s.user_id
       LEFT JOIN user_progress up
         ON f.card_id = up.card_id
        AND up.user_id = :userId
       WHERE f.set_id = :setId
    """)
    Flux<IFlashcardProgressDTO> findFlashcardsProgressBySetId(
            @Param("setId") Long setId,
            @Param("userId") Long userId
    );

    // Flashcards progress by set with version
    @Query("""
         SELECT
               f.set_id,
               s.title,
               a.avatar,
               a.user_name,
               f.card_id,
               f.question,
               f.answer,
               up.progress_type,
               up.marked_for_attention,
               f.image_url,
                f.video_url
           FROM flashcards f
           JOIN set_flashcards s ON s.set_id = f.set_id
           JOIN app_users a     ON a.user_id = s.user_id
           LEFT JOIN user_progress up
             ON f.card_id      = up.card_id
            AND up.user_id     = :userId
            AND up.mode_version = :modeVersion
           WHERE f.set_id = :setId
    """)
    Flux<IFlashcardProgressDTO> findFlashcardsProgressBySetId(
            @Param("setId") Long setId,
            @Param("userId") Long userId,
            @Param("modeVersion") long modeVersion
    );

    // Flashcards progress list by cardIds with limit
    @Query("""
         SELECT
               f.set_id,
               s.title,
               f.card_id,
               f.question,
               f.answer,
               f.image_url,
                f.video_url,
               up.progress_type,
               up.marked_for_attention
           FROM flashcards f
           JOIN set_flashcards s ON s.set_id = f.set_id
           LEFT JOIN user_progress up
             ON f.card_id = up.card_id
            AND up.user_id = :userId
           WHERE f.set_id = :setId
             AND f.card_id IN (:cardIds)
           LIMIT :limit
    """)
    Flux<IFlashcardProgressDTO> findProgCardsByCardIdsIn(
            @Param("userId") Long userId,
            @Param("setId") Long setId,
            @Param("cardIds") List<Long> cardIds,
            @Param("limit") long limit
    );


    // ==== Progress analysis (no version) ====
    @Deprecated
    @Query("""
        SELECT
          s.set_id,
          s.title,
          COUNT(DISTINCT CASE WHEN up.progress_type = FALSE THEN up.progress_id END) AS total_card_recall,
          COUNT(DISTINCT CASE WHEN up.progress_type = TRUE  THEN up.progress_id END)  AS total_card_remember,
          (COUNT(DISTINCT f.card_id)
           - COUNT(DISTINCT CASE WHEN up.progress_type = TRUE  THEN up.progress_id END)
           - COUNT(DISTINCT CASE WHEN up.progress_type = FALSE THEN up.progress_id END)
          )                                                             AS total_card_not_learn
        FROM set_flashcards s
        LEFT JOIN flashcards f  ON s.set_id = f.set_id
        LEFT JOIN user_progress up
          ON f.card_id = up.card_id
         AND up.user_id = :userId
        WHERE s.set_id = :setId
        GROUP BY s.set_id, s.title
    """)
    Mono<IProgressAnalysisDTO> findAnalysisProgressBySetId(
            @Param("setId") Long setId,
            @Param("userId") Long userId
    );

    // ==== Progress analysis (with version) ====
    @Query("""
        SELECT
          s.set_id,
          s.title,
          SUM(CASE WHEN p.progress_type = 0 THEN 1 ELSE 0 END)                  AS total_card_recall,
          SUM(CASE WHEN p.progress_type = 1 THEN 1 ELSE 0 END)                  AS total_card_remember,
          (COUNT(f.card_id) - COUNT(p.progress_id))                             AS total_card_not_learn
        FROM set_flashcards s
        JOIN flashcards f  ON f.set_id = s.set_id
        LEFT JOIN user_progress p
          ON p.card_id      = f.card_id
         AND p.user_id      = :userId
         AND p.mode_version = :modeVersion
        WHERE s.set_id = :setId
        GROUP BY s.set_id, s.title
    """)
    Mono<IProgressAnalysisDTO> findAnalysisProgressBySetId(
            @Param("setId") Long setId,
            @Param("userId") Long userId,
            @Param("modeVersion") long modeVersion
    );


    // ==== User progress rows ====
    @Query("""
        SELECT
          u.progress_id,
          u.progress_type,
          u.marked_for_attention,
          u.user_id,
          u.card_id,
          u.consecutive_correct_simple_mode,
          u.count_consecutive_hard_press,
          u.mode_version,
          u.updated_at
        FROM user_progress u
        JOIN flashcards f ON u.card_id = f.card_id
        WHERE u.user_id = :userId
          AND f.set_id  = :setId
    """)
    Flux<IProgressDTO> findUserProgressBySetIdAndUserId(
            @Param("setId") Long setId,
            @Param("userId") Long userId
    );

    @Query("""
        SELECT
          u.progress_id                            AS progressId,
          u.progress_type                          AS progressType,
          u.marked_for_attention                   AS isAttention,
          u.user_id                                AS userId,
          u.card_id                                AS cardId,
          u.consecutive_correct_simple_mode        AS consecutiveCorrectSimpleMode,
          u.count_consecutive_hard_press           AS countConsecutiveHardPress,
          u.mode_version                           AS progressVersion
        FROM user_progress u
        JOIN flashcards f ON u.card_id = f.card_id
        WHERE u.user_id      = :userId
          AND f.set_id       = :setId
          AND u.mode_version = :modeVersion
    """)
    Flux<IProgressDTO> findUserProgressBySetIdAndUserIdAndModeVersion(
            @Param("setId") Long setId,
            @Param("userId") Long userId,
            @Param("modeVersion") long modeVersion
    );

    @Query("""
         SELECT
              u.progress_id,
              u.progress_type,
              u.marked_for_attention,
              u.user_id,
              u.card_id,
              u.consecutive_correct_simple_mode,
              u.count_consecutive_hard_press,
              u.mode_version,
              u.updated_at
            FROM user_progress u
            WHERE u.progress_id = :progressId
    """)
    Mono<IProgressDTO> findUserProgressById(@Param("progressId") Long progressId);


    // ==== Derived reactive methods ====
    Mono<Boolean> existsByUserIdAndCardId(Long userId, Long cardId);
    Mono<Boolean> existsByUserIdAndCardIdAndProgressIdNot(Long userId, Long cardId, Long progressId);

    Mono<UserProgress> findByUserIdAndCardId(Long userId, Long cardId);
    Mono<UserProgress> findByUserIdAndCardIdAndModeVersion(
            Long userId, Long cardId, long modeVersion
    );

    @Query("""
         SELECT
                  u.progress_id,
                  u.progress_type,
                  u.marked_for_attention,
                  u.user_id,
                  u.card_id,
                  u.consecutive_correct_simple_mode,
                  u.count_consecutive_hard_press,
                  u.mode_version,
                  u.updated_at
                FROM user_progress u
                JOIN flashcards f ON u.card_id = f.card_id
                WHERE u.user_id     = :userId
                  AND f.set_id      = :setId
                  AND f.card_id IN (:cardIds)
    """)
    Flux<IProgressDTO> getProgressByUserIdSetIdCardIds(
            @Param("userId") Long userId,
            @Param("setId") Long setId,
            @Param("cardIds") Set<Long> cardIds
    );

    // Delete by set
    @Query("""
        DELETE FROM user_progress u
        USING flashcards f
        WHERE u.card_id = f.card_id
          AND u.user_id = :userId
          AND f.set_id  = :setId
    """)
    Mono<Void> deleteUserProgressByUserAndSetId(
            @Param("userId") Long userId,
            @Param("setId") Long setId
    );

    // Create new progress
    @Query("""
        INSERT INTO user_progress(progress_type, marked_for_attention, user_id, card_id)
        VALUES (:progressType, :isAttention, :userId, :cardId)
    """)
    Mono<Void> createUserProgress(
            @Param("progressType") Boolean progressType,
            @Param("isAttention") Boolean isAttention,
            @Param("userId") Long userId,
            @Param("cardId") Long cardId
    );

    // Update existing progress
    @Query("""
        UPDATE user_progress u
        SET
          u.progress_type         = COALESCE(:progressType, u.progress_type),
          u.marked_for_attention  = COALESCE(:isAttention, u.marked_for_attention),
          u.user_id               = COALESCE(:userId, u.user_id),
          u.card_id               = COALESCE(:cardId, u.card_id)
        WHERE u.progress_id = :progressId
    """)
    Mono<Void> updateUserProgress(
            @Param("progressId") Long progressId,
            @Param("progressType") Boolean progressType,
            @Param("isAttention") Boolean isAttention,
            @Param("userId") Long userId,
            @Param("cardId") Long cardId
    );
}
