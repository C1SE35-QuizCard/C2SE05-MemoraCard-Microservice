package com.example.quizcards.service.impl;

import com.example.quizcards.dto.IFlashcardDTO;
import com.example.quizcards.dto.ITestModeDTO;
import com.example.quizcards.dto.request.EssayTestRequest;
import com.example.quizcards.dto.request.MultipleChoiceTestRequest;
import com.example.quizcards.dto.request.test.TestCreationRequest;
import com.example.quizcards.dto.request.test.TestRequest;
import com.example.quizcards.dto.response.ApiResponse;
import com.example.quizcards.dto.response.TestDataResponse;
import com.example.quizcards.entities.AppUser;
import com.example.quizcards.entities.SetFlashcard;
import com.example.quizcards.entities.Test;
import com.example.quizcards.entities.TestDataPackage.IQuestion;
import com.example.quizcards.entities.TestDataPackage.TestData;
import com.example.quizcards.entities.TestMode;
import com.example.quizcards.entities.questionTypes.QTypes;
import com.example.quizcards.exception.AccessDeniedException;
import com.example.quizcards.exception.BadRequestException;
import com.example.quizcards.exception.ResourceNotFoundException;
import com.example.quizcards.helpers.TestHelpers.ITestHelpers;
import com.example.quizcards.helpers.TestHelpers.TestSocketSession;
import com.example.quizcards.repository.IFlashcardRepository;
import com.example.quizcards.repository.ISetFlashcardRepository;
import com.example.quizcards.repository.ITestDataMongoDbRepo;
import com.example.quizcards.repository.ITestRepository;
import com.example.quizcards.security.UserPrincipal;
import com.example.quizcards.service.ISequenceGeneratorService;
import com.example.quizcards.service.ISetFlashcardService;
import com.example.quizcards.service.ITestModeService;
import com.example.quizcards.service.ITestService;
import jakarta.transaction.NotSupportedException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class TestServiceImpl implements ITestService {
    @Autowired
    private ITestRepository testRepository;

    @Autowired
    private ISetFlashcardService setFlashcardService;

    @Autowired
    private ITestModeService testModeService;

    @Autowired
    private ITestHelpers testHelpers;

    @Autowired
    private ISetFlashcardRepository setRepository;

    @Autowired
    private IFlashcardRepository flashcardRepository;

    @Autowired
    private ITestDataMongoDbRepo testMongoRepo;

    @Autowired
    private ISequenceGeneratorService sequenceGeneratorService;

    @Autowired
    private TestScheduleRegisterServiceImpl scheduleRegisterService;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public ResponseEntity<?> createTest(Long setId, Long userId, TestCreationRequest request) {
        long flashcardCount = setFlashcardService.countFlashcardsBySetId(setId);
        TestMode testMode = TestMode.builder().testModeId(request.getTestModeId()).build();
        AppUser au = AppUser.builder().userId(userId).build();
        SetFlashcard set = SetFlashcard.builder().setId(setId).build();

        if (testModeService.exists(request.getTestModeId()) < 1) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(false, "Test mode ID " + request.getTestModeId() + " does not exist."));
        }
        if (request.getTotalQuestion() > flashcardCount) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(false, "Total questions cannot exceed the number of available flashcards: " + flashcardCount));
        }
        if (request.getTotalQuestion() < request.getGoalScore()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(false, "Total questions (" + request.getTotalQuestion() + ") cannot be less than the goal score (" + request.getGoalScore() + ")."));
        }
        Test test = new Test();
        test.setTestMode(testMode);
        test.setUser(au);
        test.setSetFlashcards(set);
        test.setTotalQuestion(request.getTotalQuestion());
        test.setGoalScore(request.getGoalScore());
        test.setRemainingTime(request.getRemainingTime());
        test.setIsTesting(true);
        test.setCreatedAt(LocalDateTime.now());

        testRepository.save(test);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse(true, "Test created successfully", HttpStatus.CREATED, test.getTestId()));
    }


    @Override
    public ResponseEntity<?> deleteTest(Long TestId) {
        Test test = testRepository.findTestId(TestId);
        testHelpers.handleDeleteTest(TestId);
        testRepository.delete(test);
        TestData testData = testMongoRepo.findByTestIdWithoutQuestions(test.getTestId());
        if (testData != null) {
            testMongoRepo.delete(testData);
        }
        return ResponseEntity.status(HttpStatus.OK)
                .body(new ApiResponse(true, "Test deleted successfully"));
    }

    @Override
    public ResponseEntity<?> createMultipleChoiceTest(Long testId) {
        Test test = testRepository.findById(testId).orElseThrow();
        List<IFlashcardDTO> flashcards = flashcardRepository.findAllFlashcardsBySetId(test.getSetFlashcards().getSetId());
        List<MultipleChoiceTestRequest> questions = new ArrayList<>();

        Collections.shuffle(flashcards);

        // Chọn số lượng câu hỏi cần thiết
        List<IFlashcardDTO> selectedFlashcards = flashcards.subList(0, test.getTotalQuestion());

        for (IFlashcardDTO flashcard : selectedFlashcards) {
            MultipleChoiceTestRequest question = new MultipleChoiceTestRequest();
            question.setTestId(test.getTestId());
            question.setCardId(flashcard.getCardId());
            question.setQuestion(flashcard.getQuestion());
            question.setCorrectAnswer(flashcard.getAnswer());

            List<String> inCorrectAnswers = new ArrayList<>();

            for (IFlashcardDTO otherFlashcard : flashcards) {
                if (!otherFlashcard.getCardId().equals(flashcard.getCardId())) {
                    inCorrectAnswers.add(otherFlashcard.getAnswer());
                }
            }

            if (inCorrectAnswers.size() > 3) {
                Collections.shuffle(inCorrectAnswers);
                inCorrectAnswers = inCorrectAnswers.subList(0, 3);
            }

            Collections.shuffle(inCorrectAnswers);
            question.setIncorrectAnswers(inCorrectAnswers);
            questions.add(question);
        }
        return ResponseEntity.ok(questions);
    }

    @Override
    public ResponseEntity<?> createEssayTest(Long testId) {
        Test test = testRepository.findById(testId).orElseThrow();
        List<IFlashcardDTO> flashcards = flashcardRepository.findAllFlashcardsBySetId(test.getSetFlashcards().getSetId());
        List<EssayTestRequest> questions = new ArrayList<>();

        Collections.shuffle(flashcards);

        List<IFlashcardDTO> selectedFlashcards = flashcards.subList(0, test.getTotalQuestion());

        for (IFlashcardDTO flashcard : selectedFlashcards) {
            EssayTestRequest question = new EssayTestRequest();
            question.setTestId(test.getTestId());
            question.setCardId(flashcard.getCardId());
            question.setQuestion(flashcard.getQuestion());
            question.setCorrectAnswer(flashcard.getAnswer());

            questions.add(question);
        }
        return ResponseEntity.ok(questions);
    }

    private TestDataResponse convertFromTestDataWithNoResult(Test testDb,
                                                             TestData test) {
        if (test.getQuestions() == null) {
            test.setQuestions(new ArrayList<>());
        }

        return TestDataResponse.builder()
                .testId(testDb.getTestId()).testModeId(testDb.getTestMode().getTestModeId())
                .testModeName(testDb.getTestMode().getTestModeName()).setId(testDb.getSetFlashcards().getSetId())
                .setTitle(testDb.getSetFlashcards().getTitle()).userId(testDb.getUser().getUserId())
                .username(testDb.getUser().getUsername()).avatar(testDb.getUser().getAvatar())
                .firstName(testDb.getUser().getFirstName()).lastName(testDb.getUser().getLastName())
                .goalScore(test.getGoalScore()).totalScore(test.getTotalQuestions()).createdAt(test.getCreatedAt())
                .endAt(test.getEndAt()).updatedAt(test.getUpdatedAt()).isEnded(test.getIsEnded())
                .questions(
                        test.getQuestions().stream()
                                .map(IQuestion::toResponse)
                                .filter(Objects::nonNull)
                                .collect(Collectors.toList())
                )
                .build();
    }

    private TestDataResponse convertFromTestData(Test testDb,
                                                 TestData test) {
        if (test.getQuestions() == null) {
            test.setQuestions(new ArrayList<>());
        }
        return TestDataResponse.builder()
                .testId(testDb.getTestId()).testModeId(testDb.getTestMode().getTestModeId())
                .setId(testDb.getSetFlashcards().getSetId()).setTitle(testDb.getSetFlashcards().getTitle())
                .userId(testDb.getUser().getUserId()).username(testDb.getUser().getUsername())
                .avatar(testDb.getUser().getAvatar()).firstName(testDb.getUser().getFirstName())
                .lastName(testDb.getUser().getLastName()).goalScore(test.getGoalScore())
                .totalScore(test.getTotalQuestions()).createdAt(test.getCreatedAt())
                .endAt(test.getEndAt()).updatedAt(test.getUpdatedAt()).questions(test.getQuestions())
                .testModeName(testDb.getTestMode().getTestModeName()).isEnded(test.getIsEnded()).build();
    }

    private TestData buildTestData(Long userId, TestRequest request, String idMongoRecord) {
        TestData test = new TestData();
        test.setUserId(userId);
        test.setSetId(request.getSetId());
        test.setTestModeId(request.getTestModeId());
        test.setTestModeName(request.getTestModeName());

        List<IFlashcardDTO> cards = flashcardRepository.findAllFlashcardsBySetId(request.getSetId());
        List<IQuestion> questions;
        if (request.getTestModeName().equalsIgnoreCase(QTypes.MULTIPLE.name())) {
            questions = testHelpers.handleCreateMulQuestion(cards,
                    request.getTypeOfMultipleChoice(), request.getTotalQuestion());
        } else if (request.getTestModeName().equalsIgnoreCase(QTypes.ESSAY.name())) {
            questions = testHelpers.handleCreateEssayQuestion(cards, request.getTotalQuestion());
        } else {
            throw new RuntimeException("Error when creating questions");
        }

        test.setQuestions(questions);

        long goalScore = request.getGoalScore();

        if (questions.size() < request.getTotalQuestion()) {
            goalScore =
                    (long) Math.floor(request.getGoalScore() * 1.0 * questions.size() / request.getTotalQuestion());
            goalScore = Math.max(goalScore, 1L);
        }

        test.setGoalScore(goalScore);

        test.setTotalQuestions((long) questions.size());

        test.setCreatedAt(LocalDateTime.now());

        test.setEndAt(LocalDateTime.now()
                .plusHours(request.getRemainingTime().getHour())
                .plusMinutes(request.getRemainingTime().getMinute())
                .plusSeconds(request.getRemainingTime().getSecond()));

        test.setIsEnded(false);

        test.setTestId(request.getTestId() == null ?
                sequenceGeneratorService.getNextSequence("test_exams", "testId") : request.getTestId());

        if (idMongoRecord != null) test.setId(idMongoRecord);

        return test;
    }

    private void shutdownOldTestNotEnded(Long userId, Long setId) {
        List<TestData> activeTests = testMongoRepo.findIdTestNotEnded(userId,
                setId,
                LocalDateTime.now());

        Query query = new Query(
                Criteria.where("userId").is(userId)
                        .and("setId").is(setId)
                        .andOperator(
                                new Criteria().orOperator(
                                        Criteria.where("isEnded").is(false),
                                        Criteria.where("endAt").gt(LocalDateTime.now())
                                )
                        )
        );

        Update update = new Update().set("isEnded", true);

        mongoTemplate.updateMulti(query, update, TestData.class);

        for (TestData testData : activeTests) {
            TestSocketSession.shutdownTest(testData.getTestId());
        }
    }

    private ResponseEntity<?> generateTestByRequest(Test testDb, TestRequest r, String id) {
        validateTestRequest(r, setFlashcardService.countFlashcardsBySetId(r.getSetId()));
        r.setTestId(testDb.getTestId());
        r.setTestModeName(testDb.getTestMode().getTestModeName());
        TestData data = buildTestData(testDb.getUser().getUserId(), r, id);
        if (data.getQuestions() == null || data.getQuestions().isEmpty())
            return ResponseEntity.unprocessableEntity().body(new ApiResponse(false, "System cannot create the question base"));
        shutdownOldTestNotEnded(testDb.getUser().getUserId(), r.getSetId());
        testMongoRepo.save(data);
        scheduleRegisterService.setupSubmitExecutor(data.getTestId(), data.getEndAt());
        return ResponseEntity.ok(convertFromTestDataWithNoResult(testDb, data));
    }

    private void validateTestRequest(TestRequest r, long flashcardCount) {
        if (r.getGoalScore() == 0) throw new BadRequestException("Number of goals cannot be zero.");
        if (r.getTotalQuestion() < r.getGoalScore())
            throw new BadRequestException(String.format("Total questions (%d) cannot be less than goal score (%d).", r.getTotalQuestion(), r.getGoalScore()));
        if (r.getTotalQuestion() > flashcardCount)
            throw new BadRequestException(String.format("Total questions cannot exceed available flashcards: (%d)", flashcardCount));
    }

    @Override
    @Transactional
    public ResponseEntity<?> createTestBySetInUser(UserPrincipal up, TestRequest r) throws NotSupportedException {
        ITestModeDTO mode = testModeService.findAllTestMode().stream()
                .filter(m -> m.getTestModeId().equals(r.getTestModeId())).findFirst()
                .orElseThrow(() -> new BadRequestException("Test mode ID " + r.getTestModeId() + " does not exist."));
        if (!setRepository.existsById(r.getSetId()))
            throw new ResourceNotFoundException("Set", "id", r.getSetId());

        Test test = Test.builder()
                .testMode(TestMode.builder().testModeId(r.getTestModeId())
                        .testModeName(mode.getTestModeName()).build())
                .user(AppUser.builder().userId(up.getId()).build())
                .setFlashcards(SetFlashcard.builder().setId(r.getSetId()).build())
                .totalQuestion(0).goalScore(0).createdAt(LocalDateTime.now()).build();
        testRepository.save(test);
        if (Stream.of(QTypes.MULTIPLE, QTypes.ESSAY)
                .map(Enum::name)
                .anyMatch(name -> name.equalsIgnoreCase(mode.getTestModeName()))) {
            return generateTestByRequest(test, r, null);
        }
        throw new NotSupportedException("Unknown type of test mode.");
    }

    @Override
    public ResponseEntity<?> getAllTests() {
        List<Test> tests = testRepository.findAll();
        Map<Long, TestData> testDatas = testMongoRepo.findAllWithoutQuestions().stream()
                .collect(Collectors.toMap(TestData::getTestId, v -> v));
        return ResponseEntity.ok()
                .body(tests.stream()
                        .map(t -> convertFromTestData(t, testDatas.get(t.getTestId()))));
    }

    @Override
    public ResponseEntity<?> getTestByUserId(Long userId) {
        return null;
    }

    @Override
    public ResponseEntity<?> getTestBySetId(Long SetId) {
        return null;
    }

    @Override
    public ResponseEntity<?> getDetailsTestByTestIdInUser(Long testId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserPrincipal up = (UserPrincipal) auth.getPrincipal();
        Test test = testRepository.findById(testId)
                .orElseThrow(() -> new ResourceNotFoundException("Test", "id", testId));
        TestData activeTest = testMongoRepo.findByTestId(testId);
        if (activeTest == null) {
            throw new ResourceNotFoundException("Data test", "id", testId);
        }
        if (!activeTest.getUserId().equals(up.getId())) {
            throw new AccessDeniedException("Cannot access to this details set by you");
        }
        if (activeTest.getIsEnded() || LocalDateTime.now().isAfter(activeTest.getEndAt())) {
            return ResponseEntity.ok().body(convertFromTestData(test, activeTest));
        }
        return ResponseEntity.ok().body(convertFromTestDataWithNoResult(test, activeTest));
    }

    @Override
    public ResponseEntity<?> getDetailsTestByTestIdWithoutQuestionsInUser(Long testId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserPrincipal up = (UserPrincipal) auth.getPrincipal();
        Test test = testRepository.findById(testId)
                .orElseThrow(() -> new ResourceNotFoundException("Test", "id", testId));
        TestData activeTest = testMongoRepo.findByTestIdWithoutQuestions(test.getTestId());
        if (activeTest == null) {
            throw new ResourceNotFoundException("Data test", "id", testId);
        }
        if (!activeTest.getUserId().equals(up.getId())) {
            throw new AccessDeniedException("Cannot access to this details set by you");
        }
        return ResponseEntity.ok().body(convertFromTestData(test, activeTest));
    }

    @Override
    public ResponseEntity<?> getTestByUserIdInUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserPrincipal up = (UserPrincipal) auth.getPrincipal();
        List<Test> tests = testRepository.findByUser(AppUser.builder().userId(up.getId()).build());
        Map<Long, TestData> testDatas = testMongoRepo.findByUserIdWithoutQuestions(up.getId()).stream()
                .collect(Collectors.toMap(TestData::getTestId, v -> v));
        List<TestDataResponse> responses =
                tests.stream().map(t -> convertFromTestData(t, testDatas.get(t.getTestId())))
                        .collect(Collectors.toList());
        return ResponseEntity.ok().body(responses);
    }

}
