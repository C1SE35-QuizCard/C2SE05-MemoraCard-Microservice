package com.microservices.scheduleservice.dto;

import java.time.LocalDate;

public interface UserWithStreakAnalysisDTO {
    Long getUserId();

    String getUsername();

    String userTz();

    Long getAnalysisId();

    Long getCurrentStreak();

    Long getMaxStreak();

    LocalDate getLastUpdated();
}
