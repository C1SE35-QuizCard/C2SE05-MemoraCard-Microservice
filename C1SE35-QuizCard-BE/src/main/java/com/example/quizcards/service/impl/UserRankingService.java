package com.example.quizcards.service.impl;

import com.example.quizcards.repository.KaRoomRepository;
import com.example.quizcards.repository.UserRankingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UserRankingService {

    @Autowired
    private UserRankingRepository userRankingRepository;

    public List<Map<String, Object>> getRanking(Long roomId) {
        // Thực hiện insert dữ liệu trước
        userRankingRepository.insertData(roomId);

        // Lấy kết quả xếp hạng
        return userRankingRepository.getRanking(roomId);
    }
}
