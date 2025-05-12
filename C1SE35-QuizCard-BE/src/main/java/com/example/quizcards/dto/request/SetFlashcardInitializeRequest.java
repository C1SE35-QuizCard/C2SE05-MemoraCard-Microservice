package com.example.quizcards.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SetFlashcardInitializeRequest {
    @NotBlank(message = "Title of the set flashcard is empty.")
    private String title;
    private String descriptionSet;
    private Boolean isAnonymous;
    private Boolean sharingMode;

    private String hashPassword;

    @NotNull(message = "Missing category.")
    private Long categoryId;

    @NotNull
    @Size(min = 2, message = "Need at least two flashcards for create set.")
    private List<FlashcardRequest> flashcards;
}
