package com.example.quizcards.service.impl;

import com.example.quizcards.dto.request.PushNotificationRequest;
import com.example.quizcards.entities.AppUser;
import com.example.quizcards.entities.PushNotificationSubscription;
import com.example.quizcards.repository.IPushNotificationSubscriptionRepository;
import com.example.quizcards.repository.ISetFlashcardRepository;
import com.example.quizcards.security.UserPrincipal;
import com.example.quizcards.service.IPushNotificaitonService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import nl.martijndwars.webpush.Notification;
import nl.martijndwars.webpush.PushService;
import nl.martijndwars.webpush.Subscription;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PushNotificationServiceImpl implements IPushNotificaitonService {

    IPushNotificationSubscriptionRepository subscriptionRepository;

    ISetFlashcardRepository setFlashcardRepository;

    PushService pushService;

    KafkaTemplate<String, Object> kafkaTemplate;

    @Override
    public void saveSubscription(Map<String, Object> subscription) {
        try {
            Authentication au = SecurityContextHolder.getContext().getAuthentication();
            UserPrincipal up = (UserPrincipal) au.getPrincipal();

            PushNotificationSubscription sub = new PushNotificationSubscription();
            sub.setEndpoint(subscription.get("endpoint").toString());
            sub.setKeys(new ObjectMapper().writeValueAsString(subscription.get("keys")));
            sub.setUser(AppUser.builder().userId(up.getId()).build());

            if (!subscriptionRepository.existsByUser_UserIdAndEndpointAndKeys(up.getId(), sub.getEndpoint(), sub.getKeys())) {
                subscriptionRepository.save(sub);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException();
        }
    }

    @Override
    public void sendNotification(Long userId, String title, String body) throws Exception {
        List<PushNotificationSubscription> subs = subscriptionRepository.findByUser_UserId(userId);

        Long setId = setFlashcardRepository.getRandomIdSetFlashcard();

        for (PushNotificationSubscription sub : subs) {
            kafkaTemplate.send("push-notification-delievery", PushNotificationRequest.builder()
                    .id(sub.getId())
                    .endpoint(sub.getEndpoint())
                    .keys(sub.getKeys())
                    .userId(sub.getUser().getUserId())
                    .payload(Map.of("title", title, "body", body, "setId", setId))
                    .build());
        }
    }

    @Override
    public void sendNotificationByKafka(PushNotificationRequest request) throws Exception {
        String jsonPayload = new ObjectMapper().writeValueAsString(request.getPayload());

        Subscription.Keys keys = new ObjectMapper().readValue(request.getKeys(), Subscription.Keys.class);

        nl.martijndwars.webpush.Subscription subscription =
                new nl.martijndwars.webpush.Subscription(
                        request.getEndpoint(),
                        new nl.martijndwars.webpush.Subscription.Keys(
                                keys.p256dh,
                                keys.auth
                        )
                );

        Notification notification = new Notification(subscription, jsonPayload);
        pushService.send(notification);
    }
}
