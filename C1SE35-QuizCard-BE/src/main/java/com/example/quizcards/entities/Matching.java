package com.example.quizcards.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
@Table(name = "matchings")
public class Matching {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "matching_id")
    private Long matchingId;

    @ManyToOne
    @JoinColumn(name = "card_id")
    private Flashcard flashcard;

    @Enumerated(EnumType.STRING)
    @Column(name = "round_number")
    private RoundNumber roundNumber;

    @Column(name = "set_code")
    private String setCode;

    @ManyToOne
    @JoinColumn(name = "user_id")
    @JsonIgnore
    private AppUser user;

    @Column(name = "is_correct")
    private Boolean isCorrect;

    public enum RoundNumber {
        ROUND_1,
        ROUND_2,
        ROUND_3,
    }

}