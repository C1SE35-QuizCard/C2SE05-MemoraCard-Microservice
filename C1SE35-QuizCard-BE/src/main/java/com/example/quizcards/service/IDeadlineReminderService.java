package com.example.quizcards.service;

import com.example.quizcards.dto.DeadlineReminderListDTO;
import com.example.quizcards.dto.IDeadlineReminderDTO;
import com.example.quizcards.dto.request.DeadlineReminderRequest;

import java.sql.Timestamp;
import java.util.List;

public interface IDeadlineReminderService {
    IDeadlineReminderDTO getDeadlineReminderById(Long id);

    List<DeadlineReminderListDTO> getDeadlineReminderByUserId(Long userId);

    List<IDeadlineReminderDTO> getDeadlineReminderBySetId(Long setId);

    void addDeadlineReminder(Timestamp reminderTime, Long userId, Long setId);

    void deleteDeadlineReminder(Long deadlineRemindersId);

    void updateDeadlineReminder(DeadlineReminderRequest request);

    void addDeadlineReminder_2(DeadlineReminderRequest request);

    void deleteDeadlineReminder_2(Long deadlineRemindersId);

    void updateDeadlineReminder_2(DeadlineReminderRequest request);
//    boolean existsByUserIdAndSetId(Long userId, Long setId);
//    boolean existsByUserIdAndSetIdAndNotId(Long userId, Long setId, Long deadlineRemindersId);
}
