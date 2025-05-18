package com.microservices.streakscheduleservice.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
@Table("streak_analysis")
public class StreakAnalysis {
    @Id                               // auto-increment (IDENTITY) vẫn OK với MySQL-R2DBC
    Long id;

    @Column("user_id")
    Long userId;

    @Builder.Default
    @Column("longest_streak")
    Long longestStreak = 0L;

    @Builder.Default
    @Column("current_streak")
    Long currentStreak = 0L;

    @Builder.Default
    @Column("day_learned")
    Long dayLearned = 0L;

    @Column("last_updated")
    LocalDate lastUpdated;
}
