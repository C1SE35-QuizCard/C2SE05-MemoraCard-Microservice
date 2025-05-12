package com.example.quizcards.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeadlineReminderRequest {
    private Long deadlineRemindersId;

    @NotNull(message = "Reminder time cannot be null.")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSSSSS", timezone = "UTC")
    private Timestamp reminderTime;

    private Long userId;
    private Long setId;
}
