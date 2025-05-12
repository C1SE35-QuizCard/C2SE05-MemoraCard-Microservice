package com.example.quizcards.dto.response;

import com.example.quizcards.dto.IFlashcardDTO;
import com.example.quizcards.dto.IUserSRSProgressDTO;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SRSProgressResponse {
    Integer numCardsPerRound;
    Integer numCardsToReview;
    Integer numNewCardsNow;
    Long userSetupNumCardsPerRound;
    List<IUserSRSProgressDTO> progresses;
    List<IFlashcardDTO> cards;
}
