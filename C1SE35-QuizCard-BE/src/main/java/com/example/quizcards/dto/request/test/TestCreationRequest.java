package com.example.quizcards.dto.request.test;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TestCreationRequest {

    private Long TestId;

    @NotNull(message = "Test Mode Id is required.")
    private Long testModeId;

    @NotNull(message = "Total Question is required.")
    @Min(value = 1)
    private int totalQuestion;

    private int goalScore;

    @NotNull(message = "Remaining Time Question is required.")
    private LocalTime remainingTime;

}
