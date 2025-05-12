package com.example.quizcards.dto;

public interface TopUserDTO {
    Long getUserId();
    String getFirstName();
    String getLastName();
    String getUserName();
    String getAvatar();
    Long getTotalSets();  // Trường này chứa tổng số bộ flashcard
}
