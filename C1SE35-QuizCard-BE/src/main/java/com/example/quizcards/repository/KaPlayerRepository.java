package com.example.quizcards.repository;

import com.example.quizcards.dto.response.UserJoiningInfo;
import com.example.quizcards.entities.KaPlayer;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface KaPlayerRepository extends JpaRepository<KaPlayer, Long> {
    KaPlayer findByUser_UserIdAndRoom_PinCode(Long userId,String pinCode);

    boolean existsByRoom_RoomIdAndUser_UserId(Long roomId, Long userId);

    @Modifying
    @Transactional
    @Query(value = """
            INSERT INTO 
            ka_players(room_id,user_id)
            values(:roomId,:userId)
            """, nativeQuery = true)
    void joinRoom(Long roomId, Long userId);

    @Query(value = """
               SELECT au.user_id, au.user_name,au.avatar
               FROM ka_players pl
               JOIN app_users au ON au.user_id = pl.user_id
               JOIN ka_rooms kr ON kr.room_id = pl.room_id
               WHERE kr.pin_code =:room_code
             """, nativeQuery = true)
    List<UserJoiningInfo> findUserJoiningInfoByRoomId(@Param("room_code") String roomCode);
}


