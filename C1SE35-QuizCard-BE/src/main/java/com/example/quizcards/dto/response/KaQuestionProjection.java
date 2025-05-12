package com.example.quizcards.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public interface KaQuestionProjection {
    @JsonProperty("ka_question_id")
    Long getId();

    @JsonProperty("room_id")
    Long getRoomId();

    @JsonProperty("answer_list")
    String getAnswerList();

    String getQuestion();
}
