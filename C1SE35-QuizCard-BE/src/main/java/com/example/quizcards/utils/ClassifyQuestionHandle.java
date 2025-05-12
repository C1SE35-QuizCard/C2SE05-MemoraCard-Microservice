package com.example.quizcards.utils;

import com.example.quizcards.entities.questionTypes.QTypes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ClassifyQuestionHandle {
    @Autowired
    private ClassifyAndDecompressQuestion classifyMultipleHandle;

    public Map<String, Object> getQuestionData(String question, String answer) {
        Map<String, Object> questionData = classifyMultipleHandle.classifyAndExtract(question);
        Map<String, Object> result = new HashMap<>();
        if (questionData.get("type") == null) {
            return null;
        }
        String questionType = QTypes.ESSAY.getType();
        List<String> optionsList = new ArrayList<>();
        long answerIndex = 0L;
        if (!questionData.containsKey("type")) {
            throw new RuntimeException("Missing type");
        }
        if (questionData.get("type").equals(QTypes.MULTIPLE.name())) {
            Object options = questionData.get("options");
            if (!(options instanceof List)) {
                return null;
            }
            optionsList = (List<String>) options;
            for (String option : optionsList) {
//                if (HandleString.removeAllNewLineAndExtraSpace(option).equalsIgnoreCase(
//                        HandleString.removeAllNewLineAndExtraSpace(answer))) {
//                    questionType = QTypes.MULTIPLE.getType();
//                    break;
//                }
                if (option.equalsIgnoreCase(answer)) {
                    questionType = QTypes.MULTIPLE.getType();
                    break;
                }
                ++answerIndex;
            }
        }
        result.put("type", questionType);
        result.put("question", question);
        result.put("answer", answer);
        if (questionType.equals(QTypes.MULTIPLE.getType())) {
            result.put("options", optionsList);
            result.put("answerIndex", answerIndex);
        }
        return result;
    }
}
