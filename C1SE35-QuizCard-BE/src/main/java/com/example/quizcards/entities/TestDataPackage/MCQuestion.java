package com.example.quizcards.entities.TestDataPackage;

import com.example.quizcards.dto.response.TestDataResponse;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.index.Indexed;

import java.util.List;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MCQuestion implements IQuestion {
    @Indexed
    private Long id; // ID của câu hỏi
    private String question;
    private List<Answer> answerList;
    private Long answerId;

    private Long cardId;

    @Transient
    public Boolean getAnswerTrue() {
        if (answerId == null || answerList == null || answerList.isEmpty()) {
            return false;
        }
        for (Answer answer : answerList) {
            if (answer.getId().equals(answerId) && answer.isTrue()) {
                return true;
            }
        }
        return false;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Answer {
        @Indexed
        private Long id; // ID của câu trả lời
        private String answer;
        private boolean isTrue;
    }

    @Override
    public TestDataResponse.MCQResponse toResponse() {
        return TestDataResponse.MCQResponse.builder()
                .id(this.id)
                .question(this.question)
                .answers(this.answerList.stream()
                        .map(a -> TestDataResponse.MCQResponse.Answer.builder()
                                .id(a.getId())
                                .answer(a.getAnswer())
                                .build()
                        ).collect(Collectors.toList()))
                .answerId(this.answerId)
                .build();
    }
}

