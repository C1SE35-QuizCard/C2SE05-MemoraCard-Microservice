package com.example.quizcards.repository;

import com.example.quizcards.dto.IFlashcardProgressDTO;
import com.example.quizcards.dto.IProgressDTO;
import com.example.quizcards.dto.IProgressAnalysisDTO;
import com.example.quizcards.entities.UserProgress;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface IUserProgressRepository extends JpaRepository<UserProgress, Long> {

//    @Deprecated
//    @Query(value = """
//            select
//                s.set_id,
//                s.title,
//                a.avatar,
//                a.user_name,
//                count(f.card_id) as total_cards,
//                sum(case when up.progress_type = 1 then 1 else 0 end) as completed_cards,
//                sum(case when up.progress_type = 0 or up.progress_type is null then 1 else 0 end) as uncompleted_cards
//            from
//                set_flashcards s
//            join
//                app_users a on a.user_id = s.user_id
//            join
//                flashcards f on s.set_id = f.set_id
//            left join
//                user_progress up on f.card_id = up.card_id and up.user_id = :user_id
//            where
//                s.user_id = :user_id
//            group by
//                s.set_id, s.title, a.avatar, a.user_name
//            """, nativeQuery = true)
//    List<IUserProgressDTO> findUserSetProgress(@Param("user_id") Long userId);

    @Deprecated
    @Query(value = """
                    select
                        s.set_id,
                        s.title,
                        a.avatar,
                        a.user_name,
                        f.card_id,
                        f.question,
                        f.answer,
                        up.progress_type as status_progress,
                        up.marked_for_attention as status_mark,
                        f.image_url
                    from
                        flashcards f
                    join
                        set_flashcards s on s.set_id = f.set_id
                    join
                        app_users a on a.user_id = s.user_id
                    left join
                        user_progress up on f.card_id = up.card_id and up.user_id =:userId
                    where f.set_id =:setId
            """, nativeQuery = true)
    List<IFlashcardProgressDTO> findFlashcardsProgressBySetId(@Param("setId") Long setId, @Param("userId") Long userId);

    @Deprecated
    @Query(value = """
                    select
                        f.card_id as cardId,
                        f.question as question,
                        f.answer as answer,
                        f.image_url as imageUrl,
                        up.marked_for_attention as statusMark
                    from
                        flashcards f
                    left join
                        user_progress up on f.card_id = up.card_id and up.user_id =:userId
                    where f.card_id in (:cardIds)
                    LIMIT :limit
            """, nativeQuery = true)
    List<IFlashcardProgressDTO> findProgCardsByCardIdsIn(@Param("userId") Long userId,
                                                         @Param("cardIds") List<Long> cardId,
                                                         @Param("limit") long limit);

    @Deprecated
    @Query(value = """
            SELECT\s
                s.set_id as setId,
                s.title as setTitle,
                COUNT(DISTINCT CASE WHEN up.progress_type = FALSE THEN up.progress_id END) AS totalCardRecall,
                COUNT(DISTINCT CASE WHEN up.progress_type = TRUE THEN up.progress_id END) AS totalCardRemember,
                (COUNT(DISTINCT f.card_id) -\s
                 COUNT(DISTINCT CASE WHEN up.progress_type = TRUE THEN up.progress_id END) -\s
                 COUNT(DISTINCT CASE WHEN up.progress_type = FALSE THEN up.progress_id END)) AS totalCardNotLearn
            FROM set_flashcards s
            LEFT JOIN flashcards f ON s.set_id = f.set_id
            LEFT JOIN user_progress up ON f.card_id = up.card_id\s
                AND up.user_id = :user_id
            WHERE s.set_id = :set_id
            GROUP BY s.set_id, s.title;
            """, nativeQuery = true)
    IProgressAnalysisDTO findAnalysisProgressBySetId(@Param("set_id") Long setId,
                                                     @Param("user_id") Long userId);

    @Query(value = """
            SELECT
                u.progress_id                           AS progressId,
                u.progress_type                         AS progressType,
                u.marked_for_attention                  AS isAttention,
                u.user_id                               AS userId,
                u.card_id                               AS cardId,
                u.consecutive_correct_simple_mode       AS consecutiveCorrectSimpleMode,
                u.count_consecutive_hard_press          AS countConsecutiveHardPress,
                u.mode_version                          AS progressVersion
                FROM user_progress u
                JOIN flashcards f 
                  ON u.card_id = f.card_id
                WHERE u.user_id = :user_id
                  AND f.set_id = :set_id
            """, nativeQuery = true)
    List<IProgressDTO> findUserProgressBySetIdAndUserId(
            @Param("set_id") Long setId,
            @Param("user_id") Long userId
    );

    @Deprecated
    Page<UserProgress> findByAppUser_UserIdAndFlashcard_Set_SetId(
            @Param("userId") Long userId,
            @Param("setId") Long setId,
            Pageable pageable
    );

    @Deprecated
    @Query(value = """
            select count(u.progress_id)
            from user_progress u
            where u.user_id = :user_id and u.card_id = :card_id
            """, nativeQuery = true)
    int existsByUserIdAndCardId(@Param("user_id") Long userId, @Param("card_id") Long cardId);

    @Deprecated
    @Query(value = """
            select COUNT(u.progress_id)
            from user_progress u
            where u.user_id = :user_id and u.card_id = :card_id and u.progress_id <> :progress_id
            """, nativeQuery = true)
    int existsByUserIdAndCardIdAndNotId(@Param("user_id") Long userId, @Param("card_id") Long cardId, @Param("progress_id") Long progressId);

    @Query(value = """
                SELECT
                    s.set_id                            AS setId,
                    s.title                             AS setTitle,
                    SUM(CASE WHEN p.progress_type = 0 THEN 1 ELSE 0 END) AS totalCardRecall,
                    SUM(CASE WHEN p.progress_type = 1 THEN 1 ELSE 0 END) AS totalCardRemember,
                    COUNT(f.card_id) - COUNT(p.progress_id)              AS totalCardNotLearn
                FROM set_flashcards s
                JOIN flashcards f
                  ON f.set_id = s.set_id
                LEFT JOIN user_progress p
                  ON p.card_id      = f.card_id
                 AND p.user_id      = :userId
                 AND p.mode_version = :modeVersion
                WHERE s.set_id = :setId
                GROUP BY s.set_id, s.title
            """, nativeQuery = true)
    IProgressAnalysisDTO findAnalysisProgressBySetId(
            @Param("setId") Long setId,
            @Param("userId") Long userId,
            @Param("modeVersion") long modeVersion
    );

    @Query(value = """
                    select
                        s.set_id,
                        s.title,
                        a.avatar,
                        a.user_name,
                        f.card_id,
                        f.question,
                        f.answer,
                        up.progress_type as status_progress,
                        up.marked_for_attention as status_mark,
                        f.image_url
                    from
                        flashcards f
                    join
                        set_flashcards s on s.set_id = f.set_id
                    join
                        app_users a on a.user_id = s.user_id
                    left join
                        user_progress up on f.card_id = up.card_id and up.user_id =:userId
                                and up.mode_version = :modeVersion
                    where f.set_id =:setId
            """, nativeQuery = true)
    List<IFlashcardProgressDTO> findFlashcardsProgressBySetId(@Param("setId") Long setId,
                                                              @Param("userId") Long userId,
                                                              @Param("modeVersion") long version);

    @Query(value = """
                SELECT
                    u.progress_id              AS progressId,
                    u.progress_type            AS progressType,
                    u.marked_for_attention     AS isAttention,
                    u.user_id                  AS userId,
                    u.card_id                  AS cardId,
                    u.consecutive_correct_simple_mode AS consecutiveCorrectSimpleMode,
                    u.count_consecutive_hard_press    AS countConsecutiveHardPress,
                    u.mode_version             AS progressVersion
                FROM user_progress u
                JOIN flashcards f
                  ON u.card_id = f.card_id
                WHERE u.user_id      = :userId
                  AND f.set_id       = :setId
                  AND u.mode_version = :modeVersion
            """, nativeQuery = true)
    List<IProgressDTO> findUserProgressBySetIdAndUserId(
            @Param("setId") Long setId,
            @Param("userId") Long userId,
            @Param("modeVersion") long modeVersion
    );

//    @Query(value = """
//        SELECT
//            u.progress_id                        AS progressId,
//            u.progress_type                      AS progressType,
//            u.marked_for_attention               AS isAttention,
//            u.user_id                            AS userId,
//            u.card_id                            AS cardId,
//            u.consecutive_correct_simple_mode    AS consecutiveCorrectSimpleMode,
//            u.count_consecutive_hard_press       AS countConsecutiveHardPress,
//            u.mode_version                       AS progressVersion
//        FROM user_progress u
//        JOIN flashcards f
//          ON u.card_id = f.card_id
//        WHERE u.user_id     = :userId
//          AND f.set_id      = :setId
//          AND f.card_id IN (:cardIds)
//          AND u.mode_version = :modeVersion
//    """, nativeQuery = true)
//    List<IProgressDTO> getProgressByUserIdSetIdCardIdsWithVersion(
//            @Param("userId")      Long userId,
//            @Param("setId")       Long setId,
//            @Param("cardIds")     Set<Long> cardIds,
//            @Param("modeVersion") Long modeVersion
//    );

    @Query(value = """
                SELECT
                    u.progress_id                        AS progressId,
                    u.progress_type                      AS progressType,
                    u.marked_for_attention               AS isAttention,
                    u.user_id                            AS userId,
                    u.card_id                            AS cardId,
                    u.consecutive_correct_simple_mode    AS consecutiveCorrectSimpleMode,
                    u.count_consecutive_hard_press       AS countConsecutiveHardPress,
                    u.mode_version                       AS progressVersion
                FROM user_progress u
                JOIN flashcards f
                  ON u.card_id = f.card_id
                WHERE u.user_id     = :userId
                  AND f.set_id      = :setId
                  AND f.card_id IN (:cardIds)
            """, nativeQuery = true)
    List<IProgressDTO> getProgressByUserIdSetIdCardIds(
            @Param("userId") Long userId,
            @Param("setId") Long setId,
            @Param("cardIds") Set<Long> cardIds
    );


    @Query(value = """
            select u.progress_type, u.marked_for_attention, u.user_id, u.card_id
            from user_progress u
            where u.progress_id = :progress_id
            """, nativeQuery = true)
    IProgressDTO findUserProgressById(@Param("progress_id") Long progressId);


    @Deprecated
    Optional<UserProgress> findByAppUser_UserIdAndFlashcard_CardId(
            @Param("userId") Long userId,
            @Param("cardId") Long cardId
    );

    Optional<UserProgress> findByAppUser_UserIdAndFlashcard_CardIdAndModeVersion(
            @Param("userId") Long userId,
            @Param("cardId") Long cardId,
            @Param("modeVersion") long modeVersion
    );

    @Modifying
    @Transactional
    @Query(value = """
            delete up
            FROM user_progress up
            join flashcards f on up.card_id = f.card_id
            where up.user_id = :user_id and f.set_id = :set_id
            """, nativeQuery = true)
    void deleteUserProgressByUserAndSetId(@Param("user_id") Long userId,
                                          @Param("set_id") Long setId);

    @Deprecated
    @Modifying
    @Transactional
    @Query(value = """
            insert into user_progress(progress_type, marked_for_attention, user_id, card_id)
            values (:progress_type, :marked_for_attention, :user_id, :card_id)
            """, nativeQuery = true)
    void createUserProgress(@Param("progress_type") Boolean progressType,
                            @Param("marked_for_attention") Boolean isAttention,
                            @Param("user_id") Long userId,
                            @Param("card_id") Long cardId);

    @Deprecated
    @Modifying
    @Transactional
    @Query(value = """
            delete from user_progress u
            where u.progress_id = :progress_id
            """, nativeQuery = true)
    void deleteUserProgressById(@Param("progress_id") Long progressId);

    @Deprecated
    @Modifying
    @Transactional
    @Query(value = """
             UPDATE user_progress u
                SET\s
                  u.progress_type         = COALESCE(:progress_type, u.progress_type),
                  u.marked_for_attention  = COALESCE(:marked_for_attention, u.marked_for_attention),
                  u.user_id               = COALESCE(:user_id, u.user_id),
                  u.card_id               = COALESCE(:card_id, u.card_id)
                WHERE u.progress_id = :progress_id
            """, nativeQuery = true)
    void updateUserProgress(@Param("progress_id") Long progressId,
                            @Param("progress_type") Boolean progressType,
                            @Param("marked_for_attention") Boolean isAttention,
                            @Param("user_id") Long userId,
                            @Param("card_id") Long cardId);


}
