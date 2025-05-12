package com.example.quizcards.repository;

import com.example.quizcards.dto.SortListDTO;
import com.example.quizcards.entities.AppUser;
import com.example.quizcards.entities.SetFlashcard;
import com.example.quizcards.entities.UserFlashcardSetting;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IFlashcardSettingRepository extends JpaRepository<UserFlashcardSetting, Long> {
    Optional<UserFlashcardSetting> findByUser_UserIdAndSetFlashcard_SetId(Long userId, Long setId);

    @Query(value = """
            SELECT 1
            FROM user_flashcard_settings ufs
            WHERE ufs.set_id =:setId
            AND ufs.user_id =:userId
            LIMIT 1;
            """, nativeQuery = true)
    Long existsUserFlashcardSetting(
            @Param("setId") Long setId,
            @Param("userId") Long userId
    );

    @Modifying
    @Transactional
    @Query(value = """
            insert into user_flashcard_settings(last_accessed,set_id,user_id) values(now(),:setId,:userId);
            """, nativeQuery = true)
    void save(Long userId, Long setId);
    @Query(value = """

                     SELECT ufs.set_id,\s
                   s.title,\s
                   s.user_id,\s
                   MAX(ufs.last_accessed) AS date_created_or_last_accessed,\s
                   au.user_name AS author,\s
                   au.avatar,
                   COUNT(DISTINCT fl.card_id) AS total_card
            FROM user_flashcard_settings ufs
            JOIN set_flashcards s ON s.set_id = ufs.set_id
            JOIN app_users au ON au.user_id = s.user_id
            JOIN flashcards fl ON fl.set_id = s.set_id
            WHERE ufs.user_id =:user_id
            GROUP BY ufs.set_id, s.title, s.user_id, au.user_name, au.avatar
            ORDER BY date_created_or_last_accessed DESC
            LIMIT 5;
                     """, nativeQuery = true)
    List<SortListDTO> findAllRecentByUserId(@Param("user_id") Long userId);

    @Query(value = """
        SELECT s.set_id,s.title,s.user_id,s.created_at as date_created_or_last_accessed,au.user_name as author,au.avatar,
        count(distinct fl.card_id) as total_card
        FROM set_flashcards s
        join app_users au on au.user_id=s.user_id
        join flashcards fl on fl.set_id = s.set_id
        where s.user_id=:user_id
        group by s.set_id
        order by s.created_at desc;
            """, nativeQuery = true)
    List<SortListDTO> findSortedByCreated(@Param("user_id") Long userId);
    @Query(value = """
            SELECT s.set_id,s.title,s.user_id,s.created_at as date_created_or_last_accessed,au.user_name as author,au.avatar,
                    count(distinct fl.card_id) as total_card
            from user_progress ug
            join flashcards fl on fl.card_id=ug.card_id
            join set_flashcards s on s.set_id=fl.set_id
            join app_users au on au.user_id =s.user_id
            where ug.user_id=:user_id
            group by s.set_id
            ;
                        """, nativeQuery = true)
    List<SortListDTO> findSortedByLearned(@Param("user_id") Long userId);
}
