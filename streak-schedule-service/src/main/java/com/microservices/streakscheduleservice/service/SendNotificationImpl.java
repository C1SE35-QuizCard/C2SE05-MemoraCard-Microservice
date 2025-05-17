package com.microservices.scheduleservice.service;

import com.microservices.dto.notification.StreakNotificationData;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class SendNotificationImpl implements ISendNotification {
    KafkaTemplate<String, Object> kafkaTemplate;

    @Override
    public void sendStreakNotification(StreakNotificationData data) {
        kafkaTemplate.send("streak-notification-events", data);
    }
}
