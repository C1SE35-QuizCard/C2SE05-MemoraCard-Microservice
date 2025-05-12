package com.example.quizcards.repository;

import com.example.quizcards.entities.KaRoom;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;

@Repository
public interface KaRoomRepository extends JpaRepository<KaRoom, Long> {
    @Query("SELECT r.roomId FROM KaRoom r WHERE r.pinCode = :roomCode")
    Optional<Long> findRoomIdByRoomCode(@Param("roomCode") String roomCode);
    boolean existsByPinCode(String pinCode);
    KaRoom findByPinCode(String pinCode);
    boolean existsByPinCodeAndAppUser_UserId(String pinCode,Long userId);
    @Query(value = """
        SELECT room_id, room_name, pin_code, created_at,time_per_question,total_rounds,set_id,quantity_question
        FROM ka_rooms
        WHERE pin_code = :pinCode
        """, nativeQuery = true)
    Map<String, Object> findByPinCodeShortDetail(String pinCode);

    @Modifying
    @Transactional
    @Query(value = """
            INSERT INTO
            ka_rooms(room_name, set_id, pin_code, current_round, max_joiner, total_rounds, quantity_question, time_per_question, status_active,author_id,created_at,current_joiner)
            VALUES (:roomName,:setId,:pinCode, 0,:maxJoiner,:totalRounds,:quantityQuestion,:timePerQuestion, 'WAITING',:userId,current_timestamp,0)
            """, nativeQuery = true)
    void save(String roomName, Long setId, String pinCode, Integer maxJoiner, Integer totalRounds, Integer quantityQuestion, Integer timePerQuestion, Long userId);
    @Modifying
    @Transactional
    @Query(value = """
        UPDATE ka_rooms
        SET pin_code = :newPinCode
        WHERE pin_code = :oldPinCode AND author_id = :userId
        """, nativeQuery = true)
    void updateRoomPinCode(String oldPinCode, String newPinCode, Long userId);
}
