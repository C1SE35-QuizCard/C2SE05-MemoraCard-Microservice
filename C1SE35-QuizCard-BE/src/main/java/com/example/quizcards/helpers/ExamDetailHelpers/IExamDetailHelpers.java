package com.example.quizcards.helpers.ExamDetailHelpers;

import com.example.quizcards.dto.request.ExamDetailRequest;
import com.example.quizcards.dto.request.FlashcardRequest;

public interface IExamDetailHelpers {
    void handleDeleteExamDetail(Long examDetailId);

    void handleUpdateExamDetail(Long examDetailId);

    void handleAdminDeleteExamDetail(Long examDetailId, Long userId);
}
