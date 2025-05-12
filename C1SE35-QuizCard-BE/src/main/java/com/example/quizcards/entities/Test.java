package com.example.quizcards.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "test", indexes = {
        @Index(name = "idx_test_mode_id", columnList = "test_mode_id"),
        @Index(name = "idx_user_id", columnList = "user_id"),
        @Index(name = "idx_set_id", columnList = "set_id")
})
public class Test {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "test_id")
    private Long testId;

    @ManyToOne
    @JoinColumn(name = "test_mode_id", referencedColumnName = "test_mode_id")
    private TestMode testMode;

    @ManyToOne
    @JoinColumn(name = "set_id", referencedColumnName = "set_id")
    private SetFlashcard setFlashcards;

    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "user_id")
    private AppUser user;

    @Column(name = "total_question", nullable = false)
    private int totalQuestion;

    @Column(name = "goal_score", nullable = false)
    private int goalScore;

    @Column(name = "remaining_time")
    private LocalTime remainingTime;

    @Column(name = "is_testing")
    private Boolean isTesting;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime createdAt;
}
