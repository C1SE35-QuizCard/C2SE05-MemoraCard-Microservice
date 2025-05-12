package com.example.quizcards.event.trigger;

import com.example.quizcards.dto.request.PushNotificationRequest;
import com.example.quizcards.service.IPushNotificaitonService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PushNotificationEvent {
    IPushNotificaitonService notificaitonService;

    @KafkaListener(topics = "push-notification-delievery", groupId = "group-consumer-email-notification")
    public void sendPushNotification(PushNotificationRequest request) {
        try {
            notificaitonService.sendNotificationByKafka(request);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
