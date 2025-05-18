package com.microservices.streakscheduleservice.service;

import com.microservices.streakscheduleservice.dto.InitStreakResultDTO;
import com.microservices.streakscheduleservice.repository.AppUserRepository;
import com.microservices.streakscheduleservice.repository.StreakDetailRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuples;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class InitStreakActivityImpl implements InitStreakActivity {
    StreakDetailRepository detailRepo;
    AppUserRepository userRepo;

    @Override
    public InitStreakResultDTO init(long userId) {
        return userRepo.findWithStreakAnalysisByUserId(userId)
                .map(data -> {
                    System.out.println("InitStreakActivityImpl fetch data: " + data);
                    // Lấy thời gian hiện tại dựa trên múi giờ của user
                    LocalDate timeFromClient = Instant.now()
                            .atOffset(ZoneOffset.of(data.userTz()))
                            .toLocalDate();

                    // Nếu không có analysisId, trả về null và streak = 0
                    if (data.getAnalysisId() == null) {
                        return new InitStreakResultDTO(LocalDate.MIN, 0L);
                    }

                    LocalDate lastDateLearned = data.getLastUpdated();
                    // Kiểm tra nếu cách hơn 1 ngày, trả về null và streak = 0
                    if (timeFromClient.isAfter(lastDateLearned.plusDays(1))) {
                        return new InitStreakResultDTO(LocalDate.MIN, 0L);
                    }

                    System.out.println("Done fetch data, continuing...");
                    // Trả về ngày hiện tại và streak hiện tại nếu streak tiếp tục
                    return new InitStreakResultDTO(timeFromClient, data.getCurrentStreak());
                })
                .switchIfEmpty(Mono.just(new InitStreakResultDTO(LocalDate.MIN, 0L)))
                .onErrorResume(e -> Mono.just(new InitStreakResultDTO(LocalDate.MIN, 0L)))
                .block();
    }
}
