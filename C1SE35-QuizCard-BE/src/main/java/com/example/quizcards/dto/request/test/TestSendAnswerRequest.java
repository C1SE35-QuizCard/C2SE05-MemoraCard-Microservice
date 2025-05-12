package com.example.quizcards.dto.request.test;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TestSendAnswerRequest {
    @NotNull
    private Long testId;

    private Long userId;

    private String type;

    private MCData mulData;

    private ESData esData;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MCData {
        private Long questionId;
        private Long answerId;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ESData {
        private Long questionId;
        private String answer;
        private Boolean changeIsTrue;
    }
}
