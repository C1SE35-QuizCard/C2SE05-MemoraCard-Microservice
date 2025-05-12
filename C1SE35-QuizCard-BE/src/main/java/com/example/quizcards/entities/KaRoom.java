package com.example.quizcards.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "ka_rooms")
public class KaRoom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "room_id")
    private Long roomId;

    @Column(name = "room_name", length = 100)
    private String roomName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "set_id", nullable = false)
    private SetFlashcard setFlashcard;

    @Column(name = "pin_code", length = 6, unique = true, nullable = false)
    private String pinCode;

    @Column(name = "current_round")
    private Integer currentRound;

    @Column(name = "max_joiner")
    private Integer maxJoiner;

    @Column(name = "total_rounds")
    private Integer totalRounds;

    @Column(name = "quantity_question")
    private Integer quantityQuestion;

    @Enumerated(EnumType.STRING)
    @Column(name = "status_active")
    private RoomStatus statusActive;

    @Column(name = "created_at")
    private Timestamp createdAt;

    @Column(name = "current_question_id")
    private Long currentQuestionId;

    @Column(name = "time_per_question")
    private Integer timePerQuestion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private AppUser appUser;
    @Column(name = "current_joiner")
    private Integer currentJoiners = 0;
    public boolean canJoinRoom() {
        return currentJoiners < maxJoiner;
    }
    public void addParticipant() {
        if (currentJoiners == null) {
            currentJoiners = 0;
        }
        if (canJoinRoom()) {
            this.currentJoiners++;
        }
    }
    public enum RoomStatus {
        WAITING, PLAYING, FINISHED
    }
}
