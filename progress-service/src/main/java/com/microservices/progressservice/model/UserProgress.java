package com.microservices.progressservice.model;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("user_progress")
public class UserProgress {

    @Id
    @Column("progress_id")
    private Long progressId;

    /**
     * foreign keys as simple fields in R2DBC
     */
    @Column("card_id")
    private Long cardId;

    @Column("user_id")
    private Long userId;

    @Column("progress_type")
    private Boolean progressType;

    @Column("marked_for_attention")
    private Boolean isAttention;

    /**
     * default values preserved via @Builder.Default
     */
    @Builder.Default
    @Column("mode_version")
    private Long modeVersion = -1L;

    @Builder.Default
    @Min(0)
    @Max(3)
    @Column("consecutive_correct_simple_mode")
    private Integer consecutiveCorrectSimpleMode = 1;

    @Builder.Default
    @Column("count_consecutive_hard_press")
    private Integer countConsecutiveHardPress = 0;

    @Builder.Default
    @Column("updated_at")
    private Instant updatedAt = Instant.now();
}