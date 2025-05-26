package com.microservices.streakservice.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.io.Serializable;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("streak_analysis")
public class StreakAnalysis implements Serializable {
    @Id
    @Column("id")
    private Long id;

    // foreign key user
    @Column("user_id")
    private Long userId;

    @Column("longest_streak")
    @Builder.Default
    private Long longestStreak = 0L;

    @Column("current_streak")
    @Builder.Default
    private Long currentStreak = 0L;

    @Column("day_learned")
    @Builder.Default
    private Long dayLearned = 0L;

    /**
     * Theo múi giờ client
     * Nếu cần convert đặc biệt, khai báo Converter khóa trong DatabaseClient config
     */
    @Column("last_updated")
    private LocalDate lastUpdated;
}
