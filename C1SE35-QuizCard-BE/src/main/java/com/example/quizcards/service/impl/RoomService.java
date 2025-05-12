package com.example.quizcards.service.impl;

import com.example.quizcards.dto.request.KQRequest;
import com.example.quizcards.dto.request.RefreshCodeRoom;
import com.example.quizcards.dto.request.RoomRequestDTO;
import com.example.quizcards.dto.response.KaQuestionProjection;
import com.example.quizcards.dto.response.UserJoiningInfo;
import com.example.quizcards.entities.KaQuestion;
import com.example.quizcards.entities.KaRoom;
import com.example.quizcards.entities.TestDataPackage.MCQuestion;
import com.example.quizcards.helpers.KQHelpers.IKQHelpers;
import com.example.quizcards.repository.KaPlayerRepository;
import com.example.quizcards.repository.KaQuestionRepository;
import com.example.quizcards.repository.KaRoomRepository;
import com.example.quizcards.security.UserPrincipal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.ResponseBody;

import java.security.SecureRandom;
import java.util.List;
import java.util.Map;

@Service
public class RoomService {
    @Autowired
    private KaQuestionRepository kaQuestionRepository;

    @Autowired
    private KaPlayerRepository kaPlayerRepository;
    @Autowired
    private KaRoomRepository kaRoomRepository;

    @Autowired
    private IKQHelpers qHelpers;

    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

    private static final int PIN_LENGTH = 6;

    public static String generatePinCode() {
        SecureRandom random = new SecureRandom();
        StringBuilder pinCode = new StringBuilder(PIN_LENGTH);

        for (int i = 0; i < PIN_LENGTH; i++) {
            int randomIndex = random.nextInt(CHARACTERS.length());
            pinCode.append(CHARACTERS.charAt(randomIndex));
        }

        return pinCode.toString();
    }

    public String generateUniquePinCode() {
        String pinCode;
        boolean isUnique;

        do {
            pinCode = generatePinCode();
            isUnique = !kaRoomRepository.existsByPinCode(pinCode);
        } while (!isUnique);

        return pinCode;
    }

    public Map<String, Object> createRoom(RoomRequestDTO requestDTO, Long userId) {
        if (requestDTO == null || requestDTO.getRoomName().isEmpty() || userId == null) {
            throw new IllegalArgumentException("Invalid room details or user ID");
        }
        String uniquePinCode = generateUniquePinCode();
        kaRoomRepository.save(
                requestDTO.getRoomName(),
                requestDTO.getSetId(),
                uniquePinCode,
                requestDTO.getMaxJoiners(),
                requestDTO.getNumQuestions(),
                requestDTO.getNumQuestions(),
                requestDTO.getTimePerQuestion(),
                userId
        );
        return kaRoomRepository.findByPinCodeShortDetail(uniquePinCode);
    }

    public Map<String, Object> refreshCode(RefreshCodeRoom requestDTO, Long userId) {
        if (requestDTO == null || requestDTO.getPinCode().isEmpty() || userId == null) {
            throw new IllegalArgumentException("Invalid room details or user ID");
        }
        boolean isExistPinCode = kaRoomRepository.existsByPinCodeAndAppUser_UserId(requestDTO.getPinCode(), userId);
        System.out.println(isExistPinCode);
        if (isExistPinCode) {
            String uniquePinCode = generateUniquePinCode();
            kaRoomRepository.updateRoomPinCode(requestDTO.getPinCode(), uniquePinCode, userId);
            return kaRoomRepository.findByPinCodeShortDetail(uniquePinCode);
        }
        return null;
    }

    public KaRoom findRoomByPinCode(String code) {
        KaRoom room = kaRoomRepository.findByPinCode(code);
        if (room != null) {
            return room;
        }
        return null;
    }

    public void joinRoom(KaRoom room) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserPrincipal up = (UserPrincipal) authentication.getPrincipal();
        kaPlayerRepository.joinRoom(room.getRoomId(), up.getId());
        room.addParticipant();
        kaRoomRepository.save(room);
    }
    public boolean isExistPlayer(String code){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserPrincipal up = (UserPrincipal) authentication.getPrincipal();
        KaRoom room=kaRoomRepository.findByPinCode(code);
        boolean exists= kaPlayerRepository.existsByRoom_RoomIdAndUser_UserId(room.getRoomId(), up.getId());
        return exists;
    }

    public List<UserJoiningInfo> listPlayersInRoom(String code){
        return kaPlayerRepository.findUserJoiningInfoByRoomId(code);

    }

    public List<MCQuestion> createListQuestions(KQRequest request) throws Exception {
        return qHelpers.generateQuestions(request.getSetId(), request.getNumQuiz());
    }
    public void saveKaQuestion(KaQuestion kaQuestion) {
        kaQuestionRepository.save(kaQuestion);
    }
    public List<KaQuestionProjection> getQuestionsByRoomCode(String roomCode) {
        return kaQuestionRepository.findQuestionsByRoomCode(roomCode);
    }
}
