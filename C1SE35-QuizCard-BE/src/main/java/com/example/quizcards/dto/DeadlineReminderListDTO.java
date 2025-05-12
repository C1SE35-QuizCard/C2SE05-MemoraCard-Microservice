package com.example.quizcards.dto;

import java.time.LocalDateTime;

public interface DeadlineReminderListDTO {
    Long getDeadlineRemindersId();
    Long getSetId();
    String getTitle();
    Integer getCardCount();
    LocalDateTime getReminderTime();
}
