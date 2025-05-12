package com.example.quizcards.entities;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.io.Serializable;
import java.time.LocalDate;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "streak_analysis", indexes = {
        @Index(name = "idx_streak_analysis_user_id", columnList = "user_id")
})
public class StreakAnalysis implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    AppUser user;

    @Builder.Default
    @Column(name = "longest_streak", nullable = false)
    Long longestStreak = 0L;

    @Builder.Default
    @Column(name = "current_streak", nullable = false)
    Long currentStreak = 0L;

    @Builder.Default
    @Column(name = "day_learned", nullable = false)
    Long dayLearned = 0L;

    // Cái này theo múi giờ của client
    @Column(name = "last_updated")
    LocalDate lastUpdated;
}
