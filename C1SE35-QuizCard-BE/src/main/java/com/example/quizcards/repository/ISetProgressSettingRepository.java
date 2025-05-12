package com.example.quizcards.repository;

import com.example.quizcards.entities.SetProgressSetting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ISetProgressSettingRepository extends JpaRepository<SetProgressSetting, Long> {
    Optional<SetProgressSetting> findBySetFlashcard_SetIdAndAppUser_UserId(Long setId, Long userId);

    @Query(value = """
            SELECT sps.current_simple_mode_version
            FROM set_progress_setting sps
            WHERE sps.card_id = :cardId AND sps.user_id = :userId
            LIMIT 1
    """, nativeQuery = true)
    Long findCurrentSimpleModeVersionByCardIdAndUserId(
            @Param("cardId") Long cardId,
            @Param("userId") Long userId);

//   @Query(value = """
//            SELECT sps.current_srs_version
//            FROM set_progress_setting sps
//            JOIN set_flashcard sf ON sps.set_id = sf.set_id
//            JOIN flashcard f ON sf.card_id = f.card_id
//            WHERE f.card_id = :cardId AND sps.user_id = :userId
//            LIMIT 1
//    """, nativeQuery = true)
//    NO CODE: Integer findCurrentSRSVersionByCardIdAndUserId(
//            @Param("cardId") Long cardId,
//            @Param("userId") Long userId);
}
