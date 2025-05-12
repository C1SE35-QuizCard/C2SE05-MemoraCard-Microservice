package com.example.quizcards.entities.processEntities.enums;

import java.util.Locale;

public enum ReviewRating {
    Again, Hard, Good, Easy, Known, Unknown;

    public static ReviewRating fromString(String input) {
        if (input == null || input.isBlank()) {
            throw new IllegalArgumentException("ReviewRating không được null hoặc rỗng");
        }
        // 1. Trim khoảng trắng, chuyển toàn bộ về lowercase
        String lower = input.trim().toLowerCase(Locale.ROOT);
        // 2. Viết hoa ký tự đầu
        String normalized = Character.toUpperCase(lower.charAt(0)) + lower.substring(1);
        // 3. Chuyển thành enum (nếu sai tên sẽ ném IllegalArgumentException)
        return ReviewRating.valueOf(normalized);
    }
}