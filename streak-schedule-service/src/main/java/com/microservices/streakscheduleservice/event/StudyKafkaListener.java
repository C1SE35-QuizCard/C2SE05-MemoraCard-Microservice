package com.microservices.streakscheduleservice.event;

import com.microservices.dto.notification.StreakNotificationData;
import com.microservices.dto.security.UserInfo;
import com.microservices.streakscheduleservice.service.PushSubscriptionService;
import com.microservices.streakscheduleservice.service.UserDailyNotiWf;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowExecutionAlreadyStarted;
import io.temporal.client.WorkflowOptions;
import io.temporal.client.WorkflowStub;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.ZoneId;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class StudyKafkaListener {
    WorkflowClient client;
    PushSubscriptionService pushSubscriptionService;

    @Value("${app.workflow_id_prefix}")
    @NonFinal
    String workflowIdPrefix;

    @Qualifier("temporalTaskQueue")
    String taskQueueName;

    @KafkaListener(topics = "on-learning-done-event", groupId = "group-consumer-email-notification")
    public void onTriggerLearningDone(StreakNotificationData data,
                                      Acknowledgment ack) {
        String userIdStr = data.getUserId();
        long uid;

        try {
            uid = Long.parseLong(userIdStr);
        } catch (NumberFormatException e) {
            log.error("Invalid userId: {}", userIdStr);
            ack.acknowledge();  // bỏ qua luôn
            return;
        }

        String workflowId = workflowIdPrefix + uid;

        WorkflowOptions opts = WorkflowOptions.newBuilder()
                .setWorkflowId(workflowId)
                .setTaskQueue(taskQueueName)
                .build();

        var stub = client.newWorkflowStub(
                UserDailyNotiWf.class,
                opts);

        String tz = data.getPayload().get("user_tz").toString();

        // convert tz to offset
        int off = ZoneId.of(tz).getRules().getOffset(Instant.now()).getTotalSeconds();

        try {
            WorkflowClient.start(stub::runWithInit, uid, off, true, null, null);
        } catch (WorkflowExecutionAlreadyStarted we) {
            log.info("Workflow already started: {}", we.getMessage());
            log.info("Updating data: {}", data);
            stub.markStudied(
                    data.getUserId(),
                    data.getLastDateLearned(),
                    data.getCurrentStreak()
            );
            ack.acknowledge();
        } catch (Exception e) {
            log.error("Error starting workflow: {}", e.getMessage());
            throw e;
        }
    }

    @KafkaListener(topics = "register-streak-notification", groupId = "group-consumer-email-notification")
    public void registerPushSubscription(UserInfo userInfo,
                                         Acknowledgment ack) {
        try {
            System.out.println("Listened...");
            pushSubscriptionService.subscribe(userInfo.getId(), userInfo.getUserTz());
            ack.acknowledge();
        } catch (Exception e) {
            log.error("Error starting workflow: {}", e.getMessage());
            throw e;
        }
    }

    @KafkaListener(topics = "unregister-streak-notification", groupId = "group-consumer-email-notification")
    public void unregisterPushSubscription(UserInfo userInfo,
                                           Acknowledgment ack) {
        // nothing to do
        ack.acknowledge();
    }

    @KafkaListener(topics = "streak-notification-done", groupId = "group-consumer-email-notification")
    public void streakNotificationGotDone(StreakNotificationData data,
                                          Acknowledgment ack) {
        // nothing to do
        ack.acknowledge();
    }
}