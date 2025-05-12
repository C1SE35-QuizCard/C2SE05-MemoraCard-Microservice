package com.example.quizcards.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "category_subscriptions")
public class CategorySubscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "category_subscriptions_id")
    private Long id;

    @Column(name = "category_subscriptions_name", length = 100, nullable = false)
    private String name;

    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "subscriptions_description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "max_sets_flashcards")
    private Integer maxSetsFlashcards;

    @Column(name = "max_flashcards_per_set")
    private Integer maxFlashcardsPerSet;

    @Column(name = "max_sets_per_day")
    private Integer maxSetsPerDay;

    @Column(name = "max_rooms_create_per_day")
    private Integer maxRoomsCreatePerDay;

    @Column(name = "max_terms_per_room")
    private Integer maxTermsPerRoom;

    @Column(name = "expired_month", nullable = false)
    private int expiredMonth;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    @Temporal(TemporalType.TIMESTAMP)
    @UpdateTimestamp
    private LocalDateTime updatedAt;
}