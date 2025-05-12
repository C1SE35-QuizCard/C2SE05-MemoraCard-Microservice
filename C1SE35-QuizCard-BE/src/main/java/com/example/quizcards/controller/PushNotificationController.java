package com.example.quizcards.controller;

import com.example.quizcards.dto.request.PushNotificationRequest;
import com.example.quizcards.service.IPushNotificaitonService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/v1/notification")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PushNotificationController {
   IPushNotificaitonService notificaitonService;

   @PostMapping("/save-subscription")
   @PreAuthorize("hasAnyRole('FREE_USER', 'PREMIUM_USER', 'ADMIN')")
   public ResponseEntity<?> saveSubscription(@RequestBody Map<String, Object> subscription) {
       notificaitonService.saveSubscription(subscription);
       return ResponseEntity.ok().build();
   }

    @GetMapping("/new-notification/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<?> triggerNotification(@PathVariable("userId") Long userId) throws Exception {
        notificaitonService.sendNotification(userId,"Xin chào", "Thông báo từ Java!");
        return ResponseEntity.ok().build();
    }
}
