package com.example.quizcards.controller;

import com.example.quizcards.dto.request.test.TestSendAnswerRequest;
import com.example.quizcards.entities.TestDataPackage.TestData;
import com.example.quizcards.entities.questionTypes.QTypes;
import com.example.quizcards.helpers.TestHelpers.TestExecutorSession;
import com.example.quizcards.helpers.TestHelpers.TestSocketSession;
import com.example.quizcards.repository.ITestDataMongoDbRepo;
import com.example.quizcards.security.UserPrincipal;
import com.example.quizcards.service.impl.TestSubmitServiceImpl;
import com.example.quizcards.utils.global_vars.TestVars;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@RestController
public class TestSocketController {
    @Autowired
    private ITestDataMongoDbRepo mongoDbRepo;

    @Autowired
    private TestSubmitServiceImpl submitService;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    private void checkTestExpired(TestData test) {
        // check hết hạn
        if (test.getIsEnded() || LocalDateTime.now().isAfter(test.getEndAt())) {
            throw new RuntimeException("Test " + test.getTestId() + " expired");
        }
    }

    private void checkTestCanAccess(TestData test, UserPrincipal up) {
        if (!test.getUserId().equals(up.getId())) {
            throw new SecurityException("Test " + test.getTestId() + " cannot be access by you");
        }
    }

    private void checkTestCanEdit(TestData test,
                                  UserPrincipal up) {
        checkTestCanAccess(test, up);
        checkTestExpired(test);
    }

    private TestData extractTestDataFromSessions(Map<String, Object> sessionAttributes,
                                                 Long testId) {
        if (sessionAttributes == null) {
            throw new RuntimeException("Error: Required session attributes");
        }
        if (sessionAttributes.get(TestVars.TEST_ACCEPT_INFO) == null ||
                !(sessionAttributes.get(TestVars.TEST_ACCEPT_INFO) instanceof TestData obj)) {
            throw new RuntimeException("Error: Cannot find test to accept");
        }
        if (!obj.getTestId().equals(testId)) {
            throw new RuntimeException("Error: Test " + testId + " does not belong to this session");
        }
        return obj;
    }

    private UserPrincipal extractPrincipal(Principal principal) {
        if (!(principal instanceof Authentication auth)) {
            throw new RuntimeException("Error: Required authentication");
        }
        return (UserPrincipal) auth.getPrincipal();
    }

    private void saveMcResult(TestData test, TestSendAnswerRequest request) {
        Query query = new Query(Criteria.where("testId").is(test.getTestId())
                .and("questions.id").is(request.getMulData().getQuestionId()));
        Update update = new Update().set("questions.$.answerId", request.getMulData().getAnswerId());
        mongoTemplate.updateFirst(query, update, TestData.class);
    }

    private void saveEsResult(TestData test, TestSendAnswerRequest request) {
        Document query = new Document("testId", test.getTestId());
        Document pipeline = new Document("$set", new Document("questions", new Document("$map", new Document("input", "$questions")
                .append("as", "q")
                .append("in", new Document("$cond", Arrays.asList(
                        new Document("$eq", Arrays.asList("$$q._id", request.getEsData().getQuestionId())),
                        new Document("$mergeObjects", Arrays.asList(
                                "$$q",
                                new Document("yourAnswer", request.getEsData().getAnswer())
                                        .append("answerTrue", new Document("$eq", Arrays.asList(
                                                new Document("$toLower", request.getEsData().getAnswer()),
                                                new Document("$toLower", "$$q.answer")
                                        )))
                        )),
                        "$$q"
                ))))));

        mongoTemplate.getCollection("test_exams").updateOne(query, Arrays.asList(pipeline));

//        Query query = new Query(Criteria.where("testId").is(test.getTestId())
//                .and("questions.id").is(request.getEsData().getQuestionId()));
//        Update update = new Update().set("questions.$.yourAnswer", request.getEsData().getAnswer());
//        mongoTemplate.updateFirst(query, update, TestData.class);
    }

    private void changeEsAnswerResult(TestData test, TestSendAnswerRequest request) {
        Query query = new Query(Criteria.where("testId").is(test.getTestId())
                .and("questions.id").is(request.getEsData().getQuestionId()));
        Update update = new Update().set("questions.$.answerTrue", request.getEsData().getChangeIsTrue());
        mongoTemplate.updateFirst(query, update, TestData.class);
    }

    @MessageMapping("/send-data-to-test")
    public void sendDataToTest(@Payload TestSendAnswerRequest request,
                               Principal principal,
                               @Header("simpSessionAttributes") Map<String, Object> sessionAttributes) {
        UserPrincipal up = extractPrincipal(principal);
        TestData testData = extractTestDataFromSessions(sessionAttributes, request.getTestId());
        checkTestCanEdit(testData, up);
        // update dữ liệu
        if (testData.getTestModeName().equalsIgnoreCase(QTypes.MULTIPLE.name())) {
            if (request.getMulData() == null) {
                throw new RuntimeException("Test " + request.getTestId() + " must have mul data");
            }
            saveMcResult(testData, request);
        } else if (testData.getTestModeName().equalsIgnoreCase(QTypes.ESSAY.name())) {
            if (request.getEsData() == null) {
                throw new RuntimeException("Test " + request.getTestId() + " must have es data");
            }
            saveEsResult(testData, request);
        }
    }

    @MessageMapping("/change-essay-answer-result")
    public void changeEssayAnswerResult(@Payload TestSendAnswerRequest request,
                                        Principal principal,
                                        @Header("simpSessionAttributes") Map<String, Object> sessionAttributes) {
        UserPrincipal up = extractPrincipal(principal);
        TestData testData = extractTestDataFromSessions(sessionAttributes, request.getTestId());
        checkTestCanEdit(testData, up);
        // update dữ liệu
        if (testData.getTestModeName().equalsIgnoreCase(QTypes.ESSAY.name())) {
            if (request.getEsData() == null) {
                throw new RuntimeException("Test " + request.getTestId() + " must have es data");
            }
            changeEsAnswerResult(testData, request);
        }
    }

    @MessageMapping("/send-list-data-to-test")
    public void sendListDataToTest(@Payload List<TestSendAnswerRequest> requests,
                                   Principal principal,
                                   @Header("simpSessionAttributes") Map<String, Object> sessionAttributes) {
        for (TestSendAnswerRequest request : requests) {
            sendDataToTest(request, principal, sessionAttributes);
        }
    }

    @MessageMapping("/force-submit-test")
    public void forceSubmitTest(@Payload TestSendAnswerRequest request,
                                Principal principal,
                                @Header("simpSessionAttributes") Map<String, Object> sessionAttributes) {
        UserPrincipal up = extractPrincipal(principal);
        TestData testData = extractTestDataFromSessions(sessionAttributes, request.getTestId());
        checkTestCanEdit(testData, up);
        submitService.submitTestOld(testData.getTestId());
        TestExecutorSession.shutdownExecutorByTestId(testData.getTestId());
        TestSocketSession.shutdownTest(testData.getTestId());
    }
}
