package com.example.quizcards.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Entity
@Table(name = "set_progress_setting", indexes = {
        @Index(name = "idx_sps_user_id_set_id", columnList = "user_id, set_id"),
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SetProgressSetting {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "sm_id", nullable = false)
    Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "set_id", nullable = false)
    SetFlashcard setFlashcard;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    AppUser appUser;

    @Builder.Default
    @Column(name = "new_cards_per_day", nullable = false)
    @Min(1)
    @Max(256)
    Long newCardsPerDay = 10L;

    @Builder.Default
    @Column(name = "custom_interval_again_seconds", nullable = false)
    Long customIntervalAgainSeconds = 60L;

    @Builder.Default
    @Column(name = "custom_interval_hard_seconds", nullable = false)
    Long customIntervalHardSeconds = 600L;

    @Builder.Default
    @Column(name = "custom_interval_good_seconds", nullable = false)
    Long customIntervalGoodSeconds = 3600L;

    @Builder.Default
    @Column(name = "custom_interval_easy_seconds", nullable = false)
    Long customIntervalEasySeconds = 43200L;

    @Builder.Default
    @Column(name = "min_interval_gap", nullable = false)
    @Min(20L)
    Long minIntervalGap = 30L;

    @Builder.Default
    @Column(name = "current_simple_mode_version", nullable = false)
    Long currentSimpleModeVersion = 1L;

    @Builder.Default
    @Column(name = "current_srs_version", nullable = false)
    Long currentSrsVersion = 1L;

    @Builder.Default
    @Column(name = "learning_steps", length = 40, nullable = false)
    String learningSteps = "60 620";

    @Builder.Default
    @Column(name = "relearning_steps", length = 40, nullable = false)
    String relearningSteps = "700";

    @Builder.Default
    @Column(name = "lapse_new_interval_factor", nullable = false)
    Float lapseNewIntervalFactor = 0.0f;

    @Builder.Default
    @Column(name = "sm2_interval_modifier", nullable = false)
    Float sm2IntervalModifier = 1.0f;

    @Builder.Default
    @Column(name = "sm2_hard_interval_factor", nullable = false)
    Float sm2HardIntervalFactor = 1.2f;


    @Builder.Default
    @Column(name = "consecutive_incorrect_penalty_factor_review", nullable = false)
    Float consecutiveIncorrectPenaltyFactorReview = 1.0f;

    @Builder.Default
    @Column(name = "consecutive_correct_bonus_factor", nullable = false)
    Float consecutiveCorrectBonusFactor = 1.0f;

    @Builder.Default
    @Column(name = "consecutive_incorrect_penalty_factor_learning", nullable = false)
    private Float consecutiveIncorrectPenaltyFactorLearning = 0.22f;

    @Builder.Default
    @Column(name = "consecutive_incorrect_penalty_factor_relearning", nullable = false)
    private Float consecutiveIncorrectPenaltyFactorRelearning = 0.25f;

    @Builder.Default
    @Column(name = "ratio_mix", length = 10, nullable = false)
    String ratioMix = "50 50";

    @Builder.Default
    @Column(name = "is_automatic_select_card", nullable = false)
    Boolean isAutomaticSelectCard = false;

    @Builder.Default
    @Column(name = "interval_seconds_can_skip", nullable = false)
    @Min(0)
    @Max(1200) // Skip tối đa 1200 giây
    Integer intervalSecondsCanSkip = 0;

    @Builder.Default
    @Column(name = "card_per_round", nullable = false)
    @Min(1)
    @Max(256)
    Long cardsPerRound = 7L;
}
