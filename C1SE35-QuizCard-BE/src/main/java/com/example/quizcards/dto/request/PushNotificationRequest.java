package com.example.quizcards.dto.request;

import com.example.quizcards.entities.PushNotificationSubscription;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PushNotificationRequest {
    Long id;

    String endpoint;

    String keys;

    Long userId;

    Map<String, Object> payload;
}
