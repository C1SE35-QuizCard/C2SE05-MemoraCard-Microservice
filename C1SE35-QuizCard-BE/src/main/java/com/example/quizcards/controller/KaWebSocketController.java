package com.example.quizcards.controller;

import com.example.quizcards.dto.response.UserJoiningInfo;
import com.example.quizcards.service.impl.RoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@Controller
public class KaWebSocketController {
    @Autowired
    private RoomService roomService;
    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    @MessageMapping("/list-players/{roomCode}")
    @SendTo("/topic/players/{roomCode}")
    public List<UserJoiningInfo> listPlayers(@PathVariable String roomCode) {
        return roomService.listPlayersInRoom(roomCode);
    }
    @MessageMapping("/start-battle/{roomId}")
    @SendTo("/topic/start-battle/{roomId}")
    public String startMessage(@DestinationVariable String roomId) {
        // Logic xử lý trước khi bắt đầu trận đấu (nếu cần)
        return "START";
    }

    @MessageMapping("/room-game/{roomCode}")
    @SendTo("/topic/room-game/{roomCode}")
    public String startBattle(@PathVariable String roomCode) {
        return roomCode;
    }

}
