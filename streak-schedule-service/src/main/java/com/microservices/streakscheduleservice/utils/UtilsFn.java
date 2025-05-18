package com.microservices.streakscheduleservice.utils;

import com.microservices.dto.notification.StreakNotificationData;
import org.springframework.data.domain.Range;
import org.springframework.data.redis.connection.Limit;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class UtilsFn {
    public static List<Integer> offsetsFor(Instant nowUtc, int hours, int minutes) {
        int utcSec = (int) (nowUtc.getEpochSecond() % 86_400);
        int tgt = hours * minutes * 3600;
        int off1 = (tgt - utcSec + 86_400) % 86_400;   // 0–86399
        int off2 = off1 - 86_400;                      // có thể âm
        List<Integer> rs = new ArrayList<>(2);
        if (off1 != 0 && off1 >= -43_200 && off1 <= 50_400) rs.add(off1);
        if (off2 >= -43_200 && off2 <= 50_400) rs.add(off2);
        return rs;
    }

    public static Instant nextXhXm(Instant current, int offsetSec, int hours, int minutes) {
        ZonedDateTime localNow = current
                .atZone(ZoneOffset.ofTotalSeconds(offsetSec));
        ZonedDateTime nextTime = localNow.withHour(hours).withMinute(minutes)
                .withSecond(0).withNano(0);
        if (!nextTime.isAfter(localNow)) nextTime = nextTime.plusDays(1);
        return nextTime.withZoneSameInstant(ZoneOffset.UTC).toInstant();
    }

    public static LocalDate toLocal(Instant i, int off) {
        return i.atZone(ZoneOffset.ofTotalSeconds(off)).toLocalDate();
    }

    public static LocalDate utcToLocal(LocalDate dUtc, int off) {
        return dUtc.atStartOfDay(ZoneOffset.UTC)
                .withZoneSameInstant(ZoneOffset.ofTotalSeconds(off))
                .toLocalDate();
    }

    public static Mono<List<Object>> popShard(
            ReactiveRedisTemplate<String, Object> redis,
            int offsetSec,
            int shard,
            int total,
            int size,
            String key) {
        int start = shard * size;
        Range<Double> exact = Range.closed((double) offsetSec, (double) offsetSec);

        return redis.opsForZSet().rangeByScore(key, exact,
                        org.springframework.data.redis.connection.RedisZSetCommands.Limit.limit().offset(start).count(size))
                .collectList()
                .flatMap(ids -> {
                    if (ids.isEmpty()) {
                        return Mono.just(ids);
                    } else {
                        return redis.opsForZSet().remove(key, ids.toArray())
                                .thenReturn(ids);
                    }
                });
    }

    public static Mono<Boolean> addBack(ReactiveRedisTemplate<String, Object> redis,
                          long userId,
                          int offsetSec,
                          String key) {
        return redis.opsForZSet().add(key, userId, offsetSec);
    }

    private static final List<String> MESSAGE_TEMPLATES = List.of(
            "You have %d days of streak! Don’t break the chain—learn at least one flashcard today!",
            "Great job! You've reached a %d-day streak. Keep it going by learning a flashcard today!",
            "You're on a %d day streak. Remember to review at least one flashcard today!",
            "You’ve maintained your streak for %d days. Let’s keep it up—learn a flashcard today!",
            "Streak: %d days. Don’t stop now—just one flashcard today keeps the streak alive!"
    );

    public static StreakNotificationData buildNotiMessage(String userId, Long streakCount, LocalDate lastDateLearned) {
        String template = MESSAGE_TEMPLATES.get(ThreadLocalRandom.current().nextInt(MESSAGE_TEMPLATES.size()));
        StreakNotificationData noti = new StreakNotificationData();
        noti.setUserId(userId);
        noti.setCurrentStreak(streakCount);
        noti.setMessage(String.format(template, streakCount));
        noti.setLastDateLearned(lastDateLearned);
        return noti;
    }

    public static Integer getOffsetInSecondsByUserTz(String userTz) {
        if (userTz == null || userTz.isBlank()) {
            return null; // Default to UTC if no timezone is provided
        }
        ZoneOffset zoneOffset = ZoneOffset.of(userTz);
        return zoneOffset.getTotalSeconds();
    }
}
