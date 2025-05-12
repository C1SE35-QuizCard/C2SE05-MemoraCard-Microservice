package com.example.quizcards.dto;

public interface IProgressAnalysisDTO {
    Long getSetId();
    String getSetTitle();
    Long getTotalCardRecall();
    Long getTotalCardRemember();
    Long getTotalCardNotLearn();
}
