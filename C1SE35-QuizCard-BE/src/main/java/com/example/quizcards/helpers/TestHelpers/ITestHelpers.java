package com.example.quizcards.helpers.TestHelpers;

import com.example.quizcards.dto.IFlashcardDTO;
import com.example.quizcards.entities.TestDataPackage.IQuestion;
import com.example.quizcards.entities.TestDataPackage.TestData;

import java.util.List;

public interface ITestHelpers {

    void handleDeleteTest(Long testId);

    void handleAccessTest(Long testId);

    void handleAdminDeleteTest(Long testId, Long userId);

    List<IQuestion> handleCreateMulQuestion(List<IFlashcardDTO> cards, String type, Long numberQuestions);

    List<IQuestion> handleCreateEssayQuestion(List<IFlashcardDTO> cards, Long numberQuestions);
}
