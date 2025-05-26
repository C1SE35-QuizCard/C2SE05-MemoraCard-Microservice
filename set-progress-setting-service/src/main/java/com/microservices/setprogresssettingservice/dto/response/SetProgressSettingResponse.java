package com.microservices.setprogresssettingservice.dto.response;

import com.microservices.setprogresssettingservice.model.SetProgressSetting;
import com.microservices.setprogresssettingservice.utils.SRSUtils;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

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

    public static SetProgressSettingResponse from(SetProgressSetting setting) {
        if (setting == null) {
            return null;
        }
        List<Integer> ratios = SRSUtils.parseRatioMix(setting.getRatioMix());

        return SetProgressSettingResponse.builder()
                .id(setting.getId())
                .setId(setting.getSetFlashcardId())
                .userId(setting.getAppUserId())
                .newCardsPerDay(setting.getNewCardsPerDay())
                .customIntervalAgainSeconds(setting.getCustomIntervalAgainSeconds())
                .customIntervalHardSeconds(setting.getCustomIntervalHardSeconds())
                .customIntervalGoodSeconds(setting.getCustomIntervalGoodSeconds())
                .customIntervalEasySeconds(setting.getCustomIntervalEasySeconds())
                .minIntervalGap(setting.getMinIntervalGap())
                .currentSimpleModeVersion(setting.getCurrentSimpleModeVersion())
                .currentSrsVersion(setting.getCurrentSrsVersion())
                .newCardsRatio(ratios.getFirst())
                .dueCardsRatio(ratios.getLast())
                .isAutomaticSelectCard(setting.getIsAutomaticSelectCard())
                .intervalSecondsCanSkip(setting.getIntervalSecondsCanSkip())
                .cardsPerRound(setting.getCardsPerRound())
                .build();
    }
}