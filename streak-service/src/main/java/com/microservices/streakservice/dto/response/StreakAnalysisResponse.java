package com.microservices.streakservice.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class StreakAnalysisResponse {
    Long currentStreak;
    Long longestStreak;
    Long dayLearned;
    Boolean isCurrentDateLearned;

    List<com.microservices.streakservice.dto.StreakStatus> streakOneWeek;
}
