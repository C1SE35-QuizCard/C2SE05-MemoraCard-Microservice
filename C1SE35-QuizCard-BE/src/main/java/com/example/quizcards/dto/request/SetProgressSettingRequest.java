package com.example.quizcards.dto.request;

import com.example.quizcards.entities.AppUser;
import com.example.quizcards.entities.SetFlashcard;
import com.example.quizcards.entities.SetProgressSetting;
import com.example.quizcards.utils.SRSUtils;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SetProgressSettingRequest {
    Long userId;

    @NotNull
    Long setId;

    @NotNull
    @Min(1L)
    @Max(256)
    @Builder.Default
    @JsonSetter(nulls = Nulls.SKIP)
    Long newCardsPerDay = 10L;

    @NotNull
    @Min(60L)
    Long customIntervalAgainSeconds;

    @NotNull
    @Min(60L)
    Long customIntervalHardSeconds;

    @NotNull
    @Min(60L)
    Long customIntervalGoodSeconds;

    @NotNull
    @Min(60L)
    Long customIntervalEasySeconds;

    @NotNull
    @Min(20L)
    @Builder.Default
    @JsonSetter(nulls = Nulls.SKIP)
    Long minIntervalGap = 20L;

    @NotNull
    @Min(1L)
    @Max(256L)
    @Builder.Default
    @JsonSetter(nulls = Nulls.SKIP)
    Long cardsPerRound = 7L;

    @NotNull
    @Min(5L)
    @Max(95L)
    @Builder.Default
    @JsonSetter(nulls = Nulls.SKIP)
    Integer newCardsRatio = 50;

    @NotNull
    @Min(5L)
    @Max(95L)
    @Builder.Default
    @JsonSetter(nulls = Nulls.SKIP)
    Integer dueCardsRatio = 50;

    @NotNull
    @Builder.Default
    @JsonSetter(nulls = Nulls.SKIP)
    Boolean isAutomaticSelectCard = Boolean.FALSE;

    @NotNull
    @Min(0)
    @Max(1200)
    @Builder.Default
    @JsonSetter(nulls = Nulls.SKIP)
    Integer intervalSecondsCanSkip = 0;

    public static SetProgressSetting toEntity(SetProgressSettingRequest request) {
        if (request == null) {
            return null;
        }

        SRSUtils.validateSettings(request);

        return SetProgressSetting.builder()
                .newCardsPerDay(request.getNewCardsPerDay())
                .cardsPerRound(request.getCardsPerRound())
                .customIntervalAgainSeconds(request.getCustomIntervalAgainSeconds())
                .customIntervalHardSeconds(request.getCustomIntervalHardSeconds())
                .customIntervalGoodSeconds(request.getCustomIntervalGoodSeconds())
                .customIntervalEasySeconds(request.getCustomIntervalEasySeconds())
                .minIntervalGap(request.getMinIntervalGap())
                .isAutomaticSelectCard(request.getIsAutomaticSelectCard())
                .intervalSecondsCanSkip(request.getIntervalSecondsCanSkip())
                .ratioMix(request.getNewCardsRatio() + " " + request.getDueCardsRatio())
                .setFlashcard(SetFlashcard.builder().setId(request.getSetId()).build())
                .appUser(AppUser.builder().userId(request.getUserId()).build())
                .build();
    }

    public static SetProgressSetting update(SetProgressSetting setting, SetProgressSettingRequest request) {
        if (setting == null || request == null) {
            return null;
        }

        SRSUtils.validateSettings(request);

        setting.setNewCardsPerDay(request.getNewCardsPerDay());
        setting.setCardsPerRound(request.getCardsPerRound());
        setting.setCustomIntervalAgainSeconds(request.getCustomIntervalAgainSeconds());
        setting.setCustomIntervalHardSeconds(request.getCustomIntervalHardSeconds());
        setting.setCustomIntervalGoodSeconds(request.getCustomIntervalGoodSeconds());
        setting.setCustomIntervalEasySeconds(request.getCustomIntervalEasySeconds());
        setting.setMinIntervalGap(request.getMinIntervalGap());
        setting.setIsAutomaticSelectCard(request.getIsAutomaticSelectCard());
        setting.setIntervalSecondsCanSkip(request.getIntervalSecondsCanSkip());
        setting.setRatioMix(request.getNewCardsRatio() + " " + request.getDueCardsRatio());

        return setting;
    }
}
