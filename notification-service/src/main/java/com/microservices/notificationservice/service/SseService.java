package com.microservices.notificationservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microservices.dto.notification.NotificationSagaRecord;
import com.microservices.dto.notification.StreakNotificationData;
import com.microservices.dto.response.StreakNotificationResponse;
import com.microservices.dto.security.UserInfo;
import com.microservices.dto.security.UserPrincipal;
import com.microservices.notificationservice.security.ConnectionGuard;
import com.microservices.utils.ReactiveRedisUtils;
import jakarta.annotation.Nullable;
import jakarta.annotation.PostConstruct;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.stream.*;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class SseService {
    Duration KEEPALIVE = Duration.ofSeconds(15);
    Duration STREAM_TTL = Duration.ofDays(1);
    Duration ACK_TIMEOUT = Duration.ofSeconds(4);
    Duration ONLINE_TTL = Duration.ofHours(6);

    ReactiveRedisTemplate<String, Object> redis;

    KafkaTemplate<String, Object> kafkaTemplate;

    ObjectMapper mapper;

    ConnectionGuard guard;

    ReactiveRedisUtils redisUtils;


//    @NonFinal
//    ReactiveRedisUtils redisUtils;
//
//    @PostConstruct
//    private void initRedisUtils() {
//        redisUtils = new ReactiveRedisUtils(redis);
//    }

    private ReadOffset parseOffset(String lastId) {
        if (lastId != null && !lastId.isBlank()) {
            try {
                return ReadOffset.from(lastId);
            } catch (IllegalArgumentException ex) {
                log.warn("Invalid Last-Event-ID='{}', fallback to $", lastId);
            }
        }
        return ReadOffset.from("$");
    }

    private ReadOffset parseOffset2(String lastId) {
        if (lastId != null && lastId.matches("\\d+-\\d+")) {
            return ReadOffset.from(lastId);
        }
        return ReadOffset.from("$");
    }

    public Flux<ServerSentEvent<StreakNotificationResponse>> subscribe(
            UserPrincipal principal,
            @Nullable String lastIdHeader,
            @NotNull String tokenId
    ) {
        String uid = principal.getId().toString();
        String streamKey = keyStream(uid);
        String onlineKey = keyOnline(uid, tokenId);
        UserInfo info = UserInfo.from(principal);

        // 1) Chuẩn bị offset khởi điểm
        AtomicReference<ReadOffset> offsetRef = new AtomicReference<>(
            parseOffset(lastIdHeader == null || lastIdHeader.isBlank() ? "$" : lastIdHeader)
        );
        StreamReadOptions readOpts = StreamReadOptions.empty()
                .count(1)
                .block(Duration.ofSeconds(30));  // chờ tới 30s cho mỗi lần XREAD
//        StreamReadOptions readOpts = StreamReadOptions.empty()
//                .block(Duration.ZERO);

        // 2) Đăng ký guard + đánh dấu online + gửi Kafka register
        Mono<Void> init = guard.register(uid, tokenId)
                .flatMap(allowed -> {
                    if (!allowed) {
                        return Mono.error(new ResponseStatusException(
                                HttpStatus.TOO_MANY_REQUESTS,
                                "Too many SSE connections – please wait"
                        ));
                    }
                    return new ReactiveRedisUtils(redis)
                            .saveToRedis(onlineKey, true, ONLINE_TTL.toHours(), TimeUnit.HOURS)
                            .then(Mono.fromRunnable(() ->
                                    kafkaTemplate.send("register-streak-notification", info)
                            ));
                });

        Flux<ServerSentEvent<StreakNotificationResponse>> dataFlux = Flux.defer(() ->
                        redis.opsForStream()
                                .read(readOpts, StreamOffset.create(streamKey, offsetRef.get()))
                )
                .onErrorResume(e -> {
                    log.warn("Stream-read error – reset offset to $", e.getMessage());
                    offsetRef.set(ReadOffset.from("$"));
                    return redis.opsForStream()
                            .read(readOpts, StreamOffset.create(streamKey, offsetRef.get()));
                })
                .flatMap(rec -> {
                    // cập nhật offset ngay khi đọc đươc record mới
                    offsetRef.set(ReadOffset.from(rec.getId().getValue()));
                    StreakNotificationResponse resp = mapper.convertValue(
                            rec.getValue().get("data"),
                            StreakNotificationResponse.class
                    );
                    return Mono.just(ServerSentEvent.<StreakNotificationResponse>builder()
                            .event("message")
                            .id(rec.getId().getValue())
                            .data(resp)
                            .build());
                })
                .repeat();  // khi xong (read về 1 record) sẽ tự gọi lại read()

        // 4) Flux ping keep-alive dưới dạng event "ping"
        Flux<ServerSentEvent<StreakNotificationResponse>> pingFlux =
                Flux.interval(KEEPALIVE)
                        .map(i -> ServerSentEvent.<StreakNotificationResponse>builder()
                                .event("ping")
                                .data(null)
                                .build());

        // 5) Merge cả hai, kèm cleanup khi client disconnect
        return init.thenMany(
                        Flux.merge(dataFlux, pingFlux)
                                .doFinally(sig -> {
                                    redis.delete(onlineKey).subscribe();
                                    guard.unregister(uid).subscribe();
                                    kafkaTemplate.send("unregister-streak-notification", info);
                                })
                )
                .onErrorResume(e -> {
                    if (e instanceof ResponseStatusException) {
                        return Flux.error(e);
                    }
                    return Flux.error(new ResponseStatusException(
                            HttpStatus.INTERNAL_SERVER_ERROR, "Internal SSE error", e
                    ));
                });
    }


    public Mono<Void> ack(String sagaId) {
        return redisUtils.saveToRedis("saga:" + sagaId + ":ack",
                true, 1, TimeUnit.HOURS);
    }

    public Mono<Void> push(String userId, StreakNotificationData data) {
        return push(userId, data, true);
    }

    public Mono<Void> push(String userId, StreakNotificationData data, boolean triggerSend) {
        return push(userId, data, triggerSend, false);
    }

    public Mono<Void> push(String userId, StreakNotificationData data,
                           boolean triggerSend, boolean autoAck) {
        return isUserOnline(userId)
                .flatMap(isOn -> {
                    if (isOn) {
                        // vẫn dùng pushWithSaga để ghi Redis Stream
                        return pushWithSaga(userId, data, 1, triggerSend, autoAck);
                    } else {
                        log.info("User {} offline → drop", userId);
                        return Mono.empty();
                    }
                });
    }

    public Mono<Void> pushWithSaga(String uid, StreakNotificationData data, int attempt) {
        return pushWithSaga(uid, data, attempt, true);
    }

    public Mono<Void> pushWithSaga(String uid, StreakNotificationData data, int attempt, boolean triggerSend) {
        return pushWithSaga(uid, data, attempt, triggerSend, false);
    }

    public Mono<Void> pushWithSaga(String uid, StreakNotificationData data, int attempt, boolean triggerSend, boolean autoAck) {
        ReactiveRedisUtils redisUtils = new ReactiveRedisUtils(redis);

        System.out.println("Trigger one time or many time ?");

        String stream = keyStream(uid);
        String sagaId = UUID.randomUUID().toString();
        long expire = System.currentTimeMillis() + ACK_TIMEOUT.toMillis();

        /* ---- bản tin gửi cho client ---- */
        StreakNotificationResponse resp = buildResp(data);

        Map<String, Object> record = new HashMap<>();
        record.put("data",    resp);
        record.put("sagaId",  sagaId);

        /* ---- object phục vụ retry ---- */
        NotificationSagaRecord saga = NotificationSagaRecord.builder()
                .sagaId(sagaId)
                .userId(uid)
                .payload(data)
                .attempt(attempt)
                .expireAt(expire)
                .build();

        /* ---- nếu cần push, thực thi chuỗi ghi stream & TTL ---- */
        Mono<Void> maybePushStream = triggerSend
                ? redis.opsForStream()
                .add(StreamRecords.mapBacked(record).withStreamKey(stream))
                .flatMap(id -> redis.expire(stream, STREAM_TTL))
                .then()
                : Mono.empty();   // bỏ qua hoàn toàn

        /* ---- pipeline chung ---- */
        return maybePushStream
                .then(redisUtils.saveToRedis(
                        "saga:" + sagaId,
                        saga,
                        ACK_TIMEOUT.toSeconds() * 4,
                        TimeUnit.SECONDS))
//                .then(autoAck ? redisUtils.saveToRedis(
//                        "saga:" + sagaId + ":ack",
//                        true,
//                        1,
//                        TimeUnit.HOURS) : Mono.empty()
//                )
                .then(redis.opsForZSet()
                        .add("saga:pending", sagaId, (double) expire))
                .then(redisUtils.saveToRedis(
                        "data_streak:" + sagaId,
                        data,
                        2,
                        TimeUnit.HOURS))
                .then(Mono.fromRunnable(() ->
                        System.out.println("[pushWithSaga] Completed pipeline for sagaId=" + sagaId)
                ));
    }


    private ServerSentEvent<StreakNotificationResponse> toSse(
            MapRecord<String, Object, Object> rec) {
        System.out.println("Where to sse ?");
        StreakNotificationResponse resp =
                mapper.convertValue(rec.getValue().get("data"), StreakNotificationResponse.class);

        return ServerSentEvent.<StreakNotificationResponse>builder()
                .event("message")
                .id(rec.getId().getValue())
                .data(resp)
                .build();
    }


    private StreakNotificationResponse buildResp(StreakNotificationData d) {
        // cũng không dùng Map.of để build response payload
        Map<String,Object> response = new HashMap<>();
        response.put("message",       d.getMessage());
        response.put("currentStreak", d.getCurrentStreak());
        response.put("payload",       d.getPayload());

        return StreakNotificationResponse.builder()
                .response(response)
                .httpStatusCode(200)
                .build();
    }

    private String keyStream(String uid) {
        return "noti:" + uid;
    }

    private String keyOnline(String uid, String tokenId) {
        return "online:" + uid + ":" + tokenId;
    }

    private String keyOnline(String uid) {
        return keyOnline(uid, "*");
    }

    private Mono<Boolean> isUserOnline(String userId) {
        String pattern = keyOnline(userId);
        // keys() trả Flux<String> các key match pattern
        return redis.keys(pattern)
                .hasElements();
    }
}
