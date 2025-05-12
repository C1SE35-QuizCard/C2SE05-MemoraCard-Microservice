package com.example.quizcards.entities.TestDataPackage;

import com.example.quizcards.dto.response.TestDataResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.index.Indexed;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ESQuestion implements IQuestion {
    @Indexed
    private Long id; // ID của câu hỏi
    private String question;
    private String answer; // Câu trả lời cho câu hỏi tự luận
    private String yourAnswer;

    private Long cardId;

    private Boolean answerTrue;

    public Boolean getAnswerTrue() {
        if (answerTrue != null) {
            return answerTrue;
        }
        if (question == null || answer == null || yourAnswer == null) {
            return false;
        }
        return answerTrue = answer.equalsIgnoreCase(yourAnswer);
    }

    @Override
    public TestDataResponse.ESQResponse toResponse() {
        return TestDataResponse.ESQResponse.builder()
                .id(this.id)
                .question(this.question)
                .yourAnswer(this.yourAnswer)
                .build();
    }
}
