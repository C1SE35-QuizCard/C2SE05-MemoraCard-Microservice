package com.example.quizcards.repository;

import com.example.quizcards.dto.response.KaQuestionProjection;
import com.example.quizcards.entities.KaQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface KaQuestionRepository extends JpaRepository<KaQuestion, Long> {
    Optional<KaQuestion> findKaQuestionById(Long id);
    @Query(value = "SELECT id,room_id,answer_list,question FROM ka_questions k WHERE k.room_id = (SELECT r.room_id FROM ka_rooms r WHERE r.pin_code =:roomCode)",nativeQuery = true)
    List<KaQuestionProjection> findQuestionsByRoomCode(String roomCode);
}
