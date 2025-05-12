package com.example.quizcards.helpers.TestHelpers;

import com.example.quizcards.dto.IFlashcardDTO;
import com.example.quizcards.entities.Test;
import com.example.quizcards.entities.TestDataPackage.ESQuestion;
import com.example.quizcards.entities.TestDataPackage.IQuestion;
import com.example.quizcards.entities.TestDataPackage.MCQuestion;
import com.example.quizcards.entities.questionTypes.QTypes;
import com.example.quizcards.entities.role.RoleName;
import com.example.quizcards.exception.AccessDeniedException;
import com.example.quizcards.exception.ResourceNotFoundException;
import com.example.quizcards.helpers.AuthenticationHelpers;
import com.example.quizcards.repository.ITestRepository;
import com.example.quizcards.security.UserPrincipal;
import com.example.quizcards.service.ICustomUserDetailsService;
import com.example.quizcards.utils.ClassifyQuestionHandle;
import com.example.quizcards.utils.DistinctFunction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class TestHelpersImpl implements ITestHelpers {
    @Autowired
    private ICustomUserDetailsService customUserDetailsService;

    @Autowired
    private AuthenticationHelpers authenticationHelpers;

    @Autowired
    private ITestRepository testRepository;

    @Autowired
    private ClassifyQuestionHandle questionHandle;

    public TestHelpersImpl(ICustomUserDetailsService customUserDetailsService,
                           AuthenticationHelpers authenticationHelpers,
                           ITestRepository testRepository) {
        this.customUserDetailsService = customUserDetailsService;
        this.authenticationHelpers = authenticationHelpers;
        this.testRepository = testRepository;
    }

    private void checkTestOwner(Long testId, UserPrincipal up) throws AccessDeniedException, ResourceNotFoundException {
        Test test = testRepository.findById(testId)
                .orElseThrow(() -> new ResourceNotFoundException("Test", "id", testId));

        if (!up.getId().equals(test.getUser().getUserId())) {
            throw new AccessDeniedException("You do not have permission to access this test");
        }
    }

    private void checkCurrentUserOwnerTest(Long testId) {
        Authentication authentication = authenticationHelpers.getAuthenticationAuthenticated();
        UserPrincipal up = (UserPrincipal) authentication.getPrincipal();
        checkTestOwner(testId, up);
    }

    private void checkTestExists(Long testId) throws ResourceNotFoundException {
        Integer testExists = testRepository.countTestsById(testId);

        if (testExists == null || testExists == 0) {
            throw new ResourceNotFoundException("Test", "id", testId);
        }
    }

    public void handleDeleteTest(Long testId) {

        Authentication authentication = authenticationHelpers.getAuthenticationAuthenticated();
        UserPrincipal up = (UserPrincipal) authentication.getPrincipal();
        if (!up.getRolesBaseAuthorities().contains(RoleName.ROLE_ADMIN.name())) {
            checkTestOwner(testId, up);
            checkTestExists(testId);
        }
        checkCurrentUserOwnerTest(testId);
    }

    public void handleAccessTest(Long testId) {
        checkCurrentUserOwnerTest(testId);
    }

    public void handleAdminDeleteTest(Long testId, Long userId) {

    }

    private IQuestion handleCreateAToQMulQuestion(IFlashcardDTO card,
                                                  List<String> questionList,
                                                  Long numQuiz,
                                                  Random random) {
        List<String> wrongQuestion =
                new ArrayList<>(questionList.parallelStream().filter(question -> !question.equalsIgnoreCase(card.getQuestion()))
                        .toList());
        if (!wrongQuestion.isEmpty()) {
            int idx = 0;
            while (idx < 3 && idx < wrongQuestion.size()) {
                int rndIdx = random.nextInt(idx, wrongQuestion.size());
                Collections.swap(wrongQuestion, idx, rndIdx);
                ++idx;
            }
            List<MCQuestion.Answer> answers = new ArrayList<>();
            List<Pair<String, Boolean>> answersPair =
                    new ArrayList<>(wrongQuestion.subList(0, idx).stream().map(
                            str -> Pair.of(str, Boolean.FALSE)
                    ).toList());
            answersPair.add(Pair.of(card.getQuestion(), Boolean.TRUE));
            Collections.shuffle(answersPair);
            for (int i = 0; i < answersPair.size(); i++) {
                answers.add(new MCQuestion.Answer(
                        1L + i, answersPair.get(i).getFirst(), answersPair.get(i).getSecond()
                ));
            }
            MCQuestion mcQuestion = new MCQuestion();
            mcQuestion.setQuestion(card.getAnswer());
            mcQuestion.setCardId(card.getCardId());
            mcQuestion.setId(numQuiz);
            mcQuestion.setAnswerList(answers);
            return mcQuestion;
        }
        return null;
    }

    private IQuestion extractMulQuestion(IFlashcardDTO card,
                                         Long numQuiz) {
        Map<String, Object> extractQuestionData =
                questionHandle.getQuestionData(card.getQuestion(), card.getAnswer());
        if (!extractQuestionData.containsKey("type")) {
            throw new RuntimeException("Missing type");
        }
        if (extractQuestionData.get("type") instanceof String type && type.equalsIgnoreCase(QTypes.MULTIPLE.name())) {
            List<String> optionsList = (List<String>) extractQuestionData.get("options");
            long answerIndex = (long) extractQuestionData.get("answerIndex");
            MCQuestion mcQuestion = new MCQuestion();
            mcQuestion.setQuestion(card.getQuestion());
            mcQuestion.setAnswerList(new ArrayList<>());
            for (int i = 0; i < optionsList.size(); i++) {
                mcQuestion.getAnswerList().add(
                        new MCQuestion.Answer(1L + i, optionsList.get(i), i == answerIndex)
                );
            }
            mcQuestion.setCardId(card.getCardId());
            mcQuestion.setId(numQuiz);
            return mcQuestion;
        }
        return null;
    }

    private IQuestion handleCreateQToAMulQuestion(IFlashcardDTO card,
                                                  List<String> answerList,
                                                  Long numQuiz,
                                                  Random random) {
        IQuestion question = extractMulQuestion(card, numQuiz);
        if (question != null) {
            return question;
        }

        List<String> wrongAnswer =
                new ArrayList<>(answerList.parallelStream().filter(answer -> !answer.equalsIgnoreCase(card.getAnswer()))
                        .toList());
        if (!wrongAnswer.isEmpty()) {
            int idx = 0;
            while (idx < 3 && idx < wrongAnswer.size()) {
                int rndIdx = random.nextInt(idx, wrongAnswer.size());
                Collections.swap(wrongAnswer, idx, rndIdx);
                ++idx;
            }
            List<MCQuestion.Answer> answers = new ArrayList<>();
            List<Pair<String, Boolean>> answersPair =
                    new ArrayList<>(wrongAnswer.subList(0, idx).stream().map(
                            str -> Pair.of(str, Boolean.FALSE)
                    ).toList());
            answersPair.add(Pair.of(card.getAnswer(), Boolean.TRUE));
            Collections.shuffle(answersPair);
            for (int i = 0; i < answersPair.size(); i++) {
                answers.add(new MCQuestion.Answer(
                        1L + i, answersPair.get(i).getFirst(), answersPair.get(i).getSecond()
                ));
            }
            MCQuestion mcQuestion = new MCQuestion();
            mcQuestion.setQuestion(card.getQuestion());
            mcQuestion.setCardId(card.getCardId());
            mcQuestion.setId(numQuiz);
            mcQuestion.setAnswerList(answers);
            return mcQuestion;
        }
        return null;
    }

    private void generateQuestions(List<String> questionList,
                                   List<String> answerList,
                                   List<IQuestion> questions,
                                   List<IFlashcardDTO> cards,
                                   int typeMin,
                                   int typeMax,
                                   Long numberQuestions) {
        Random random = new Random();
        long numQuiz = 1L;
        Set<String> questionUsed = new HashSet<>();
        for (IFlashcardDTO card : cards) {
            if (questions.size() >= numberQuestions) break;
            String sumQALower = card.getQuestion().toLowerCase() + card.getAnswer().toLowerCase();
            if (questionUsed.contains(sumQALower)) {
                continue;
            }
            questionUsed.add(sumQALower);
            int typeQ = random.nextInt(typeMin, typeMax);
            IQuestion question;
            if (typeQ == 0) {
                question = handleCreateQToAMulQuestion(card,
                        answerList, numQuiz, random);
            } else {
                question = handleCreateAToQMulQuestion(card,
                        questionList, numQuiz, random);
            }
            if (question != null) {
                questions.add(question);
                ++numQuiz;
            }
        }
    }

    @Override
    public List<IQuestion> handleCreateMulQuestion(List<IFlashcardDTO> cards, String type, Long numberQuestions) {
        Collections.shuffle(cards);

        ArrayList<IQuestion> questions = new ArrayList<>();
        questions.ensureCapacity(500);

        List<String> questionList = new ArrayList<>();

        List<String> answerList = new ArrayList<>();

        int typeMin = 0;
        int typeMax = 2;

        if (type.equalsIgnoreCase("q-a")) {
            answerList = new ArrayList<>(cards.parallelStream()
                    .filter(DistinctFunction.distinctByKey(IFlashcardDTO::getAnswer))
                    .map(IFlashcardDTO::getAnswer)
                    .toList());
            typeMax = 1;
        } else if (type.equalsIgnoreCase("a-q")) {
            questionList = new ArrayList<>(cards.parallelStream()
                    .filter(DistinctFunction.distinctByKey(IFlashcardDTO::getQuestion))
                    .map(IFlashcardDTO::getQuestion)
                    .toList());
            typeMin = 1;
        } else if (type.equalsIgnoreCase("mix")) {
            questionList = new ArrayList<>(cards.parallelStream()
                    .filter(DistinctFunction.distinctByKey(IFlashcardDTO::getQuestion))
                    .map(IFlashcardDTO::getQuestion)
                    .toList());
            answerList = new ArrayList<>(cards.parallelStream()
                    .filter(DistinctFunction.distinctByKey(IFlashcardDTO::getAnswer))
                    .map(IFlashcardDTO::getAnswer)
                    .toList());
        } else {
            throw new RuntimeException("ERROR: Unknown type.");
        }

        generateQuestions(questionList, answerList, questions, cards, typeMin, typeMax, numberQuestions);
        return questions.parallelStream().filter(
                DistinctFunction.distinctByKey(c -> c.getQuestion().toLowerCase())
        ).toList();
    }

    @Override
    public List<IQuestion> handleCreateEssayQuestion(List<IFlashcardDTO> cards, Long numberQuestions) {
        Collections.shuffle(cards);

        List<IFlashcardDTO> cardsUniqueQ = new ArrayList<>(cards.parallelStream()
                .filter(DistinctFunction.distinctByKey(IFlashcardDTO::getQuestion))
                .toList());

        ArrayList<IQuestion> questions = new ArrayList<>();
        questions.ensureCapacity(500);

        long numQuiz = 1L;

        for (IFlashcardDTO card : cardsUniqueQ) {
            if (questions.size() >= numberQuestions) break;
            questions.add(ESQuestion.builder().id(numQuiz++)
                    .question(card.getQuestion())
                    .answer(card.getAnswer())
                    .cardId(card.getCardId())
                    .build());
        }

        return questions;
    }
}
