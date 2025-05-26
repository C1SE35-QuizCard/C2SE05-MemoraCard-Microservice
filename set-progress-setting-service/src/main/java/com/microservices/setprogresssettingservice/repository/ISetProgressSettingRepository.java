package com.microservices.setprogresssettingservice.repository;

import com.microservices.setprogresssettingservice.model.SetProgressSetting;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface ISetProgressSettingRepository extends R2dbcRepository<SetProgressSetting, Long> {

    /**
     * Derived query: tìm bản ghi theo set_id và user_id
     */
    Mono<SetProgressSetting> findBySetFlashcardIdAndAppUserId(Long setId, Long userId);

    /**
     * Truy vấn native để lấy current_simple_mode_version theo card_id và user_id
     */
    @Query("""
        SELECT COALESCE(sps.current_simple_mode_version, 1)
            FROM set_progress_setting sps
            JOIN set_flashcards sf ON sps.set_id = sf.set_id
            JOIN flashcards f ON sf.set_id = f.set_id
            WHERE f.card_id = :cardId AND sps.user_id = :userId
            LIMIT 1
    """)
    Mono<Long> findCurrentSimpleModeVersionByCardIdAndUserId(
            Long cardId,
            Long userId
    );

    //    @Query("""
    //        SELECT sps.current_srs_version
    //          FROM set_progress_setting sps
    //          JOIN set_flashcard sf ON sps.set_id = sf.set_id
    //          JOIN flashcard f       ON sf.card_id = f.card_id
    //         WHERE f.card_id = :cardId
    //           AND sps.user_id = :userId
    //         LIMIT 1
    //    """)
    //    Mono<Integer> findCurrentSRSVersionByCardIdAndUserId(
    //        Long cardId,
    //        Long userId
    //    );
}
