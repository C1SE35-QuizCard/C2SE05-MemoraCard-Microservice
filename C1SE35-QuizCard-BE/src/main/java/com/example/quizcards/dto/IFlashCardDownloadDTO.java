package com.example.quizcards.dto;

public interface IFlashCardDownloadDTO {
    Long getCardId();
    String getQuestion();
    String getAnswer();
    String getImageUrl();
}
