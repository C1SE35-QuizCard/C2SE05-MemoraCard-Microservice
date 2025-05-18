package com.microservices.notificationservice.security;


import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Range;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ConnectionGuard {

    private static final Duration WINDOW = Duration.ofSeconds(20);
    private static final int MAX_CONN_PER_WINDOW = 5;

    private final ReactiveRedisTemplate<String, Object> redis;

    /**
     * Trả về Mono<Boolean> – true nếu được phép, false nếu vượt ngưỡng.
     * Cơ chế:
     * 1. Xóa hết các entry cũ hơn (now – WINDOW)
     * 2. Thêm timestamp hiện tại
     * 3. Đếm số entry còn lại
     * 4. So sánh với MAX_CONN_PER_WINDOW và log nếu vượt
     * 5. Refresh TTL = 2×WINDOW cho key
     */
    public Mono<Boolean> register(String uid, String tokenId) {
        String key = "sse:conn:" + uid + ":" + tokenId;
        long now = System.currentTimeMillis();
        // Xác định ngưỡng: các entry cũ hơn (now – WINDOW)
        double windowStart = now - WINDOW.toMillis();

        // Tạo Range [0, windowStart] để xóa tất cả entry <= windowStart
        Range<Double> outdatedRange = Range.closed(0.0, windowStart);

        return redis.opsForZSet()
                // 1. Xóa các kết nối cũ
                .removeRangeByScore(key, outdatedRange)
                .then(
                        // 2. Thêm connection hiện tại
                        redis.opsForZSet().add(key, now, (double) now)
                )
                .then(
                        // 3. Đếm số trong window
                        redis.opsForZSet().size(key)
                )
                .flatMap(size -> {
                    boolean allowed = size <= MAX_CONN_PER_WINDOW;
                    if (!allowed) {
                        log.warn("Spam SSE: {} kết nối trong {} bởi {}", size, WINDOW, uid);
                    }
                    // 5. Đặt TTL = 2×WINDOW để tự dọn khi hết kết nối
                    return redis.expire(key, WINDOW.multipliedBy(2))
                            .thenReturn(allowed);
                });
    }

    /**
     * Tùy chọn: xóa toàn bộ dấu vết khi ngắt kết nối
     */
    public Mono<Long> unregister(String uid) {
        return redis.delete("sse:conn:" + uid);
    }
}
