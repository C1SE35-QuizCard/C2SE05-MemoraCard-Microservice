package com.example.quizcards.helpers.FlashcardHelpers;

import com.example.quizcards.dto.request.FlashcardRequest;


public interface IFlashcardHelpers {
    void handleAddFlashcard(FlashcardRequest request);

    void handleUpdateFlashcard(FlashcardRequest request);

    void handleDeleteFlashcard(Long cardId, Long setId);

    void handleAdminAddFlashcard(FlashcardRequest request, Long userId);

    void handleAdminUpdateFlashcard(FlashcardRequest request, Long userId);

    void handleAdminDeleteFlashcard(Long cardId, Long setId, Long userId);
}
