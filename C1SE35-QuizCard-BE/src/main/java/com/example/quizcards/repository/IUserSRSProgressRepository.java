package com.example.quizcards.repository;

import com.example.quizcards.dto.IUserSRSProgressDTO;
import com.example.quizcards.entities.UserSRSProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface IUserSRSProgressRepository extends JpaRepository<UserSRSProgress, Long> {
    @Query(value = """
                            SELECT COUNT(p.progress_id)
                            FROM progress_with_srs p
                            JOIN flashcards f ON p.card_id = f.card_id
                            WHERE p.user_id     = :userId
                              AND f.set_id      = :setId
                              AND p.srs_version = :srsVersion
                              AND p.card_state IN (:cardStates)
            """, nativeQuery = true)
    Long countSrsProgressesWhereStatesIn(
            @Param("userId") Long userId,
            @Param("setId") Long setId,
            @Param("srsVersion") Long srsVersion,
            @Param("cardStates") List<String> cardStates
    );

        @Query(value = """
                                SELECT
                                    p.progress_id                      AS id,
                                    p.card_id                          AS cardId,
                                    p.user_id                          AS userId,
                                    p.card_state                       AS cardState,
                                    p.current_interval                 AS currentInterval,
                                    p.ease_factor                      AS easeFactor,
                                    p.lapses                           AS lapses,
                                    p.learning_step                    AS learningStep,                                    
                                    CONVERT_TZ(p.last_review_time, '+00:00', :jvmZoneIdParam) AS lastReviewTime,
                                    CONVERT_TZ(p.due_date, '+00:00', :jvmZoneIdParam) AS dueDate,
                                    p.consecutive_correct              AS consecutiveCorrect,
                                    p.consecutive_incorrect            AS consecutiveIncorrect,
                                    p.old_next_interval_again          AS oldNextIntervalAgain,
                                    p.old_next_interval_hard           AS oldNextIntervalHard,
                                    p.old_next_interval_good           AS oldNextIntervalGood,
                                    p.old_next_interval_easy           AS oldNextIntervalEasy,
                                    p.srs_version                      AS srsVersion,
                                    CONVERT_TZ(p.created_at, '+00:00', :jvmZoneIdParam) AS createdAt,
                                    CONVERT_TZ(p.updated_at, '+00:00', :jvmZoneIdParam) AS updatedAt
                                FROM progress_with_srs p
                                JOIN flashcards f ON p.card_id = f.card_id
                                WHERE p.user_id     = :userId
                                  AND f.set_id      = :setId
                                  AND p.srs_version = :srsVersion
                                  AND p.card_state IN (:cardStates)
                                  AND p.due_date   <= (:referenceTime)
                                ORDER BY p.learning_step ASC, p.due_date ASC
                                LIMIT :limit
                """, nativeQuery = true)
    List<IUserSRSProgressDTO> getListCardProgressesDueDateAndStatesIn(
            @Param("userId") Long userId,
            @Param("setId") Long setId,
            @Param("srsVersion") Long srsVersion,
            @Param("cardStates") List<String> cardStates,
            @Param("referenceTime") Instant referenceTime,
            @Param("limit") Long limit,
            @Param("jvmZoneIdParam") String jvmZoneIdParam
    );

    @Query(value = """
            SELECT count(p.progress_id)
            FROM progress_with_srs p
            JOIN flashcards f ON p.card_id = f.card_id
            WHERE p.user_id      = :userId
              AND f.set_id       = :setId
              AND p.srs_version  = :srsVersion
              AND p.due_date    <= :referenceTime
            """, nativeQuery = true)
    Long countCardsDueDateBeforeTime(
            @Param("userId") Long userId,
            @Param("setId") Long setId,
            @Param("srsVersion") Long srsVersion,
            @Param("referenceTime") Instant referenceTime
    );

    @Query(value = """
            SELECT COUNT(p.progress_id)
            FROM progress_with_srs p
            JOIN flashcards f 
              ON p.card_id = f.card_id
            WHERE p.user_id        = :userId
              AND f.set_id         = :setId
              AND p.srs_version    = :srsVersion
              /*AND p.card_state     = 'New'*/
              AND p.created_at BETWEEN :startTime AND :endTime
            """, nativeQuery = true)
    Long countNewCardsLearnedBetweenTimes(
            @Param("userId") Long userId,
            @Param("setId") Long setId,
            @Param("srsVersion") Long srsVersion,
            @Param("startTime") Instant startTime,
            @Param("endTime") Instant endTime
    );

    @Query(value = """
                SELECT
                    f.card_id                          AS cardId,
                    :userId                            AS userId
                FROM flashcards f
                LEFT JOIN progress_with_srs p on f.card_id = p.card_id and p.user_id = :userId
                WHERE 
                    f.set_id = :setId AND (
                        p.progress_id is null or p.srs_version <> :srsVersion
                    )
                LIMIT :limit
            """, nativeQuery = true)
    List<IUserSRSProgressDTO> getNewCardsByUserIdSetId(
            @Param("userId") Long userId,
            @Param("setId") Long setId,
            @Param("srsVersion") Long srsVersion,
            @Param("limit") Long limit
    );

    @Query(value = """
                SELECT COUNT(f.card_id)
                FROM flashcards f
                LEFT JOIN progress_with_srs p on f.card_id = p.card_id and p.user_id = :userId
                WHERE 
                    f.set_id = :setId AND (
                        p.progress_id is null or p.srs_version <> :srsVersion
                    )
            """, nativeQuery = true)
    Long countNewCardsByUserIdSetId(
            @Param("userId") Long userId,
            @Param("setId") Long setId,
            @Param("srsVersion") Long srsVersion
    );

    @Query(value = """
                SELECT
                    p.progress_id                      AS id,
                    p.card_id                          AS cardId,
                    p.user_id                          AS userId,
                    p.card_state                       AS cardState,
                    p.current_interval                 AS currentInterval,
                    p.ease_factor                      AS easeFactor,
                    p.lapses                           AS lapses,
                    p.learning_step                    AS learningStep,
                    CONVERT_TZ(p.last_review_time, '+00:00', :jvmZoneIdParam) AS lastReviewTime,
                                                        CONVERT_TZ(p.due_date, '+00:00', :jvmZoneIdParam) AS dueDate,
                    p.consecutive_correct              AS consecutiveCorrect,
                    p.consecutive_incorrect            AS consecutiveIncorrect,
                    p.old_next_interval_again          AS oldNextIntervalAgain,
                    p.old_next_interval_hard           AS oldNextIntervalHard,
                    p.old_next_interval_good           AS oldNextIntervalGood,
                    p.old_next_interval_easy           AS oldNextIntervalEasy,
                    p.srs_version                      AS srsVersion,
                    CONVERT_TZ(p.created_at, '+00:00', :jvmZoneIdParam) AS createdAt,
                                                        CONVERT_TZ(p.updated_at, '+00:00', :jvmZoneIdParam) AS updatedAt                   
                FROM progress_with_srs p
                JOIN flashcards f ON p.card_id = f.card_id
                WHERE p.user_id     = :userId
                  AND f.set_id      = :setId
                  AND p.card_id IN (:cardIds)
            """, nativeQuery = true)
    List<IUserSRSProgressDTO> getSRSProgressByUserIdSetIdCardIds(
            @Param("userId") Long userId,
            @Param("setId") Long setId,
            @Param("cardIds") Set<Long> cardIds,
            @Param("jvmZoneIdParam") String jvmZoneIdParam
    );


    @Query(value = """
            SELECT
              /* số giây thực còn lại sau khi skip */
              TIMESTAMPDIFF(SECOND, :referenceTime, p.due_date) AS intervalSeconds
            FROM progress_with_srs p
            JOIN flashcards f
              ON p.card_id = f.card_id
            WHERE p.user_id            = :userId
              AND f.set_id             = :setId
              AND p.srs_version        = :srsVersion
              AND p.due_date          > :referenceTime
            ORDER BY p.due_date        ASC
            LIMIT 1
            """, nativeQuery = true)
    Optional<Long> getNearestIntervalSecondsOnSRS(
            @Param("userId")              Long userId,
            @Param("setId")               Long setId,
            @Param("srsVersion")          Long srsVersion,
            @Param("referenceTime")       Instant referenceTime
    );


//    @Query(value = """
//                SELECT
//                    p.progress_id                      AS id,
//                    p.card_id                          AS cardId,
//                    p.user_id                          AS userId,
//                    p.card_state                       AS cardState,
//                    p.current_interval                 AS currentInterval,
//                    p.ease_factor                      AS easeFactor,
//                    p.lapses                           AS lapses,
//                    p.learning_step                    AS learningStep,
//                    p.last_review_time                 AS lastReviewTime,
//                    p.due_date                         AS dueDate,
//                    p.consecutive_correct              AS consecutiveCorrect,
//                    p.consecutive_incorrect            AS consecutiveIncorrect,
//                    p.old_next_interval_again          AS oldNextIntervalAgain,
//                    p.old_next_interval_hard           AS oldNextIntervalHard,
//                    p.old_next_interval_good           AS oldNextIntervalGood,
//                    p.old_next_interval_easy           AS oldNextIntervalEasy,
//                    p.srs_version                      AS srsVersion,
//                    p.created_at                       AS createdAt,
//                    p.updated_at                       AS updatedAt
//                FROM progress_with_srs p
//                JOIN flashcards f ON p.card_id = f.card_id
//                WHERE p.user_id      = :userId
//                  AND f.set_id       = :setId
//                  AND p.srs_version  = :srsVersion
//                  AND p.due_date    <= :referenceTime
//                ORDER BY
//                  /* 1. learning_step nhỏ nhất */
//                  p.learning_step ASC,
//                  /* 2. các mục quá hạn xa nhất so với deadline */
//                  /* CODE CŨ: TIMESTAMPDIFF(SECOND, p.due_date, :referenceTime) DESC*/
//                  p.due_date ASC,
//                  /* 3. New - Learning → Lapsed → Review */
//                  FIELD(p.card_state, 'New', 'Learning','Lapsed','Review') ASC
//                LIMIT :limit
//            """, nativeQuery = true)
//    NO CODE: List<IUserSRSProgressDTO> getDueCardsPriority(
//            @Param("userId") Long userId,
//            @Param("setId") Long setId,
//            @Param("srsVersion") Long srsVersion,
//            @Param("referenceTime") Instant referenceTime,
//            @Param("limit") Long limit
//    );



//    @Query(value = """
//                SELECT
//                    p.progress_id                      AS id,
//                    p.card_id                          AS cardId,
//                    p.user_id                          AS userId,
//                    p.card_state                       AS cardState,
//                    p.current_interval                 AS currentInterval,
//                    p.ease_factor                      AS easeFactor,
//                    p.lapses                           AS lapses,
//                    p.learning_step                    AS learningStep,
//                    p.last_review_time                 AS lastReviewTime,
//                    p.due_date                         AS dueDate,
//                    p.consecutive_correct              AS consecutiveCorrect,
//                    p.consecutive_incorrect            AS consecutiveIncorrect,
//                    p.old_next_interval_again          AS oldNextIntervalAgain,
//                    p.old_next_interval_hard           AS oldNextIntervalHard,
//                    p.old_next_interval_good           AS oldNextIntervalGood,
//                    p.old_next_interval_easy           AS oldNextIntervalEasy,
//                    p.srs_version                      AS srsVersion,
//                    p.created_at                       AS createdAt,
//                    p.updated_at                       AS updatedAt
//                FROM progress_with_srs p
//                JOIN flashcards f ON p.card_id = f.card_id
//                WHERE p.user_id      = :userId
//                  AND f.set_id       = :setId
//                  AND p.srs_version  = :srsVersion
//                  AND p.due_date    <= :referenceTime
//                ORDER BY
//                  p.learning_step ASC,
//                  FIELD(p.card_state, 'New', 'Learning','Lapsed','Review') ASC,
//                  /*CODE CŨ: TIMESTAMPDIFF(SECOND, p.due_date, :referenceTime) DESC*/
//                  p.due_date ASC
//                LIMIT :limit
//            """, nativeQuery = true)
//    NO CODE: List<IUserSRSProgressDTO> getDueCardsOrdered(
//            @Param("userId") Long userId,
//            @Param("setId") Long setId,
//            @Param("srsVersion") Long srsVersion,
//            @Param("referenceTime") Instant referenceTime,
//            @Param("limit") Long limit
//    );

    //    @Query(value = """
//        SELECT
//            p.progress_id                      AS id,
//            p.card_id                          AS cardId,
//            p.user_id                          AS userId,
//            p.card_state                       AS cardState,
//            p.current_interval                 AS currentInterval,
//            p.ease_factor                      AS easeFactor,
//            p.lapses                           AS lapses,
//            p.learning_step                    AS learningStep,
//            p.last_review_time                 AS lastReviewTime,
//            p.due_date                         AS dueDate,
//            p.consecutive_correct              AS consecutiveCorrect,
//            p.consecutive_incorrect            AS consecutiveIncorrect,
//            p.old_next_interval_again          AS oldNextIntervalAgain,
//            p.old_next_interval_hard           AS oldNextIntervalHard,
//            p.old_next_interval_good           AS oldNextIntervalGood,
//            p.old_next_interval_easy           AS oldNextIntervalEasy,
//            p.srs_version                      AS srsVersion,
//            p.created_at                       AS createdAt,
//            p.updated_at                       AS updatedAt
//        FROM progress_with_srs p
//        JOIN flashcards f ON p.card_id = f.card_id
//        WHERE p.user_id     = :userId
//          AND f.set_id      = :setId
//          AND p.card_id IN (:cardIds)
//          AND p.srs_version = :srsVersion
//    """, nativeQuery = true)
//    NO CODE: List<IUserSRSProgressDTO> getSRSProgressByUserId_SetId_CardIdsWithVersion(
//            @Param("userId")    Long userId,
//            @Param("setId")     Long setId,
//            @Param("cardIds")   Set<Long> cardIds,
//            @Param("srsVersion") Long srsVersion
//    );


    //    @Query(value = """
//                SELECT
//                    p.progress_id                      AS id,
//                    p.card_id                          AS cardId,
//                    p.user_id                          AS userId,
//                    p.card_state                       AS cardState,
//                    p.current_interval                 AS currentInterval,
//                    p.ease_factor                      AS easeFactor,
//                    p.lapses                           AS lapses,
//                    p.learning_step                    AS learningStep,
//                    p.last_review_time                 AS lastReviewTime,
//                    p.due_date                         AS dueDate,
//                    p.consecutive_correct              AS consecutiveCorrect,
//                    p.consecutive_incorrect            AS consecutiveIncorrect,
//                    p.old_next_interval_again          AS oldNextIntervalAgain,
//                    p.old_next_interval_hard           AS oldNextIntervalHard,
//                    p.old_next_interval_good           AS oldNextIntervalGood,
//                    p.old_next_interval_easy           AS oldNextIntervalEasy,
//                    p.srs_version                      AS srsVersion,
//                    p.created_at                       AS createdAt,
//                    p.updated_at                       AS updatedAt
//                FROM progress_with_srs p
//                JOIN flashcards f ON p.card_id = f.card_id
//                WHERE p.user_id     = :userId
//                  AND f.set_id      = :setId
//                  AND p.srs_version = :srsVersion
//                  AND p.due_date   <= CURRENT_TIMESTAMP
//                ORDER BY p.learning_step ASC, p.due_date ASC
//                LIMIT :limit
//            """, nativeQuery = true)
//    NO CODE: List<IUserSRSProgressDTO> getSRSProgressDueDateOrderByLevel(
//            @Param("userId") Long userId,
//            @Param("setId") Long setId,
//            @Param("limit") Long limit,
//            @Param("srsVersion") Long srsVersion
//    );
}
