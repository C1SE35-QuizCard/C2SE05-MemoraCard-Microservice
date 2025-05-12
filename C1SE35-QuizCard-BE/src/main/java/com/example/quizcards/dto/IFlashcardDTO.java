package com.example.quizcards.dto;

import java.time.LocalDateTime;
import lombok.*;
import lombok.experimental.FieldDefaults;

public interface IFlashcardDTO {
    Long getCardId();

    String getQuestion();

    String getAnswer();

    String getImageUrl();

    Boolean getIsApproved();

    LocalDateTime getCreatedAt();

    LocalDateTime getUpdatedAt();

    String getTitle();

//    @Data
//    @Builder
//    @NoArgsConstructor
//    @AllArgsConstructor
//    @FieldDefaults(level = AccessLevel.PRIVATE)
//    NO CODE: class FlashcardDTO implements IFlashcardDTO {
//        Long cardId;
//        String question;
//        String answer;
//        String imageUrl;
//        Boolean isApproved;
//        LocalDateTime createdAt;
//        LocalDateTime updatedAt;
//        String title;
//        Long userId;
//        String userName;
//        Long setId;
//        String setTitle;
//
//        public FlashcardDTO(Long cardId,
//                            Long userId,
//                            Long setId) {
//            this.cardId = cardId;
//            this.userId = userId;
//            this.setId = setId;
//        }
//    }
}
