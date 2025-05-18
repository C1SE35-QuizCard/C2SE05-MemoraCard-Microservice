package com.microservices.streakscheduleservice.service;

import com.microservices.dto.notification.StreakNotificationData;
import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;

@ActivityInterface
public interface ISendNotification {
    @ActivityMethod
    void sendStreakNotification(StreakNotificationData data);
}
