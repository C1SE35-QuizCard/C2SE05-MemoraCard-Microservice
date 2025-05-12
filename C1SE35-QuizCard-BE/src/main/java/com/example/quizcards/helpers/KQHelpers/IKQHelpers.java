package com.example.quizcards.helpers.KQHelpers;

import com.example.quizcards.entities.TestDataPackage.MCQuestion;

import java.util.List;

public interface IKQHelpers {
    List<MCQuestion> generateQuestions(Long setId, Long numQuiz) throws Exception;
}
