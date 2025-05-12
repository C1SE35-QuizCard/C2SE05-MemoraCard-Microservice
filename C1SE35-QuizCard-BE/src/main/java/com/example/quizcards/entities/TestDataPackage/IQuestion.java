package com.example.quizcards.entities.TestDataPackage;

import com.example.quizcards.entities.questionTypes.QTypes;

public interface IQuestion {
    Long getId();
    String getQuestion();
    Long getCardId();

    default IQuestion toResponse() {
        return null;
    }
}
