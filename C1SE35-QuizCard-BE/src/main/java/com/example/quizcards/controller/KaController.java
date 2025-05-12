package com.example.quizcards.controller;

import com.example.quizcards.dto.request.*;
import com.example.quizcards.dto.response.ApiResponse;
import com.example.quizcards.dto.response.KaQuestionProjection;
import com.example.quizcards.dto.response.UserJoiningInfo;
import com.example.quizcards.entities.KaAnswer;
import com.example.quizcards.entities.KaPlayer;
import com.example.quizcards.entities.KaQuestion;
import com.example.quizcards.entities.KaRoom;
import com.example.quizcards.entities.TestDataPackage.MCQuestion;
import com.example.quizcards.repository.KaPlayerRepository;
import com.example.quizcards.repository.KaQuestionRepository;
import com.example.quizcards.repository.KaRoomRepository;
import com.example.quizcards.security.UserPrincipal;
import com.example.quizcards.service.impl.KaAnswerService;
import com.example.quizcards.service.impl.RoomService;
import com.example.quizcards.service.impl.UserRankingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/v1/ka")
public class KaController {
    @Autowired
    UserRankingService userRankingService;

    @Autowired
    private KaPlayerRepository kaPlayerRepository;
    @Autowired
    private KaQuestionRepository kaQuestionRepository;
    @Autowired
    private KaRoomRepository kaRoomRepository;
    @Autowired
    private RoomService roomService;
    @Autowired
    private KaAnswerService kaAnswerService;

    //    @PostMapping("/room/create")
//    public ResponseEntity<?> createRoom(@RequestBody RoomRequestDTO roomRequestDTO) {
//        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//        UserPrincipal up = (UserPrincipal) authentication.getPrincipal();
//        try {
//            Map<String, Object> createdRoom = roomService.createRoom(roomRequestDTO, up.getId());
//            return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse(true, "Room created successfully", HttpStatus.CREATED, createdRoom));
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred while creating the room");
//        }
//    }
    @PostMapping("/room/create")
    public ResponseEntity<?> createRoom(@RequestBody RoomRequestDTO roomRequestDTO) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserPrincipal up = (UserPrincipal) authentication.getPrincipal();
        try {

            Map<String, Object> createdRoom = roomService.createRoom(roomRequestDTO, up.getId());
            Long roomId = (Long) createdRoom.get("room_id");


            KQRequest kqRequest = new KQRequest();
            kqRequest.setSetId(roomRequestDTO.getSetId());
            kqRequest.setNumQuiz((long) roomRequestDTO.getNumQuestions());
            List<MCQuestion> questions = roomService.createListQuestions(kqRequest);


            for (MCQuestion mcQuestion : questions) {
                KaQuestion kaQuestion = new KaQuestion();
                kaQuestion.setQuestion(mcQuestion.getQuestion());
                kaQuestion.setAnswerList(mcQuestion.getAnswerList().stream()
                        .map(answer -> new KaQuestion.Answer(answer.getId(), answer.getAnswer(), answer.isTrue()))
                        .toList());
                kaQuestion.setAnswerId(mcQuestion.getAnswerId());
                kaQuestion.setCardId(mcQuestion.getCardId());
                kaQuestion.setRoomId(roomId);
                roomService.saveKaQuestion(kaQuestion);
            }

            return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse(true, "Room created successfully", HttpStatus.CREATED, createdRoom));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred while creating the room");
        }
    }

    @Transactional
    @GetMapping("/room/infoRoom/{room-code}")
    public ResponseEntity<Object> getInfoRoom(@PathVariable("room-code") RoomCodeDTO roomCodeDTO) {
        try {

            Map<String, Object> room = kaRoomRepository.findByPinCodeShortDetail(roomCodeDTO.getCode());
            if (room == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse(false, "Room not found", HttpStatus.NOT_FOUND));
            }

            if (roomService.isExistPlayer(roomCodeDTO.getCode())) {
                return ResponseEntity.ok(room);
            }
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You are not in this room");
        } catch (Exception e) {
            System.out.println(e);
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred while joining the room");
        }
    }

    @PostMapping("/room/refresh-code")
    public ResponseEntity<?> refreshCode(@RequestBody RefreshCodeRoom refreshCodeRoom) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserPrincipal up = (UserPrincipal) authentication.getPrincipal();
        try {
            Map<String, Object> refreshCode = roomService.refreshCode(refreshCodeRoom, up.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse(true, "Code room refreshed successfully", HttpStatus.CREATED, refreshCode));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred while creating the room");
        }
    }

    @PostMapping("/room/join-room/{room-code}")
    public ResponseEntity<?> joinRoom(@PathVariable("room-code") String code) {
        try {
            KaRoom room = roomService.findRoomByPinCode(code);
            if (room == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse(false, "Room not found", HttpStatus.NOT_FOUND));
            }
            if (!room.canJoinRoom()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ApiResponse(false, "Room is full", HttpStatus.FORBIDDEN));
            }
            if (roomService.isExistPlayer(code)) {
                return ResponseEntity.status(HttpStatus.OK).body("You have already joined in the lobby room.");
            }
            roomService.joinRoom(room);
            return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse(true, "Join room successfully", HttpStatus.CREATED));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred while joining the room");
        }
    }

    @GetMapping("/room/players/{room-code}")
    public ResponseEntity<Object> listPlayers(@PathVariable("room-code") String code) {
        try {
            List<UserJoiningInfo> list = roomService.listPlayersInRoom(code);
            return ResponseEntity.status(HttpStatus.OK).body(list);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred while joining the room");
        }
    }

    @GetMapping("/room/isAuthor/{room-code}")
    public boolean isAuthorOfRoom(@PathVariable("room-code") String code) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserPrincipal up = (UserPrincipal) authentication.getPrincipal();
        KaRoom kaRoom = roomService.findRoomByPinCode(code);
        if (kaRoom.getAppUser().getUserId() == up.getId()) {
            return true;
        }
        return false;
    }

    @GetMapping("/get-questions/{room-code}")
    public ResponseEntity<?> getQuestionsByRoomCode(@PathVariable("room-code") String roomCode) {
        try {
            List<KaQuestionProjection> questions = roomService.getQuestionsByRoomCode(roomCode);
            if (questions.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ApiResponse(false, "No questions found for the given room code", HttpStatus.NOT_FOUND, null));
            }
            return ResponseEntity.ok(questions);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "An error occurred while retrieving questions", HttpStatus.INTERNAL_SERVER_ERROR, null));
        }
    }

    @PostMapping("/answer/submit")
    public ResponseEntity<String> submitAnswer(@RequestBody KaAnswerDTO kaAnswerDTO) {
        try {
            System.out.println(kaAnswerDTO);
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserPrincipal up = (UserPrincipal) authentication.getPrincipal();

            KaPlayer kaPlayer = kaPlayerRepository.findByUser_UserIdAndRoom_PinCode(up.getId(), kaAnswerDTO.getPinCode());
            if (kaPlayer == null) {
                throw new RuntimeException("Player not found");
            }
//
//            KaQuestion kaQuestion = kaQuestionRepository.findKaQuestionById(kaAnswerDTO.getQuestionId()).orElseThrow(() -> new RuntimeException("Question not found"));
//            System.out.println(kaQuestion.getId());

            kaAnswerService.saveKaAnswer(kaAnswerDTO.getQuestionId(), kaAnswerDTO.getQuestionId(), kaAnswerDTO.getCompletionTime(), kaAnswerDTO.getIsTrue(), kaPlayer.getId());
            return ResponseEntity.ok("Answer submitted successfully");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred while joining the room");
        }

    }

    @GetMapping("/ranking/{roomCode}")
    public ResponseEntity<List<Map<String, Object>>> getRankings(@PathVariable("roomCode") String roomCode) {
        Long roomId = kaRoomRepository.findRoomIdByRoomCode(roomCode).orElse(null);
        List<Map<String, Object>> rankings = userRankingService.getRanking(roomId);
        return ResponseEntity.ok(rankings);
    }
}

