package com.example.quizcards.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "flashcards", indexes = {
        @Index(name = "idx_flashcards_question", columnList = "question"),
        @Index(name = "idx_flashcards_answer", columnList = "answer"),
        @Index(name = "idx_flashcards_set", columnList = "set_id"),
})
public class Flashcard implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "card_id", nullable = false)
    private Long cardId;

    @Column(name = "question", nullable = false, length = 500)
    private String question;

    @Column(name = "answer", nullable = false, length = 500)
    private String answer;

    @Column(name = "image_url")
    private String imageLink;

    @Column(name = "is_approved")
    private Boolean isApproved;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @ManyToOne
    @JoinColumn(name = "set_id", foreignKey = @ForeignKey(name = "fk_flashcard_set"), nullable = false)
    private SetFlashcard set;
}