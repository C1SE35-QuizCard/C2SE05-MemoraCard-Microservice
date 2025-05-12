package com.example.quizcards.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public interface ITestDTO {
    Long getTestId();
    Long getTestMode();
    Long getSetId();
    Long getUserId();
    int getTotalQuestion();
    int getGoalScore();
    LocalTime getRemainingTime();
    Boolean isTesting();
    LocalDateTime getCreatedAt();
}
