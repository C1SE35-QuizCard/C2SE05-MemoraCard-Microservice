package com.example.quizcards.entities;

import com.example.quizcards.entities.processEntities.enums.CardState;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;


// có thể tách thêm 2 bảng là progress sm version (progressId (khóa ngoại), smVersion (index), created_at (index), index composite (giữa sm version và created at))
// và bảng progress srs version (progressId (khóa ngoại), srsVersion (index), created_at (index), index composite (giữa srs version và created at))
// giúp giảm đánh index lên các bảng có nhiều thuộc tính -> tăng hiệu suất truy vấn.
@Entity
@Table(
        name = "progress_with_srs",
        uniqueConstraints = @UniqueConstraint(name = "uniq_user_card", columnNames = {"user_id", "card_id"}),
        indexes = {
                @Index(name = "idx_user", columnList = "user_id"),
                @Index(name = "idx_card", columnList = "card_id"),
                @Index(name = "idx_user_card_srs_ver", columnList = "user_id,card_id,srs_version"),
                @Index(name = "idx_user_card_srs_ver_card_state", columnList = "user_id,card_id,srs_version,card_state"),
        }
)
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserSRSProgress {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "progress_id", nullable = false)
    Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "card_id", nullable = false)
    Flashcard flashcard;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    AppUser appUser;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "card_state", length = 20, nullable = false)
    CardState cardState = CardState.New;

    @Builder.Default
    @Column(name = "current_interval")
    Long currentInterval = 0L;

    @Builder.Default
    @Column(name = "ease_factor")
    Float easeFactor = 2.5f;

    @Builder.Default
    @Column(name = "lapses", nullable = false)
    Integer lapses = 0;

    @Builder.Default
    @Column(name = "learning_step", nullable = false)
    Integer learningStep = 0;

    @Builder.Default
    @Column(name = "last_review_time", nullable = false)
    Instant lastReviewTime = Instant.now();

    @Builder.Default
    @Column(name = "due_date", nullable = false)
    Instant dueDate = Instant.now();

    @Builder.Default
    @Column(name = "consecutive_correct", nullable = false)
    Integer consecutiveCorrect = 0;

    @Builder.Default
    @Column(name = "consecutive_incorrect", nullable = false)
    Integer consecutiveIncorrect = 0;

    @Column(name = "old_next_interval_again")
    Long oldNextIntervalAgain;

    @Column(name = "old_next_interval_hard")
    Long oldNextIntervalHard;

    @Column(name = "old_next_interval_good")
    Long oldNextIntervalGood;

    @Column(name = "old_next_interval_easy")
    Long oldNextIntervalEasy;

    @Builder.Default
    @Column(name = "srs_version")
    Long srsVersion = -1L;

    @Column(name = "created_at",
            nullable = false)
    Instant createdAt;

    @Column(name = "updated_at",
            nullable = false)
    Instant updatedAt;
}