package com.example.quizcards.dto;

import com.example.quizcards.entities.AppUser;
import com.example.quizcards.entities.Flashcard;
import com.example.quizcards.entities.UserProgress;

import java.time.Instant;

public interface IProgressDTO {
    Long    getProgressId();
    Boolean getProgressType();
    Boolean getIsAttention();
    Long    getUserId();
    Long    getCardId();
    Integer getConsecutiveCorrectSimpleMode();
    Integer getCountConsecutiveHardPress();
    Long    getProgressVersion();
    Instant getUpdatedAt();

    static UserProgress toUserProgress(IProgressDTO dto) {
        return UserProgress.builder()
                .progressId(dto.getProgressId())
                .progressType(dto.getProgressType())
                .isAttention(dto.getIsAttention())
                .appUser(AppUser.builder().userId(dto.getUserId()).build())
                .flashcard(Flashcard.builder().cardId(dto.getCardId()).build())
                .consecutiveCorrectSimpleMode(dto.getConsecutiveCorrectSimpleMode())
                .countConsecutiveHardPress(dto.getCountConsecutiveHardPress())
                .modeVersion(dto.getProgressVersion())
                .build();
    }
}
