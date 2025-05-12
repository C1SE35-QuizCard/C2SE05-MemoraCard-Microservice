package com.example.quizcards.dto;

public interface IExamDetailDTO {
    Long getExId();
    String getQuestion();
    String getYourAnswer();
    String getAnswer();
    String getIsTrue();
    Long getTestModeId();
}
