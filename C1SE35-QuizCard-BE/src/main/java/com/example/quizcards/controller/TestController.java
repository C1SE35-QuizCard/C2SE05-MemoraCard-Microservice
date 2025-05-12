package com.example.quizcards.controller;

import com.example.quizcards.dto.request.test.TestCreationRequest;
import com.example.quizcards.dto.request.test.TestRequest;
import com.example.quizcards.security.UserPrincipal;
import com.example.quizcards.service.ITestService;
import jakarta.transaction.NotSupportedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;


@CrossOrigin(origins = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
@RestController
@RequestMapping("/v1/test")
public class TestController {
    @Autowired
    private ITestService testService;

    @PostMapping("/create/{set_id}")
    @PreAuthorize("hasAnyRole('ROLE_FREE_USER', 'ROLE_PREMIUM_USER')")
    public ResponseEntity<?> createTest(@PathVariable("set_id") Long setId,
                                               @RequestBody @Validated TestCreationRequest request){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserPrincipal up = (UserPrincipal) authentication.getPrincipal();
        return testService.createTest(setId, up.getId(), request);
    }

    @DeleteMapping("/delete/{test_id}")
    public ResponseEntity<?> deleteTest(@PathVariable("test_id") Long TestId) {
        return testService.deleteTest(TestId);
    }

    @GetMapping("/multiple-choice/{test_id}")
    @PreAuthorize("hasAnyRole('ROLE_FREE_USER', 'ROLE_PREMIUM_USER')")
    public ResponseEntity<?> getMultipleChoiceTest(@PathVariable("test_id") Long testId) {
        return testService.createMultipleChoiceTest(testId);
    }

    @GetMapping("/essay/{test_id}")
    @PreAuthorize("hasAnyRole('ROLE_FREE_USER', 'ROLE_PREMIUM_USER')")
    public ResponseEntity<?> getEssayTest(@PathVariable("test_id") Long testId) {
        return testService.createEssayTest(testId);
    }

    @PostMapping("/create-new-test")
    @PreAuthorize("hasAnyRole('ROLE_FREE_USER', 'ROLE_PREMIUM_USER')")
    public ResponseEntity<?> createNewTestWithUserAndSetNotDone(@Validated @RequestBody TestRequest request)
            throws NotSupportedException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserPrincipal up = (UserPrincipal) auth.getPrincipal();
        return testService.createTestBySetInUser(up, request);
    }

    @GetMapping("/get-all-test")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN')")
    public ResponseEntity<?> getAllTests() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserPrincipal up = (UserPrincipal) auth.getPrincipal();
        return testService.getAllTests();
    }

    @GetMapping("/get-all-by-user-id/{userId}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN')")
    public ResponseEntity<?> getAllTestsByUserId(@PathVariable("userId") Long userId) {
        return testService.getTestByUserId(userId);
    }

    @GetMapping("/get-all-by-set-id/{setId}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN')")
    public ResponseEntity<?> getAllTestsBySetId(@PathVariable("setId") Long setId) {
        return testService.getTestBySetId(setId);
    }

    @GetMapping("/user/get-all")
    @PreAuthorize("hasAnyRole('ROLE_FREE_USER', 'ROLE_PREMIUM_USER')")
    public ResponseEntity<?> getAllTestsBySetIdInUser() {
        return testService.getTestByUserIdInUser();
    }

    @GetMapping("/user/get-detail-by-test-id/{testId}")
    @PreAuthorize("hasAnyRole('ROLE_FREE_USER', 'ROLE_PREMIUM_USER')")
    public ResponseEntity<?> getDetailTestByIdInUser(@PathVariable("testId") Long testId) {
        return testService.getDetailsTestByTestIdInUser(testId);
    }

    @GetMapping("/user/get-detail-by-test-id-without-question/{testId}")
    @PreAuthorize("hasAnyRole('ROLE_FREE_USER', 'ROLE_PREMIUM_USER')")
    public ResponseEntity<?> getDetailTestByIdWithoutQuestionsInUser(@PathVariable("testId") Long testId) {
        return testService.getDetailsTestByTestIdWithoutQuestionsInUser(testId);
    }

    @PostMapping("/reset-test-not-done/{testId}")
    @PreAuthorize("hasAnyRole('ROLE_FREE_USER', 'ROLE_PREMIUM_USER')")
    public ResponseEntity<?> resetTestingNotDone(@PathVariable("testId") Long testId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserPrincipal up = (UserPrincipal) auth.getPrincipal();
        return null;
    }
}
