package com.example.quizcards.dto;

public interface ISrsProgressAnalysisDTO {
    Long getSetId();
    String getSetTitle();
    Long getNumNewCards();
    Long getNumLearningCards();
    Long getNumAlmostDoneCards();
    Long getNumMasteredCards();
}
