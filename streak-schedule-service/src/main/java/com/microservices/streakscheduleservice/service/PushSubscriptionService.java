package com.microservices.streakscheduleservice.service;

import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowExecutionAlreadyStarted;
import io.temporal.client.WorkflowOptions;
import io.temporal.spring.boot.autoconfigure.properties.TemporalProperties;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.ZoneId;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class PushSubscriptionService {
    WorkflowClient client;
    TemporalProperties props;

    @Qualifier("temporalTaskQueue")
    String taskQueueName;

    @Value("${app.workflow_id_prefix}")
    @NonFinal
    String workflowIdPrefix;

    public void subscribe(long uid, String tz) {
        int off = ZoneId.of(tz).getRules().getOffset(Instant.now()).getTotalSeconds();
//      NO CODE:  var stub = client.newWorkflowStub(
//                UserDailyNotiWf.class,
//                "push_" + uid);

        var stub = client.newWorkflowStub(
                UserDailyNotiWf.class,
                WorkflowOptions.newBuilder()
                        .setWorkflowId(workflowIdPrefix + uid)
                        .setTaskQueue(taskQueueName)
                        .build());

        try {
            WorkflowClient.start(stub::runWithInit, uid, off, true, null, null);
        } catch (WorkflowExecutionAlreadyStarted we) {
            log.info("Workflow already started: {}", we.getMessage());
            stub.updateTz(tz);
        } catch (Exception e) {
            log.error("Error starting workflow: {}", e.getMessage());
            throw e;
        }
    }
}
