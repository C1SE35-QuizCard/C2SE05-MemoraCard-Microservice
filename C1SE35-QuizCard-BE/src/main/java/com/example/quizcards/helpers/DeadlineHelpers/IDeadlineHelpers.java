package com.example.quizcards.helpers.DeadlineHelpers;

import com.example.quizcards.dto.request.CollectionParamRequest;
import com.example.quizcards.dto.request.CollectionRequest;
import com.example.quizcards.dto.request.DeadlineReminderRequest;

public interface IDeadlineHelpers {
    void handleDeleteDeadline(Long deadlineId);

    void handleUpdateDeadline(DeadlineReminderRequest request);

    void handleAddDeadline(DeadlineReminderRequest request);

    void handleAdminDeleteDeadline(Long deadlineId, Long userId);

    void handleAdminUpdateDeadline(DeadlineReminderRequest request, Long userId);

    void handleAdminAddDeadline(DeadlineReminderRequest request, Long userId);
}
