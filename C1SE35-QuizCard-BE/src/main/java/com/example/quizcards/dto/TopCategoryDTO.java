package com.example.quizcards.dto;

public interface TopCategoryDTO {
    Long getCategoryId();
    String getCategoryName();
    Long getTotalSets();  // Trường này chứa tổng số bộ flashcard
}
