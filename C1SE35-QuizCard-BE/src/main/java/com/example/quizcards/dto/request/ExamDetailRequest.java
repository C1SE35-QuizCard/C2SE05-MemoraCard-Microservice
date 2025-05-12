package com.example.quizcards.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExamDetailRequest {

    @NotNull(message = "Id of the test is empty.")
    private Long testId;

    @NotNull(message = "Id of the flashcard is empty.")
    private Long cardId;

    @Size(max = 1850, message = "Max length answer is 1850.")
    private String yourAnswer;

    private Boolean isTrue;
}
