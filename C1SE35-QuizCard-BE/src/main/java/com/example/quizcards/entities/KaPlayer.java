package com.example.quizcards.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "ka_players")
public class KaPlayer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;


    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "user_id", nullable = false)
    private AppUser user;

    @ManyToOne
    @JoinColumn(name = "room_id", referencedColumnName = "room_id", nullable = false)
    private KaRoom room;

    @Column(name = "user_score")
    private Integer userScore;

    @Column(name = "answered_correctly")
    private Integer answeredCorrectly;

    @Column(name = "rounds_won")
    private Integer roundsWon;

//    @ManyToOne
//    @JoinColumn(name = "ka_answer_id", referencedColumnName = "answer_id", nullable = true)
//    private KaAnswer kaAnswer;
    @ManyToOne
    private KaAnswer kaAnswer;
}
