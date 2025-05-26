package com.microservices.setprogresssettingservice.model;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("set_progress_setting")
public class SetProgressSetting {
    @Id
    @Column("sm_id")
    private Long id;

    // Khóa ngoại sang set_flashcards
    @Column("set_id")
    private Long setFlashcardId;

    // Khóa ngoại sang app_user
    @Column("user_id")
    private Long appUserId;

    @Builder.Default
    @Column("new_cards_per_day")
    @NotNull
    @Min(1)
    @Max(256)
    private Long newCardsPerDay = 10L;

    @Builder.Default
    @Column("custom_interval_again_seconds")
    @NotNull
    private Long customIntervalAgainSeconds = 60L;

    @Builder.Default
    @Column("custom_interval_hard_seconds")
    @NotNull
    private Long customIntervalHardSeconds = 600L;

    @Builder.Default
    @Column("custom_interval_good_seconds")
    @NotNull
    private Long customIntervalGoodSeconds = 3600L;

    @Builder.Default
    @Column("custom_interval_easy_seconds")
    @NotNull
    private Long customIntervalEasySeconds = 43200L;

    @Builder.Default
    @Column("min_interval_gap")
    @NotNull
    @Min(20)
    private Long minIntervalGap = 30L;

    @Builder.Default
    @Column("current_simple_mode_version")
    @NotNull
    private Long currentSimpleModeVersion = 1L;

    @Builder.Default
    @Column("current_srs_version")
    @NotNull
    private Long currentSrsVersion = 1L;

    @Builder.Default
    @Column("learning_steps")
    @NotNull
    @Size(max = 40)
    private String learningSteps = "60 620";

    @Builder.Default
    @Column("relearning_steps")
    @NotNull
    @Size(max = 40)
    private String relearningSteps = "700";

    @Builder.Default
    @Column("lapse_new_interval_factor")
    @NotNull
    private Float lapseNewIntervalFactor = 0.0f;

    @Builder.Default
    @Column("sm2_interval_modifier")
    @NotNull
    private Float sm2IntervalModifier = 1.0f;

    @Builder.Default
    @Column("sm2_hard_interval_factor")
    @NotNull
    private Float sm2HardIntervalFactor = 1.2f;

    @Builder.Default
    @Column("consecutive_incorrect_penalty_factor_review")
    @NotNull
    private Float consecutiveIncorrectPenaltyFactorReview = 1.0f;

    @Builder.Default
    @Column("consecutive_correct_bonus_factor")
    @NotNull
    private Float consecutiveCorrectBonusFactor = 1.0f;

    @Builder.Default
    @Column("consecutive_incorrect_penalty_factor_learning")
    @NotNull
    private Float consecutiveIncorrectPenaltyFactorLearning = 0.22f;

    @Builder.Default
    @Column("consecutive_incorrect_penalty_factor_relearning")
    @NotNull
    private Float consecutiveIncorrectPenaltyFactorRelearning = 0.25f;

    @Builder.Default
    @Column("ratio_mix")
    @NotNull
    @Size(max = 10)
    private String ratioMix = "50 50";

    @Builder.Default
    @Column("is_automatic_select_card")
    @NotNull
    private Boolean isAutomaticSelectCard = false;

    @Builder.Default
    @Column("interval_seconds_can_skip")
    @NotNull
    @Min(0)
    @Max(1200)
    private Integer intervalSecondsCanSkip = 0;

    @Builder.Default
    @Column("card_per_round")
    @NotNull
    @Min(1)
    @Max(256)
    private Long cardsPerRound = 7L;
}