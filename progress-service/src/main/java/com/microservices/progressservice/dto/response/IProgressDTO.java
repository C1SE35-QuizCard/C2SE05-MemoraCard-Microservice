package com.microservices.progressservice.dto.response;

import org.springframework.data.relational.core.mapping.Column;

import java.time.Instant;

public record IProgressDTO(
        @Column("progress_id")                       Long    progressId,
        @Column("progress_type")                     Boolean progressType,
        @Column("marked_for_attention")              Boolean isAttention,
        @Column("user_id")                           Long    userId,
        @Column("card_id")                           Long    cardId,
        @Column("consecutive_correct_simple_mode")   Integer consecutiveCorrectSimpleMode,
        @Column("count_consecutive_hard_press")      Integer countConsecutiveHardPress,
        @Column("mode_version")                      Long    progressVersion,
        @Column("updated_at")                        Instant updatedAt
) {}
