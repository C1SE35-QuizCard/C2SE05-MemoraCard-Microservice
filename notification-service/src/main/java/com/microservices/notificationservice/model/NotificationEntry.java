package com.microservices.notificationservice.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class NotificationEntry {
    private String notificationId;   // UUID business key
    private String userId;
    private Instant createdAt;
    private String message;
    private String urlLink;
    private Map<String, Object> payload;
    private Boolean isRead;
}
