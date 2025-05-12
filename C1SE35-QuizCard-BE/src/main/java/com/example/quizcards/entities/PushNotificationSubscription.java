package com.example.quizcards.entities;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;


@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "push_notification_subscriptions", indexes = {
        @Index(name = "idx_userid_endpoint_keys", columnList = "user_id,endpoint,data_keys", unique = true)
})
public class PushNotificationSubscription implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

//    @Lob
    @Column(name = "endpoint")
    private String endpoint;

//    @Lob
    @Column(name = "data_keys")
    private String keys; // lưu cả p256dh và auth dưới dạng JSON string

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;
}
