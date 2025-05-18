package com.microservices.notificationservice.service;

import com.microservices.dto.notification.NotificationSagaRecord;
import com.microservices.dto.notification.StreakNotificationData;
import com.microservices.notificationservice.model.NotificationEntry;
import com.microservices.utils.ReactiveRedisUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Range;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class StreakNotiSagaWatcher {

    /* constructor-injected (nhớ thêm @EnableScheduling ở @SpringBootApplication) */
    private final ReactiveRedisTemplate<String, Object> redis;
    private final SseService sse;
    private final NotificationCRUDService notiService;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    /**
     * quét mỗi 5 giây
     */
    @Scheduled(fixedDelay = 5_000)
    public void scan() {
        System.out.println("scan saga pending...");
        double nowScore = System.currentTimeMillis();          // millis → double
        Range<Double> range = Range.leftOpen(0.0, nowScore);   // (0, now]

        redis.opsForZSet()                                     // ZREVRANGEBYSCORE
                .rangeByScore("saga:pending", range)
                .flatMap(id -> handle(id.toString()))
                .onErrorContinue((e, v) -> log.error("scan error {}", v, e))
                .subscribe();  // ← chỉ subscribe ở biên ngoài scheduler
    }

    /**
     * Xử lý 1 sagaId – có/không ack
     */
    private Mono<Void> handle(String sagaId) {

        return redis.opsForValue()
                .get("saga:" + sagaId + ":ack")
//                .get("saga:" + sagaId)
                .hasElement()                              // Mono<Boolean>
                .flatMap(hasAck ->
                        hasAck ? cleanup(sagaId, true)
                                : retryOrDrop(sagaId));
    }

    /**
     * Retry nếu chưa quá 3 lần; quá → drop
     */
    private Mono<Void> retryOrDrop(String sagaId) {
        return redis.opsForValue()
                .get("saga:" + sagaId)                    // lấy bản ghi saga
                .cast(NotificationSagaRecord.class)
                .flatMap(saga -> {

                    if (saga.getAttempt() >= 3) {
                        log.warn("Saga {} drop after {} times retry",
                                sagaId, saga.getAttempt());
                        return cleanup(sagaId, false);
                    }

                    int next = saga.getAttempt() + 1;
                    log.info("Retry #{} user {}", next, saga.getUserId());

                    return sse.pushWithSaga(saga.getUserId(),
                                    saga.getPayload(), next)
                            .then(cleanup(sagaId, false));
                });
    }

    /**
     * Thu gom – successAck==true thì bắn sự kiện done lên Kafka
     */
    private Mono<Void> cleanup(String sagaId, boolean successAck) {
        ReactiveRedisUtils utils = new ReactiveRedisUtils(redis);
        String dataKey = "data_streak:" + sagaId;

        return utils.getFromRedis(dataKey, StreakNotificationData.class)
                .flatMap(streakData -> {
                    // Nếu successAck thì tạo notification rồi chuyển sang Mono<Void>, ngược lại empty
//                    Mono<Void> sendAck = successAck
//                            ? notiService.create(
//                            streakData.getUserId(),
//                            NotificationEntry.builder()
//                                    .message(streakData.getMessage())
//                                    .payload(Map.of("userId", streakData.getUserId()))
//                                    .build()
//                    ).then()
//                            : Mono.empty();

                    return redis.opsForZSet().remove("saga:pending", sagaId)
                            .then(redis.delete("saga:" + sagaId))
                            .then(redis.delete("saga:" + sagaId + ":ack"))
//                            .then(sendAck)               // bắn sự kiện nếu cần
                            .then(redis.delete(dataKey)) // xóa data_streak
                            .doOnSuccess(v ->
                                    log.info("Saga {} cleanup done", sagaId)
                            )
                            .then();
                });
    }
}
