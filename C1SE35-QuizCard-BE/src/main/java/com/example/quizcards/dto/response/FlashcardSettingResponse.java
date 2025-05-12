package com.example.quizcards.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FlashcardSettingResponse {
    private Long lastCardId;
    private boolean shuffleMode;
    private boolean flipCardMode;
    private LocalDateTime lastAccessed;
    private Long settingId;
    private Long userId;
}
