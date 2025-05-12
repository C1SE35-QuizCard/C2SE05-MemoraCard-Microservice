package com.example.quizcards.dto.response;

import com.example.quizcards.entities.TestDataPackage.IQuestion;
import com.example.quizcards.entities.TestDataPackage.MCQuestion;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TestDataResponse {
    private Long testId;
    private Long testModeId;
    private String testModeName;
    private Long goalScore;
    private Long totalScore;
    private Long numberQuestionsTrue;
    private Long setId;
    private String setTitle;
    private Long userId;
    private String username;
    private String avatar;
    private String firstName;
    private String lastName;
    private LocalDateTime createdAt;
    private LocalDateTime endAt;
    private LocalDateTime updatedAt;
    private Boolean isEnded;
    private List<IQuestion> questions;

    private Long numQuestionsTrue;
    private Long numQuestions;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MCQResponse implements IQuestion {
        private Long id;
        private String question;
        private List<Answer> answers;
        private Long answerId;

        private Long cardId;

        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        @Builder
        public static class Answer {
            private Long id;
            private String answer;
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ESQResponse implements IQuestion {
        private Long id;
        private String question;
        private String yourAnswer;
        private Long cardId;
    }
}
