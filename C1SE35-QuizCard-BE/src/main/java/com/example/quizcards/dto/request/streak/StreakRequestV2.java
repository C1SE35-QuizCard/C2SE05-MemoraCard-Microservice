package com.example.quizcards.dto.request.streak;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class StreakRequestV2 {
    int offsetHours;
    int offsetMinutes;
}
