package com.example.quizcards.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FlashcardSettingRequest {
    @NotNull(message = "Set id is required.")
    private Long setId;

    private Long lastCardId;

    private Boolean shuffleMode;

    private Boolean flipCardMode;

    private LocalDateTime lastAccessed;
}
