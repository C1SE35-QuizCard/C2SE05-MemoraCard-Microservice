package com.example.quizcards.dto.request;

import com.example.quizcards.dto.OnUpdate;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SetFlashcardRequest {
    @NotNull(message = "Missing set id", groups = {OnUpdate.class})
    private Long setId;

    @NotBlank(message = "Title of the set flashcard is empty.")
    private String title;
    private String descriptionSet;

    private Boolean isApproved;
    private Boolean isAnonymous;
    private Boolean sharingMode;

    private String hashPassword;

    private Long categoryId;

    private Long userId;
}
