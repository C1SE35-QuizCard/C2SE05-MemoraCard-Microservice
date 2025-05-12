package com.example.quizcards.helpers.KQHelpers;


import com.example.quizcards.dto.IFlashcardDTO;
import com.example.quizcards.entities.TestDataPackage.IQuestion;
import com.example.quizcards.entities.TestDataPackage.MCQuestion;
import com.example.quizcards.entities.questionTypes.QTypes;
import com.example.quizcards.repository.IFlashcardRepository;
import com.example.quizcards.utils.ClassifyQuestionHandle;
import com.example.quizcards.utils.DistinctFunction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class KQHelpersImpl implements IKQHelpers {

    @Autowired
    private IFlashcardRepository fRepo;

    @Autowired
    private ClassifyQuestionHandle questionHandle;

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
            while (idx < Math.min(3, wrongAnswer.size())) {
                int rndIdx = random.nextInt(idx, wrongAnswer.size());
                Collections.swap(wrongAnswer, idx, rndIdx);
                ++idx;
            }
            List<MCQuestion.Answer> answers = new ArrayList<>(4);
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

    @Override
    public List<MCQuestion> generateQuestions(Long setId, Long numQuiz) throws Exception {
        if (numQuiz < 0) {
            throw new Exception();
        }
        List<IFlashcardDTO> cards = fRepo.findAllFlashcardsBySetId(setId);
        if (cards.isEmpty()) {
            throw new Exception();
        }
        Random random = new Random();
        Collections.shuffle(cards);
        List<String> answerList = new ArrayList<>(cards.parallelStream()
                .filter(DistinctFunction.distinctByKey(IFlashcardDTO::getAnswer))
                .map(IFlashcardDTO::getAnswer)
                .toList());
        List<MCQuestion> questions = new ArrayList<>(500);
        long numQuizId = 1L;
        Set<String> questionUsed = new HashSet<>();
        for (IFlashcardDTO card : cards) {
            if (questions.size() >= numQuiz) break;
            String sumQALower = card.getQuestion().toLowerCase() + card.getAnswer().toLowerCase();
            if (questionUsed.contains(sumQALower)) {
                continue;
            }
            questionUsed.add(sumQALower);
            MCQuestion question = (MCQuestion) handleCreateQToAMulQuestion(card, answerList, numQuizId, random);
            if (question != null) {
                questions.add(question);
                ++numQuizId;
            }
        }
        return questions;
    }
}
