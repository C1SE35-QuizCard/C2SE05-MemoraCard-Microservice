package com.microservices.scheduleservice.config;

import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowClientOptions;
import io.temporal.client.schedules.ScheduleClient;
import io.temporal.serviceclient.WorkflowServiceStubs;
import io.temporal.serviceclient.WorkflowServiceStubsOptions;
import io.temporal.worker.Worker;
import io.temporal.worker.WorkerFactory;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;

@Configuration
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TemporalConfig {
    @Value("${spring.temporal.connection.target:127.0.0.1:7233}") // Địa chỉ Temporal server
    String temporalServiceAddress;

    @Value("${spring.temporal.namespace:default}") // Namespace của Temporal
    String temporalNamespace;

    @Value("${spring.temporal.workers[0].task-queue:default-task-queue}")
    String taskQueueName;

//    @Bean
//    NO CODE: public WorkflowServiceStubs workflowServiceStubs() {
//        return WorkflowServiceStubs.newServiceStubs(
//                WorkflowServiceStubsOptions.newBuilder()
//                        .setTarget(temporalServiceAddress)
//                        .build());
//    }

    @Bean
    public WorkflowClient workflowClient(WorkflowServiceStubs service) {
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
    public ScheduleClient scheduleClient(WorkflowServiceStubs service) {
        return ScheduleClient.newInstance(service);
    }

    @EventListener(ContextRefreshedEvent.class)
    public void startWorkerFactory(ContextRefreshedEvent event) {
        WorkerFactory factory = event.getApplicationContext().getBean(WorkerFactory.class);
        factory.start();
    }

//    @Bean
//    public Worker scheduledTaskWorker(WorkerFactory workerFactory, TaskExecutorActivity taskExecutorActivity) {
//        Worker worker = workerFactory.newWorker(TASK_QUEUE_NAME);
//        // Đăng ký Workflow Implementation
//        worker.registerWorkflowImplementationTypes(ScheduledTaskWorkflowImpl.class);
//        // Đăng ký Activity Implementation
//        worker.registerActivitiesImplementations(taskExecutorActivity);
//        return worker;
//    }
}
