package com.example.quizcards.service;

import com.example.quizcards.dto.request.test.TestCreationRequest;
import com.example.quizcards.dto.request.test.TestRequest;
import com.example.quizcards.security.UserPrincipal;
import jakarta.transaction.NotSupportedException;
import org.springframework.http.ResponseEntity;

public interface ITestService {
    ResponseEntity<?> createTest(Long SetId,
                                 Long UserId,
                                 TestCreationRequest request);

    ResponseEntity<?> deleteTest(Long TestId);

    ResponseEntity<?> createMultipleChoiceTest(Long testId);

    ResponseEntity<?> createEssayTest(Long testId);

    ResponseEntity<?> createTestBySetInUser(UserPrincipal up,
                                            TestRequest request) throws NotSupportedException;

    ResponseEntity<?> getAllTests();

    ResponseEntity<?> getTestByUserId(Long userId);

    ResponseEntity<?> getTestBySetId(Long SetId);

    ResponseEntity<?> getDetailsTestByTestIdInUser(Long userId);

    ResponseEntity<?> getDetailsTestByTestIdWithoutQuestionsInUser(Long testId);

    ResponseEntity<?> getTestByUserIdInUser();
}
