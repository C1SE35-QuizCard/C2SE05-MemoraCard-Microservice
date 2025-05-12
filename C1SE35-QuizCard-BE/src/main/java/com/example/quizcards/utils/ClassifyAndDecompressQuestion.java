package com.example.quizcards.utils;

import com.example.quizcards.entities.questionTypes.QTypes;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@Service
public class ClassifyAndDecompressQuestion {
    // Bị bẫy ở chỗ
    /*
     * Question: Abc
     * A. AAAA
     * BNNNN
     * B. AAAA
     * C. DDD
     * EEEE
     * Answer:
     * A. AAAA
     *
     *
     * BNNNN
     * */
    public Map<String, Object> classifyAndExtract(String input) {
        Map<String, Object> result = new HashMap<>();
        List<String> options = new ArrayList<>();
        StringBuilder currentAnswer = new StringBuilder();
        StringBuilder question = new StringBuilder();
        boolean foundAnswer = false;

        // Tách dòng và làm sạch khoảng trắng
        String[] lines = input.split("\n");

        // Regex nhận diện đáp án
        Pattern answerPattern = Pattern.compile("^(\\d+[-.)]|[A-Za-z][-.)]).*");
//        Pattern answerPattern = Pattern.compile("^(\\p{Nd}+[-.)]|\\p{L}+[-.)]).*");

        for (String line : lines) {
//            line = line.trim(); // Loại bỏ khoảng trắng thừa
            String lineAfterTrim = line.trim();
            if (lineAfterTrim.isEmpty()) {
                if (foundAnswer) {
                    currentAnswer.append("\n").append(line);
                }
                // chưa thấy đáp án -> đang parse câu hỏi -> thêm dòng.
                if (!question.isEmpty() && !foundAnswer && currentAnswer.isEmpty()) {
                    question.append("\n").append(line);
                }
                continue;
            }

            boolean isAnswer = answerPattern.matcher(lineAfterTrim).matches();

            if (question.isEmpty()) {
                if (!isAnswer) {
                    question = new StringBuilder(lineAfterTrim); // Gán dòng làm câu hỏi
                    continue;
                } else {
                    result.put("type", QTypes.ESSAY.name()); // Không có câu hỏi, trả về tự luận
                    return result;
                }
            } else {
                // chưa thấy đáp án -> đang parse câu hỏi -> không phải định dạng câu hỏi -> thêm dòng lẫn line .
                if (!foundAnswer && currentAnswer.isEmpty() && !isAnswer) {
                    question.append("\n").append(line);
                    continue;
                }
            }

            // Nếu dòng khớp với định dạng đáp án
            if (isAnswer) {
                if (options.size() >= 4) {
                    // đã vượt quá 4 câu trả lời -> không phải multiple choice -> trả về tự luận
                    result.put("type", QTypes.ESSAY.name());
                    return result;
                }
                parseCurrentAnswer(options, currentAnswer);
                currentAnswer = new StringBuilder(line); // Bắt đầu đáp án mới
                foundAnswer = true;
            } else if (foundAnswer) {
                // Nếu dòng không khớp định dạng và đã bắt đầu thu thập đáp án
                currentAnswer.append("\n").append(line);
            } else {
                // Nếu không tìm thấy câu hỏi hợp lệ trước đó
                result.put("type", QTypes.ESSAY.name());
                return result;
            }
        }

        // Thêm đáp án cuối cùng (nếu có)
        if (foundAnswer && !currentAnswer.isEmpty()) {
            if (options.size() >= 4) {
                // đã vượt quá 4 câu trả lời -> không phải multiple choice -> trả về tự luận
                result.put("type", QTypes.ESSAY.name());
                return result;
            }
            parseCurrentAnswer(options, currentAnswer);
        }

        // Phân loại dựa trên câu hỏi và số lượng đáp án
        if (!question.isEmpty() && options.size() >= 2) {
            result.put("type", QTypes.MULTIPLE.name());
            result.put("question", HandleString.popExtraNewLineAndSpace(question.toString()));
            result.put("options", options);
        } else {
            result.put("type", QTypes.ESSAY.name());
        }

        return result;
    }

    private void parseCurrentAnswer(List<String> options, StringBuilder currentAnswer) {
        String tempAnswer = HandleString.popExtraNewLineAndSpace(currentAnswer.toString());
        if (!tempAnswer.isEmpty()) {
            options.add(tempAnswer); // Thêm đáp án trước đó vào danh sách
        }
    }
}
