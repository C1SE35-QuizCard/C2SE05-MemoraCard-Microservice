package com.example.quizcards.service;

import com.example.quizcards.dto.request.PushNotificationRequest;

import java.util.Map;

public interface IPushNotificaitonService {
    void saveSubscription(Map<String, Object> subscription);

    void sendNotification(Long userId, String title, String body) throws Exception;

    void sendNotificationByKafka(PushNotificationRequest request) throws Exception;


}
