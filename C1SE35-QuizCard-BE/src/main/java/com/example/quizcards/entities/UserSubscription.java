package com.example.quizcards.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
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
@Table(name = "user_subscriptions", indexes = {
        @Index(name = "idx_user_id", columnList = "user_id"),
        @Index(name = "idx_category_subscriptions_id", columnList = "category_subscriptions_id"),
        @Index(name = "idx_expired_date", columnList = "expired_date"),
        @Index(name = "idx_status_paid", columnList = "status_paid")
})
public class UserSubscription {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_subscriptions_id")
    private Long userSubscriptionsId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser appUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_subscriptions_id", nullable = false)
    private CategorySubscription categorySubscription;

    @Column(name = "expired_date")
    private Timestamp expiredDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status_paid", nullable = false)
    private StatusPaid statusPaid;

    public enum StatusPaid {
        UNPAID, PENDING, PAID
    }

    @NotNull
    @Column(name = "token_payment", nullable = false, unique = true)
    private String tokenPayment;

    @Enumerated(EnumType.STRING)
    @Column(name = "token_type", nullable = false)
    private TokenType type;

    public enum TokenType {
        CARD,            // Thanh toán qua thẻ tín dụng
        GOOGLE_PAY,      // Thanh toán qua Google Pay
        APPLE_PAY,       // Thanh toán qua Apple Pay
        STRIPE_BALANCE   // Thanh toán qua số dư Stripe (nếu có)
    }
}