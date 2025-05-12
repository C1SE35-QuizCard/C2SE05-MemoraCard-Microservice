package com.example.quizcards.service.impl;

import com.example.quizcards.dto.IProgressDTO;
import com.example.quizcards.entities.AppUser;
import com.example.quizcards.entities.Flashcard;
import com.example.quizcards.entities.TestDataPackage.ESQuestion;
import com.example.quizcards.entities.TestDataPackage.IQuestion;
import com.example.quizcards.entities.TestDataPackage.MCQuestion;
import com.example.quizcards.entities.TestDataPackage.TestData;
import com.example.quizcards.entities.UserProgress;
import com.example.quizcards.helpers.TestHelpers.TestSocketSession;
import com.example.quizcards.repository.ITestDataMongoDbRepo;
import com.example.quizcards.repository.IUserProgressRepository;
import com.ibm.icu.impl.Pair;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TestSubmitServiceImpl {
    ITestDataMongoDbRepo mongoDbRepo;

    MongoTemplate mongoTemplate;

    IUserProgressRepository progressRepo;

    TransactionTemplate txTemplate;

    public TestSubmitServiceImpl(ITestDataMongoDbRepo mongoDbRepo,
                                 MongoTemplate mongoTemplate,
                                 IUserProgressRepository progressRepo,
                                 PlatformTransactionManager transactionManager) {
        this.mongoDbRepo = mongoDbRepo;
        this.mongoTemplate = mongoTemplate;
        this.progressRepo = progressRepo;
        // dùng mặc định bean TransactionManager (JPA) cho TransactionTemplate
        this.txTemplate = new TransactionTemplate(transactionManager);
    }

    @Deprecated
    public void submitTestOld(Long testId) {
        TestData test = mongoDbRepo.findByTestId(testId);
        if (test == null || test.getIsEnded()) {
            return;
        }
        Long setId = test.getSetId();
        ArrayList<UserProgress> progressesUpdate = new ArrayList<>();
        progressesUpdate.ensureCapacity(500);
        List<IProgressDTO> currentProgresses = progressRepo.findUserProgressBySetIdAndUserId(
                setId, test.getUserId()
        );
        Map<Pair<Long, Long>, IProgressDTO> mapProgresses = currentProgresses.stream()
                .collect(Collectors.toMap(p -> Pair.of(p.getUserId(), p.getCardId()),
                        progressDTO -> progressDTO));
        Long numQuestionsTrue = updateProcessAndCountTrueAnswers(mapProgresses, test.getQuestions(),
                progressesUpdate, test.getUserId());
        Query query = new Query(Criteria.where("testId").is(test.getTestId()));
        Update update = new Update().set("isEnded", true).set("numQuestionsTrue", numQuestionsTrue);
        mongoTemplate.updateFirst(query, update, TestData.class);
        progressRepo.saveAll(progressesUpdate);
        TestSocketSession.shutdownTest(test.getTestId());
    }

    @Deprecated
    private Long updateProcessAndCountTrueAnswers(Map<Pair<Long, Long>, IProgressDTO> progressInSet,
                                                  List<IQuestion> questions,
                                                  List<UserProgress> progresses,
                                                  Long userId) {
        long numQuestionsTrue = 0L;
        for (IQuestion question : questions) {
            Boolean isAnswerTrue = null;
            if (question instanceof MCQuestion mc) {
                isAnswerTrue = mc.getAnswerTrue();
            } else if (question instanceof ESQuestion es) {
                isAnswerTrue = es.getAnswerTrue();
            }
            numQuestionsTrue += Boolean.TRUE.equals(isAnswerTrue) ? 1 : 0;
            IProgressDTO progress = progressInSet.get(Pair.of(userId, question.getCardId()));
            UserProgress up = UserProgress.builder()
                    .progressId(progress != null ? progress.getProgressId() : null)
                    .appUser(AppUser.builder().userId(userId).build())
                    .flashcard(Flashcard.builder().cardId(question.getCardId()).build())
                    .progressType(Boolean.TRUE.equals(isAnswerTrue))
                    .isAttention(progress != null ? progress.getIsAttention() : false)
                    .build();
            progresses.add(up);
        }
        return numQuestionsTrue;
    }

    public void submitTest(Long testId) {
        TestData test = mongoDbRepo.findByTestId(testId);
        if (test == null || test.getIsEnded()) {
            return;
        }
        // 2) Cập nhật MySQL trong transaction riêng qua TransactionTemplate
        Long totalTrueQuestions = txTemplate.execute(status -> {
            long countTrue = 0L;
            int batchSize = 350;
            Pageable page = PageRequest.of(0, batchSize);
            Page<UserProgress> slice;

            do {
                slice = progressRepo
                        .findByAppUser_UserIdAndFlashcard_Set_SetId(
                                test.getUserId(), test.getSetId(), page
                        );

                // Map (userId,cardId) → entity
                Map<Pair<Long, Long>, UserProgress> map =
                        slice.getContent().stream()
                                .collect(Collectors.toMap(p -> Pair.of(p.getAppUser().getUserId(),
                                                p.getFlashcard().getCardId()),
                                        progressDTO -> progressDTO));

                // Tính và cập nhật progressType lên entity
                for (IQuestion q : test.getQuestions()) {
                    boolean correct = false;
                    if (q instanceof MCQuestion mc) correct = Boolean.TRUE.equals(mc.getAnswerTrue());
                    else if (q instanceof ESQuestion es) correct = Boolean.TRUE.equals(es.getAnswerTrue());
                    if (correct) countTrue++;

                    Pair<Long, Long> key = Pair.of(test.getUserId(), q.getCardId());
                    UserProgress up = map.get(key);
                    if (up != null) {
                        up.setProgressType(correct);
                    } else {
                        up = UserProgress.builder()
                                .appUser(AppUser.builder().userId(test.getUserId()).build())
                                .flashcard(Flashcard.builder().cardId(q.getCardId()).build())
                                .progressType(correct)
                                .isAttention(false)
                                .build();
                        UserProgress finalUp = up;
                        map.computeIfAbsent(key, k -> finalUp);
                    }
                }

                // Save batch và flush
                progressRepo.saveAll(map.values());
                progressRepo.flush();

                page = page.next();
            } while (!slice.isLast());

            return countTrue;
        });

        Query query = new Query(Criteria.where("testId").is(test.getTestId()));
        Update update = new Update().set("isEnded", true).set("numQuestionsTrue", totalTrueQuestions);
        mongoTemplate.updateFirst(query, update, TestData.class);
        TestSocketSession.shutdownTest(test.getTestId());
    }
}
