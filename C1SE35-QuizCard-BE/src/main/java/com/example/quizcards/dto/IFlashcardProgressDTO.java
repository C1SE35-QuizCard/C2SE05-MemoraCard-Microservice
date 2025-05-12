package com.example.quizcards.dto;
public interface IFlashcardProgressDTO {
    Long getSetId();
    String getTitle();
    String getAvatar();
    String getUserName();
    Long getCardId();
    String getQuestion();
    String getAnswer();
    Boolean getStatusProgress();
    Boolean getStatusMark();
    String getImageUrl();
}

