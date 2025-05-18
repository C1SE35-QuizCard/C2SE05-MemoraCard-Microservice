package com.microservices.streakscheduleservice.config;

import com.microservices.streakscheduleservice.service.ISendNotification;
import com.microservices.streakscheduleservice.service.InitStreakActivity;
import com.microservices.streakscheduleservice.service.UserDailyNotiWfImpl;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowClientOptions;
import io.temporal.client.schedules.ScheduleClient;
import io.temporal.serviceclient.WorkflowServiceStubs;
import io.temporal.serviceclient.WorkflowServiceStubsOptions;
import io.temporal.spring.boot.autoconfigure.RootNamespaceAutoConfiguration;
import io.temporal.worker.Worker;
import io.temporal.worker.WorkerFactory;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;

@Configuration
@FieldDefaults(level = AccessLevel.PRIVATE)
@EnableAutoConfiguration(exclude = RootNamespaceAutoConfiguration.class)
public class TemporalConfig {
    @Value("${spring.temporal.connection.target:127.0.0.1:7233}") // Địa chỉ Temporal server
    String temporalServiceAddress;

    @Value("${spring.temporal.namespace:default}") // Namespace của Temporal
    String temporalNamespace;

    @Bean("temporalTaskQueue")
    public String getTaskQueueName(
            @Value("${spring.temporal.workers[0].task-queue:notification-schedule-queue}")
            String name) {
        return name;
    }

    @Bean
    public WorkflowServiceStubs workflowServiceStubs() {
        return WorkflowServiceStubs.newServiceStubs(
                WorkflowServiceStubsOptions.newBuilder()
                        .setTarget(temporalServiceAddress)
                        .build());
    }

    @Bean
    public WorkflowClient workflowClient(@Qualifier("workflowServiceStubs") WorkflowServiceStubs service) {
        return WorkflowClient.newInstance(
                service,
                WorkflowClientOptions.newBuilder()
                        .setNamespace(temporalNamespace)
                        .build());
    }

    @Bean
    public WorkerFactory workerFactory(WorkflowClient workflowClient) {
        return WorkerFactory.newInstance(workflowClient);
    }

    @Bean
    public ScheduleClient scheduleClient(@Qualifier("workflowServiceStubs") WorkflowServiceStubs service) {
        return ScheduleClient.newInstance(service);
    }

    @EventListener(ContextRefreshedEvent.class)
    public void startWorkerFactory(ContextRefreshedEvent event) {
        WorkerFactory factory = event.getApplicationContext().getBean(WorkerFactory.class);
        factory.start();
    }

    @Bean
    public Worker scheduledTaskWorker(
            WorkerFactory workerFactory,
            @Qualifier("temporalTaskQueue") String taskQueueName,
            InitStreakActivity initStreakActivity,
            ISendNotification sendNotification
    ) {
        Worker worker = workerFactory.newWorker(taskQueueName);
        // Đăng ký Workflow Implementation
        worker.registerWorkflowImplementationTypes(
                UserDailyNotiWfImpl.class
        );
//        // Đăng ký Activity Implementation
        worker.registerActivitiesImplementations(
                initStreakActivity,
                sendNotification
        );
        return worker;
    }
}