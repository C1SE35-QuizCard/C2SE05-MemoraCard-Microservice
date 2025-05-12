package com.example.quizcards.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class EssayTestRequest {
    @NotNull(message = "Id of the test is empty.")
    private Long testId;

    @NotNull(message = "Id of the card is empty.")
    private Long cardId;

    private String image;
    private String question;
    private String correctAnswer;
}
