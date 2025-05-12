package com.example.quizcards.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "user_flashcard_settings", indexes = {
        @Index(name = "idx_user_id", columnList = "user_id"),
        @Index(name = "idx_set_id", columnList = "set_id"),
        @Index(name = "idx_user_id_set_id", columnList = "user_id,set_id", unique = true),
})
public class UserFlashcardSetting {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long settingId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "set_id", nullable = false)
    private SetFlashcard setFlashcard;

    @Column(name = "last_card_index")
    @Min(value = 0)
    private Long lastCardId;

    @Column(name = "shuffle_mode")
    private boolean shuffleMode;

    @Column(name = "flip_card_mode")
    private boolean flipCardMode;

    @Column(name = "last_accessed")
    @UpdateTimestamp
    private LocalDateTime lastAccessed;

    public UserFlashcardSetting(AppUser user, SetFlashcard setFlashcard) {
        this.user = user;
        this.setFlashcard = setFlashcard;
    }
}
