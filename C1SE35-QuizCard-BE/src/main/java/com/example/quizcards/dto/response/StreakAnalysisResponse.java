package com.example.quizcards.dto.response;

import com.example.quizcards.dto.StreakStatus;
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

    List<StreakStatus> streakOneWeek;
}
