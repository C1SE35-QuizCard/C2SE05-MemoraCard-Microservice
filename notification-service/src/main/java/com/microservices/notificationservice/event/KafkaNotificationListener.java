package com.microservices.notificationservice.event;

import com.microservices.dto.notification.StreakNotificationData;
import com.microservices.notificationservice.model.NotificationEntry;
import com.microservices.notificationservice.service.NotificationCRUDService;
import com.microservices.notificationservice.service.SseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaNotificationListener {
    private final SseService sse;

    private final NotificationCRUDService crud;

    @KafkaListener(topics = "streak-notification-events", groupId = "group-consumer-email-notification",
            containerFactory = "kafkaListenerContainerFactory")
    public void consume(StreakNotificationData data,
                        Acknowledgment ack) {
        System.out.println("Pushing event data ...");
        Map<String, Object> mutablePayload = data.getPayload() != null
                ? new HashMap<>(data.getPayload())
                : new HashMap<>();
        mutablePayload.put("userId", data.getUserId());
        crud.create(
                        data.getUserId(),
                        NotificationEntry.builder()
                                .userId(data.getUserId())
                                .message(data.getMessage())
                                .payload(mutablePayload)
                                .build()
                )
                .flatMap(savedEntry -> {
                    mutablePayload.put("notificationId", savedEntry.getNotificationId());
                    mutablePayload.put("createdAt", savedEntry.getCreatedAt().toString());
                    data.setPayload(mutablePayload);
                    return sse.push(savedEntry.getUserId(), data, true, true);
                })
                .doOnSuccess(v -> ack.acknowledge())
                .doOnError(e ->
                {
                    log.error("Error pushing event data", e);
                    e.printStackTrace();
                })
                .subscribe();
    }
}
