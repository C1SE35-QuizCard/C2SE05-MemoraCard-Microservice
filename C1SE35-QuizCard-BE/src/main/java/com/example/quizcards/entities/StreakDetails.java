package com.example.quizcards.entities;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "streak_details", indexes = {
        @Index(name = "idx_streak_details_user_id", columnList = "user_id"),
        @Index(name = "idx_streak_details_date_learned", columnList = "date_learned"),
        @Index(name = "idx_streak_details_user_date", columnList = "user_id, date_learned", unique = true)
})
public class StreakDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    AppUser user;

    @Column
    LocalDate dateLearned;

    //    enum StreakAction {
    //        LEARNED,
    //        FREEZE
    //    }
    //
    //    @Enumerated(EnumType.STRING)
    //    @Column(name = "streak_action", nullable = false)
    //    StreakAction action;
}
