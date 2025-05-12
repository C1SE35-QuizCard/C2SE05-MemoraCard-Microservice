package com.example.quizcards.helpers.SetFlashcardHelpers;

import com.example.quizcards.dto.request.SetFlashcardInitializeRequest;
import com.example.quizcards.dto.request.SetFlashcardRequest;


public interface ISetFlashcardHelpers {
    void handleAddSetFlashcard(SetFlashcardInitializeRequest request);

    void handleUpdateSetFlashcard(SetFlashcardRequest request);

    void handleDeleteSetFlashcard(Long setId);

    void handleAdminAddSetFlashcard(SetFlashcardInitializeRequest request, Long userId);

    void handleAdminUpdateSetFlashcard(SetFlashcardRequest request, Long userId);

    void handleAdminDeleteSetFlashcard(Long setId, Long userId);
}
