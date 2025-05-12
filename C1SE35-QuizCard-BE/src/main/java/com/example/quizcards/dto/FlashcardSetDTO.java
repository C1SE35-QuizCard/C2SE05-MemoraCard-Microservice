package com.example.quizcards.dto;

public interface FlashcardSetDTO {
    Long getSetId();
    String getTitle();
    Boolean getIsAnonymous();
    Long getUserId();
    String getAvatar();
    String getUserName();
    Long getTotalCard();
    Long getUserInteractionCount();
}
