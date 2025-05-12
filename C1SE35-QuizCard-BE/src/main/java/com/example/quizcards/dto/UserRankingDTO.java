package com.example.quizcards.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserRankingDTO {
    private Long userId;
    private Long roomId;
    private Long totalQuestions;
    private Long totalCorrect;
    private Long totalTime;
    private Integer totalRanking;
}
