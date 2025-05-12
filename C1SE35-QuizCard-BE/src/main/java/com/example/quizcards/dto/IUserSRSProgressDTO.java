package com.example.quizcards.dto;

import com.example.quizcards.entities.AppUser;
import com.example.quizcards.entities.Flashcard;
import com.example.quizcards.entities.UserSRSProgress;
import com.example.quizcards.entities.processEntities.enums.CardState;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

public interface IUserSRSProgressDTO {
    Long   getId();
    Long   getCardId();
    Long   getUserId();
    String getCardState();
    Long   getCurrentInterval();
    Float  getEaseFactor();
    Integer getLapses();
    Integer getLearningStep();
    Instant getLastReviewTime();
    Instant getDueDate();
    Integer getConsecutiveCorrect();
    Integer getConsecutiveIncorrect();
    Long    getOldNextIntervalAgain();
    Long    getOldNextIntervalHard();
    Long    getOldNextIntervalGood();
    Long    getOldNextIntervalEasy();
    Long    getSrsVersion();
    Instant getCreatedAt();
    Instant getUpdatedAt();

    // Build UserSRSProgressDTO from UserSRSProgress
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    class UserSRSProgressDTO implements IUserSRSProgressDTO {
        private Long id;
        private Long cardId;
        private Long userId;
        private String cardState;
        private Long currentInterval;
        private Float easeFactor;
        private Integer lapses;
        private Integer learningStep;
        private Instant lastReviewTime;
        private Instant dueDate;
        private Integer consecutiveCorrect;
        private Integer consecutiveIncorrect;
        private Long oldNextIntervalAgain;
        private Long oldNextIntervalHard;
        private Long oldNextIntervalGood;
        private Long oldNextIntervalEasy;
        private Long srsVersion;
        private Instant createdAt;
        private Instant updatedAt;


        public static UserSRSProgressDTO clone(IUserSRSProgressDTO progress) {
            return UserSRSProgressDTO.builder()
                    .id(progress.getId())
                    .cardId(progress.getCardId())
                    .userId(progress.getUserId())
                    .cardState(progress.getCardState())
                    .currentInterval(progress.getCurrentInterval())
                    .easeFactor(progress.getEaseFactor())
                    .lapses(progress.getLapses())
                    .learningStep(progress.getLearningStep())
                    .lastReviewTime(progress.getLastReviewTime())
                    .dueDate(progress.getDueDate())
                    .consecutiveCorrect(progress.getConsecutiveCorrect())
                    .consecutiveIncorrect(progress.getConsecutiveIncorrect())
                    .oldNextIntervalAgain(progress.getOldNextIntervalAgain())
                    .oldNextIntervalHard(progress.getOldNextIntervalHard())
                    .oldNextIntervalGood(progress.getOldNextIntervalGood())
                    .oldNextIntervalEasy(progress.getOldNextIntervalEasy())
                    .srsVersion(progress.getSrsVersion())
                    .createdAt(progress.getCreatedAt())
                    .updatedAt(progress.getUpdatedAt())
                    .build();
        }
    }

    static UserSRSProgress toUserProgressWithSRS(IUserSRSProgressDTO dto) {
        return UserSRSProgress.builder()
                .id(dto.getId())
                .flashcard(Flashcard.builder().cardId(dto.getCardId()).build())
                .appUser(AppUser.builder().userId(dto.getUserId()).build())
                .cardState(CardState.valueOf(dto.getCardState()))
                .currentInterval(dto.getCurrentInterval())
                .easeFactor(dto.getEaseFactor())
                .lapses(dto.getLapses())
                .learningStep(dto.getLearningStep())
                .lastReviewTime(dto.getLastReviewTime())
                .dueDate(dto.getDueDate())
                .consecutiveCorrect(dto.getConsecutiveCorrect())
                .consecutiveIncorrect(dto.getConsecutiveIncorrect())
                .oldNextIntervalAgain(dto.getOldNextIntervalAgain())
                .oldNextIntervalHard(dto.getOldNextIntervalHard())
                .oldNextIntervalGood(dto.getOldNextIntervalGood())
                .oldNextIntervalEasy(dto.getOldNextIntervalEasy())
                .srsVersion(dto.getSrsVersion())
                .createdAt(dto.getCreatedAt())
                .updatedAt(dto.getUpdatedAt())
                .build();
    }
}
