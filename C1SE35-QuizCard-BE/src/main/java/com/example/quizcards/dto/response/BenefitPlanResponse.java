package com.example.quizcards.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BenefitPlanResponse {
    private Integer maxSetsFlashcards;
    private Integer maxFlashcardsPerSet;
    private Integer maxSetsPerDay;
    private Integer maxRoomsCreatePerDay;
    private Integer maxTermsPerRoom;
    private int expiredMonth;
}
