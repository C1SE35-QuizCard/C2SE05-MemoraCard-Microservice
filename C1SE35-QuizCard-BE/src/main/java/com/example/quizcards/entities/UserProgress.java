package com.example.quizcards.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

//@Audited
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "user_progress", indexes = {
        @Index(name = "idx_user_card", columnList = "user_id,card_id", unique = true),
        @Index(name = "idx_user", columnList = "user_id"),
        @Index(name = "idx_user_version_card", columnList = "user_id,card_id,mode_version"),
        @Index(name = "idx_user_card_version_pt", columnList = "user_id,card_id,mode_version,progress_type"),
})
public class UserProgress {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "progress_id")
    private Long progressId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "card_id", nullable = false)
    private Flashcard flashcard;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser appUser;

    @Column(name = "progress_type")
    private Boolean progressType;

    @Column(name = "marked_for_attention")
    private Boolean isAttention;

    @Builder.Default
    @Column(name = "mode_version", nullable = false)
    private Long modeVersion = -1L;

    @Builder.Default
    @Column(name = "consecutive_correct_simple_mode", nullable = false)
    @Min(0)
    @Max(3)
    Integer consecutiveCorrectSimpleMode = 1;

    @Builder.Default
    @Column(name = "count_consecutive_hard_press", nullable = false)
    Integer countConsecutiveHardPress = 0;

    @Column(name = "updated_at",
            nullable = false
    )
    @Builder.Default
    private Instant updatedAt = Instant.now();
}