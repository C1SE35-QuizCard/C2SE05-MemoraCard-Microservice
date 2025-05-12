package com.example.quizcards.repository;

import com.example.quizcards.dto.DeadlineReminderListDTO;
import com.example.quizcards.dto.IDeadlineReminderDTO;
import com.example.quizcards.entities.AppUser;
import com.example.quizcards.entities.DeadlineReminder;
import com.example.quizcards.entities.SetFlashcard;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;

@Repository
public interface IDeadlineReminderRepository extends JpaRepository<DeadlineReminder, Long> {

    @Query(value = """
            select d.deadline_reminders_id, d.reminder_time, a.user_id, s.set_id, s.title, COUNT(f.card_id) as card_count
            from deadline_reminders d
            join app_users a on d.user_id = a.user_id
            join set_flashcards s on d.set_id = s.set_id
            left join flashcards f on f.set_id = s.set_id
            where d.deadline_reminders_id = :deadline_reminders_id
            group by d.deadline_reminders_id, d.reminder_time, a.user_id, s.set_id, s.title
            """, nativeQuery = true)
    IDeadlineReminderDTO findDeadlineReminderById(@Param("deadline_reminders_id") Long deadlineRemindersId);

    @Query(value = """
            select dl.deadline_reminders_id,s.set_id,s.title, count(distinct fl.card_id) as card_count,max(dl.reminder_time) as reminder_time
            from deadline_reminders dl
            join set_flashcards s on s.set_id=dl.set_id
            join flashcards fl on fl.set_id=s.set_id
            where dl.user_id=:user_id and reminder_time>= now()
            group by s.set_id,dl.deadline_reminders_id
            order by reminder_time asc
            ;
            """, nativeQuery = true)
    List<DeadlineReminderListDTO> findDeadlineReminderByUserId(@Param("user_id") Long userId);

    @Query(value = """
            select d.deadline_reminders_id, d.reminder_time, a.user_id, s.set_id, s.title, COUNT(f.card_id) as card_count
                                                    from deadline_reminders d
                                                    join app_users a on d.user_id = a.user_id
                                                    join set_flashcards s on d.set_id = s.set_id
                                                    left join flashcards f on f.set_id = s.set_id
                                                    where s.set_id =:set_id and d.user_id=:user_id and d.reminder_time >= now()
                                                    group by d.deadline_reminders_id, d.reminder_time, a.user_id, s.set_id, s.title
                                                    order by d.reminder_time asc
            """, nativeQuery = true)
    List<IDeadlineReminderDTO> findDeadlineReminderBySetId(@Param("set_id") Long setId,@Param("user_id") Long userId);


    @Modifying
    @Transactional
    @Query(value = """
            insert into deadline_reminders(reminder_time, user_id, set_id)
            values (:reminder_time, :user_id, :set_id)
            """, nativeQuery = true)
    void createDeadlineReminder(@Param("reminder_time") Timestamp reminderTime,
                                @Param("user_id") Long userId,
                                @Param("set_id") Long setId);

    @Modifying
    @Transactional
    @Query(value = """
            delete from deadline_reminders d
            where d.deadline_reminders_id = :deadline_reminders_id
            """, nativeQuery = true)
    void deleteDeadlineReminder(@Param("deadline_reminders_id") Long deadlineRemindersId);

    @Modifying
    @Transactional
    @Query(value = """
            update deadline_reminders d
            set d.reminder_time = :reminder_time, d.user_id = :user_id, d.set_id = :set_id
            where d.deadline_reminders_id = :deadline_reminders_id
            """, nativeQuery = true)
    void updateDeadlineReminder(@Param("deadline_reminders_id") Long deadlineRemindersId,
                                @Param("reminder_time") Timestamp reminderTime,
                                @Param("user_id") Long userId,
                                @Param("set_id") Long setId);

    @Query("SELECT EXISTS (SELECT 1 FROM DeadlineReminder dr " +
            "WHERE dr.setFlashcards.setId = :set_id " +
            "AND dr.user.userId = :user_id " +
            "AND dr.reminderTime = :time_line)")
    boolean existsDeadlineReminderGreaterThanNowBySetId(@Param("set_id") Long setId,
                                                        @Param("user_id") Long userId,
                                                        @Param("time_line") Timestamp timestamp);

//    @Query("SELECT EXISTS (SELECT 1 FROM DeadlineReminder dr " +
//            "WHERE dr.setFlashcards.setId = :set_id " +
//            "AND dr.user.userId = :user_id " +
//            "AND dr.reminderTime >= FUNCTION('utc_timestamp'))")
//    boolean existsDeadlineReminderGreaterThanNowBySetId(@Param("set_id") Long setId,
//                                                        @Param("user_id") Long userId);
//    @Query(value = """
//            select count(d.deadline_reminders_id)
//            from deadline_reminders d
//            where d.user_id = :user_id and d.set_id = :set_id
//            """, nativeQuery = true)
//    int existsByUserIdAndSetId(@Param("user_id") Long userId, @Param("set_id") Long setId);
//
//    @Query(value = """
//            SELECT COUNT(deadline_reminders_id)
//            FROM deadline_reminders
//            WHERE user_id = :user_id AND set_id = :set_id AND deadline_reminders_id <> :deadline_reminders_id
//            """, nativeQuery = true)
//    int existsByUserIdAndSetIdAndNotId(@Param("user_id") Long userId, @Param("set_id") Long setId, @Param("deadline_reminders_id") Long deadlineRemindersId);
}