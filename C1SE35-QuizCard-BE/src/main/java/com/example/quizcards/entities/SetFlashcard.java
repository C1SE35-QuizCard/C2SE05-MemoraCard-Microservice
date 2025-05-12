package com.example.quizcards.entities;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Generated;
import org.hibernate.annotations.GenerationTime;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "set_flashcards", indexes = {
        @Index(name = "idx_category_id", columnList = "category_id"),
        @Index(name = "idx_user_id", columnList = "user_id"),
        @Index(name = "idx_title_fulltext", columnList = "title"),
        @Index(name = "idx_sharing_mode", columnList = "sharing_mode"),
})
public class SetFlashcard implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "set_id")
    private Long setId;

    @Column(name = "title", length = 100, nullable = false)
    private String title;

    @Column(name = "description_set", columnDefinition = "TEXT")
    private String descriptionSet;

    @Generated(GenerationTime.INSERT)
    @Column(name = "created_at", nullable = false, updatable = false, insertable = false,
            columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime createdAt;

    @Generated(GenerationTime.ALWAYS)
    @Column(name = "updated_at", insertable = false, updatable = false, nullable = false,
            columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime updatedAt;

    @Column(name = "is_approved")
    private Boolean isApproved;

    @Column(name = "is_anonymous")
    private Boolean isAnonymous;

    @Column(name = "sharing_mode")
    private Boolean sharingMode;

    @Column(name = "hash_password", columnDefinition = "TEXT", nullable = true)
    private String hashPassword;

    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "user_id", nullable = false)
    private AppUser user;

    @ManyToOne
    @JoinColumn(name = "category_id", referencedColumnName = "category_id")
    private CategorySetFlashcard category;
}