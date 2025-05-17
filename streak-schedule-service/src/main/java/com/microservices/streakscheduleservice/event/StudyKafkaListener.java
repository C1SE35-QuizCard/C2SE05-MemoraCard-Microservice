package com.microservices.scheduleservice.event;

import com.microservices.dto.notification.StreakNotificationData;
import com.microservices.dto.security.UserInfo;
import com.microservices.scheduleservice.service.PushSubscriptionService;
import com.microservices.scheduleservice.service.UserDailyNotiWf;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
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
        UserDailyNotiWf stub = client.newWorkflowStub(
                UserDailyNotiWf.class,
                WorkflowOptions.newBuilder()
                        .setWorkflowId(workflowId)
                        .setTaskQueue(taskQueueName)
                        .build()
        );

        try {
            // 2) Chỉ signal — không start mới
            stub.markStudied(
                    uid,
                    data.getLastDateLearned(),
                    data.getCurrentStreak()
            );
            log.info("signal markStudied to {}", workflowId);
            ack.acknowledge();
        } catch (io.temporal.client.WorkflowNotFoundException e) {
            // workflow chưa tồn tại → bỏ qua nhẹ
            log.warn("workflow {} not found → ignore learning-done event", workflowId);
            ack.acknowledge();
        } catch (Exception e) {
            // lỗi khác → retry theo DLT policy
            log.error("error signaling {}: {}", workflowId, e.getMessage());
            throw e;
        }
    }

    @KafkaListener(topics = "register-streak-notification", groupId = "group-consumer-email-notification")
    public void registerPushSubscription(UserInfo userInfo) {
        try {
            pushSubscriptionService.subscribe(userInfo.getId(), userInfo.getUserTz());
        } catch (Exception e) {
            log.error("Error starting workflow: {}", e.getMessage());
        }
    }

    @KafkaListener(topics = "unregister-streak-notification", groupId = "group-consumer-email-notification")
    public void unregisterPushSubscription(UserInfo userInfo) {
        // nothing to do
    }
}