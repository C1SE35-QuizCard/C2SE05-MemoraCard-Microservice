package com.example.quizcards.dto;

import java.util.List;

public interface IMultipleChoiceTestDTO {
    Long getTestId();
    Long getCardId();
    String getImage();
    String getQuestion();
    String correctAnswer();
    List<String> incorrectAnswers();
}
