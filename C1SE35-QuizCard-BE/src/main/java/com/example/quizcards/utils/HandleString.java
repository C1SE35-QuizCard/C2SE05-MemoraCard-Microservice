package com.example.quizcards.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HandleString {
    private static final Pattern answerPattern = Pattern.compile("^(\\d+[-.)]|[A-Za-z][-.)])\\s*");

    public static String popExtraNewLineAndSpace(String text) {
        return text.replaceAll("^[\\s\\n\\r]+", "").replaceAll("[\\s\\n\\r]+$", "");
    }

    public static String removeAllNewLineAndExtraSpace(String text) {
        // Convert new lines (\n, \r\n, \r) to spaces and trim leading/trailing spaces
        String result = text.replaceAll("[\\r\\n]+", " ").trim();

        // Replace multiple spaces with a single space
        result = result.replaceAll("\\s{2,}", " ");

        return result;
    }

    public static String trimStart(String input) {
        if (input == null) {
            return null;
        }
        return input.replaceAll("^\\s+", ""); // ^\\s+ là regex để match khoảng trắng đầu chuỗi
    }

    public static String trimEnd(String input) {
        if (input == null) {
            return null;
        }
        return input.replaceAll("\\s+$", ""); // \\s+$ là regex để match khoảng trắng cuối chuỗi
    }

    public static String popResult(String result) {
        result = HandleString.popExtraNewLineAndSpace(result);
        Matcher matcher = answerPattern.matcher(result);
        if (matcher.find()) {
            result = result.substring(matcher.end()); // Loại bỏ phần đầu
        }

        return result.replaceAll("(?m)^(\\s*\\n)+", "")
                .replaceAll("(?m)(\\s*\\n)+$", "");
    }
}
