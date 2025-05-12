package com.example.quizcards.dto.request.test;

import com.example.quizcards.validation.TimeRange;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TestRequest {
    private Long testId;

    @NotNull(message = "Test Mode Id is required.")
    private Long testModeId;

    @NotNull(message = "Total Question is required.")
    @Min(value = 1)
    private Long totalQuestion;

    private Long goalScore;

    private Long userId;

    @NotNull(message = "Set id cannot be null.")
    private Long setId;

    private String typeOfMultipleChoice;

    @NotNull(message = "Remaining Time Question is required.")
    @TimeRange(minMinute = 5, maxHour = 5)
    private LocalTime remainingTime;

    private String testModeName;
}
