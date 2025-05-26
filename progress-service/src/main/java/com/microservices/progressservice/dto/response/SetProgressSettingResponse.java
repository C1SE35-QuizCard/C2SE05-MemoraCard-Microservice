package com.microservices.progressservice.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SetProgressSettingResponse {
    Long id;
    Long setId;
    Long userId;
    Long newCardsPerDay;
    Long customIntervalAgainSeconds;
    Long customIntervalHardSeconds;
    Long customIntervalGoodSeconds;
    Long customIntervalEasySeconds;
    Long minIntervalGap;
    Long currentSimpleModeVersion;
    Long currentSrsVersion;
    Integer newCardsRatio;
    Integer dueCardsRatio;
    Boolean isAutomaticSelectCard;
    Integer intervalSecondsCanSkip;
    Long cardsPerRound;
}