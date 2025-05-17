package com.microservices.scheduleservice.service;

import com.microservices.scheduleservice.repository.AppUserRepository;
import com.microservices.scheduleservice.repository.StreakDetailRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
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
    public Tuple2<LocalDate, Long> init(long userId) {
        return userRepo.findWithStreakAnalysisByUserId(userId)
                .map(data -> {
                    // Lấy thời gian hiện tại dựa trên múi giờ của user
                    LocalDate timeFromClient = Instant.now()
                            .atOffset(ZoneOffset.of(data.userTz()))
                            .toLocalDate();

                    // Nếu không có analysisId, trả về null và streak = 0
                    if (data.getAnalysisId() == null) {
                        return Tuples.of(LocalDate.MIN, 0L);
                    }

                    LocalDate lastDateLearned = data.getLastUpdated();
                    // Kiểm tra nếu cách hơn 1 ngày, trả về null và streak = 0
                    if (timeFromClient.isAfter(lastDateLearned.plusDays(1))) {
                        return Tuples.of(LocalDate.MIN, 0L);
                    }

                    // Trả về ngày hiện tại và streak hiện tại nếu streak tiếp tục
                    return Tuples.of(timeFromClient, data.getCurrentStreak());
                })
                .switchIfEmpty(Mono.just(Tuples.of(LocalDate.MIN, 0L)))
                .onErrorResume(e -> Mono.just(Tuples.of(LocalDate.MIN, 0L)))
                .block();
    }
}
