package com.example.quizcards.dto;


import java.sql.Timestamp;

public interface IDeadlineReminderDTO {
    Long getDeadlineRemindersId();

    Timestamp getReminderTime();

    Long getUserId();

    Long getSetId();

    String getTitle();

    int getCardCount();
}