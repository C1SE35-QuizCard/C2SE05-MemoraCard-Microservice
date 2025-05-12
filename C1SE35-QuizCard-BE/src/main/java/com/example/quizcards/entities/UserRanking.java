package com.example.quizcards.entities;

import jakarta.persistence.*;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "user_rankings")
@IdClass(UserRankingId.class)
public class UserRanking {

    @Id
    @Column(name = "room_id", nullable = false)
    private Long roomId;

    @Id
    @Column(name = "question_id", nullable = false)
    private Long questionId;

    @Id
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "is_correct")
    private Boolean isCorrect;

    @Column(name = "completion_time")
    private Integer completionTime;

    @Column(name = "question_ranking")
    private Integer questionRanking;

    @Column(name = "total_correct")
    private Integer totalCorrect;

    @Column(name = "total_time")
    private Integer totalTime;

    @Column(name = "total_ranking")
    private Integer totalRanking;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id")
    private KaRoom room;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id")
    private KaQuestion question;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private KaPlayer user;
}
