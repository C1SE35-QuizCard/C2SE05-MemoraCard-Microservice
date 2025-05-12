package com.example.quizcards.dto.response;

import com.example.quizcards.entities.SetProgressSetting;
import com.example.quizcards.utils.SRSUtils;
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
                .setId(setting.getSetFlashcard().getSetId())
                .userId(setting.getAppUser().getUserId())
                .newCardsPerDay(setting.getNewCardsPerDay())
                .customIntervalAgainSeconds(setting.getCustomIntervalAgainSeconds())
                .customIntervalHardSeconds(setting.getCustomIntervalHardSeconds())
                .customIntervalGoodSeconds(setting.getCustomIntervalGoodSeconds())
                .customIntervalEasySeconds(setting.getCustomIntervalEasySeconds())
                .minIntervalGap(setting.getMinIntervalGap())
                .currentSrsVersion(setting.getCurrentSimpleModeVersion())
                .newCardsRatio(ratios.getFirst())
                .dueCardsRatio(ratios.getLast())
                .isAutomaticSelectCard(setting.getIsAutomaticSelectCard())
                .intervalSecondsCanSkip(setting.getIntervalSecondsCanSkip())
                .cardsPerRound(setting.getCardsPerRound())
                .build();
    }
}
