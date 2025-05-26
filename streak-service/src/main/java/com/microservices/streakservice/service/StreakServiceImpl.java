package com.microservices.streakservice.service;

import com.microservices.dto.notification.StreakNotificationData;
import com.microservices.streakservice.dto.StreakStatus;
import com.microservices.streakservice.dto.response.StreakAnalysisResponse;
import com.microservices.streakservice.model.AppUser;
import com.microservices.streakservice.model.StreakAnalysis;
import com.microservices.streakservice.model.StreakDetails;
import com.microservices.streakservice.repository.IStreakAnalysisRepository;
import com.microservices.streakservice.repository.IStreakDetailsRepository;
import com.microservices.streakservice.utils.IbmTzLocaleUtils;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.time.*;
import java.time.format.TextStyle;
import java.time.temporal.TemporalAdjusters;
import java.time.temporal.WeekFields;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Stream;

import static lombok.AccessLevel.PRIVATE;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = PRIVATE, makeFinal = true)
@Slf4j
public class StreakServiceImpl {
    IStreakDetailsRepository learningRepository;
    IStreakAnalysisRepository analysisRepository;
    IbmTzLocaleUtils ibmTzLocaleUtils;
    KafkaTemplate<String, Object> kafkaTemplate;

    @Transactional
    public Mono<Boolean> generateStreakV2(AppUser user, int offsetHours, int offsetMinutes) {
        if (offsetHours < -12 || offsetHours > 14 || offsetMinutes < -59 || offsetMinutes > 59) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid timezone offset");
        }

        LocalDate dateFromClient = Instant.now()
                .atZone(ZoneId.ofOffset("UTC", ZoneOffset.ofHoursMinutes(offsetHours, offsetMinutes)))
                .toLocalDate();

        return analysisRepository.findByUserId(user.getUserId())
                .defaultIfEmpty(StreakAnalysis.builder()
                        .userId(user.getUserId())
                        .longestStreak(1L)
                        .dayLearned(0L)
                        .build())
                .flatMap(sa ->
                        updateStreakDetailsReactive(user.getUserId(), sa, dateFromClient)
                                .flatMap(tuple -> {
                                    LocalDate currDate = tuple.getT1();
                                    StreakDetails sd = tuple.getT2();
                                    return handleUpdateStreakAnalysisV2Reactive(sa, currDate)
                                            .flatMap(updated -> handleSendMessageReactive(user, updated, sd));
                                })
                                .switchIfEmpty(Mono.just(false))
                );
    }

    @Transactional
    public Mono<Boolean> generateStreak(AppUser user, OffsetDateTime clientDateLearned) {
        ZoneOffset off = clientDateLearned.getOffset();

        LocalDate dateFromClient = Instant.now()
                .atZone(ZoneId.ofOffset("UTC", off))
                .toLocalDate();

        if (clientDateLearned.toLocalDate().isAfter(dateFromClient)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Client date is in the future");
        }

        return analysisRepository.findByUserId(user.getUserId())
                .defaultIfEmpty(StreakAnalysis.builder()
                        .userId(user.getUserId())
                        .longestStreak(0L)
                        .currentStreak(0L)
                        .dayLearned(0L)
                        .build())
                .flatMap(sa ->
                        updateStreakDetailsReactive(user.getUserId(), sa, dateFromClient)
                                .flatMap(tuple -> {
                                    LocalDate currDate = tuple.getT1();
                                    StreakDetails sd = tuple.getT2();
                                    return handleUpdateStreakAnalysisReactive(sa, currDate)
                                            .flatMap(updated -> handleSendMessageReactive(user, updated, sd));
                                })
                                .switchIfEmpty(Mono.just(false))
                );
    }

    public Mono<StreakAnalysisResponse> getAnalysisStreakV2(AppUser user,
                                                            OffsetDateTime timeFromClient,
                                                            String localeStr) {
        // (Tùy chọn) Validate lại offset ở tầng service
        ZoneOffset offset = timeFromClient.getOffset();
        int totalSeconds = offset.getTotalSeconds();
        int offsetHours = totalSeconds / 3600;
        if (offsetHours < -12 || offsetHours > 14) {
            throw new IllegalArgumentException("Invalid timezone offset");
        }

        // Lấy ngày client-local
        LocalDate dateFromClient = timeFromClient.toLocalDate();

        // Xử lý locale
        Locale locale = ibmTzLocaleUtils.getLocale(localeStr);

        return getStreakAnalysisResponseMono(user.getUserId(), dateFromClient, locale);
    }

    public Mono<StreakAnalysisResponse> getAnalysisStreak(Long userId,
                                                          int offsetHours,
                                                          int offsetMinutes,
                                                          String localeStr) {
        LocalDate dateFromClient = Instant.now()
                .atZone(ZoneId.ofOffset("UTC", ZoneOffset.ofHoursMinutes(offsetHours, offsetMinutes)))
                .toLocalDate();
        Locale locale = ibmTzLocaleUtils.getLocale(localeStr);

        return getStreakAnalysisResponseMono(userId, dateFromClient, locale);
    }

    public Mono<StreakAnalysisResponse> getAnalysisStreakV3(AppUser user,
                                                            String localeStr) {
        ZoneOffset offset = ZoneOffset.of(user.getUserTz());

        LocalDate dateFromClient = Instant.now().atZone(offset).toLocalDate();

        Locale locale = ibmTzLocaleUtils.getLocale(localeStr);

        return getStreakAnalysisResponseMono(user.getUserId(), dateFromClient, locale);
    }

    private Mono<StreakAnalysisResponse> getStreakAnalysisResponseMono(Long userId, LocalDate dateFromClient, Locale locale) {
        return analysisRepository.findByUserId(userId)
                .defaultIfEmpty(StreakAnalysis.builder()
                        .userId(userId)
                        .longestStreak(0L)
                        .currentStreak(0L)
                        .dayLearned(0L)
                        .build())
                .flatMap(sa -> {
                    LocalDate currDate = sa.getLastUpdated() == null
                            ? dateFromClient
                            : Stream.of(sa.getLastUpdated(), dateFromClient)
                            .max(LocalDate::compareTo).get();

                    WeekFields wf = WeekFields.of(locale);
                    LocalDate startOfWeek = dateFromClient.with(TemporalAdjusters.previousOrSame(wf.getFirstDayOfWeek()));
                    LocalDate endOfWeek = startOfWeek.plusDays(6);

                    Mono<Boolean> todayLearned = learningRepository.existsByUserIdAndDateLearned(userId, currDate);
                    Mono<Boolean> ytdLearned = learningRepository.existsByUserIdAndDateLearned(userId, currDate.minusDays(1));
                    Mono<List<StreakDetails>> weekList = learningRepository
                            .findByUserIdAndDateLearnedBetween(userId, startOfWeek, endOfWeek)
                            .collectList();

                    return Mono.zip(todayLearned, ytdLearned, weekList)
                            .map(t -> buildResponse(sa, t.getT1(), t.getT2(), t.getT3(), locale));
                });
    }

    public Flux<StreakStatus> getLearnedDetails(Long userId, int month, int year, String localeCode) {
        return learningRepository.getLearnedByMonthAndYear(userId, month, year)
                .map(status -> decorateStatus(status, localeCode));
    }

    public Flux<StreakStatus> getAllLearnedDate(Long userId, String localeCode) {
        return learningRepository.findAllStreakStatusByUserId(userId)
                .map(status -> decorateStatus(status, localeCode));
    }

    public Mono<Tuple2<List<StreakStatus>, Long>> getLearnedByDateRange(Long userId,
                                                                        LocalDate startDate,
                                                                        LocalDate endDate,
                                                                        String localeCode,
                                                                        int page,
                                                                        int size) {
        if (startDate.isAfter(endDate)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid date range");
        }
        Mono<List<StreakStatus>> pageData = learningRepository
                .getLearnedByDateRange(userId, startDate, endDate, org.springframework.data.domain.PageRequest.of(page, size))
                .map(s -> decorateStatus(s, localeCode))
                .collectList();
        Mono<Long> count = learningRepository
                .countByUserIdAndDateLearnedBetween(userId, startDate, endDate);
        return Mono.zip(pageData, count);
    }

    private Mono<Tuple2<LocalDate, StreakDetails>> updateStreakDetailsReactive(Long userId,
                                                                               StreakAnalysis sa,
                                                                               LocalDate dateFromClient) {
        LocalDate currDate = sa.getLastUpdated() == null
                ? dateFromClient
                : Stream.of(sa.getLastUpdated(), dateFromClient)
                .max(LocalDate::compareTo).get();

        return learningRepository.existsByUserIdAndDateLearned(userId, currDate)
                .flatMap(exists -> {
                    if (exists) return Mono.empty();
                    StreakDetails sd = StreakDetails.builder()
                            .userId(userId)
                            .dateLearned(currDate)
                            .build();
                    return learningRepository.save(sd)
                            .map(saved -> Tuples.of(currDate, saved));
                });
    }

    private Mono<StreakAnalysis> handleUpdateStreakAnalysisV2Reactive(StreakAnalysis sa,
                                                                      LocalDate currDate) {
        return learningRepository.existsByUserIdAndDateLearned(sa.getUserId(), currDate.minusDays(1))
                .flatMap(prev -> {
                    if (!prev) sa.setCurrentStreak(1L);
                    else {
                        sa.setCurrentStreak(sa.getCurrentStreak() + 1);
                        sa.setLongestStreak(Math.max(sa.getLongestStreak(), sa.getCurrentStreak()));
                    }
                    sa.setLastUpdated(currDate);
                    sa.setDayLearned(sa.getDayLearned() + 1);
                    return analysisRepository.save(sa);
                });
    }

    private Mono<StreakAnalysis> handleUpdateStreakAnalysisReactive(StreakAnalysis sa,
                                                                    LocalDate currDate) {
        return learningRepository.existsByUserIdAndDateLearned(sa.getUserId(), currDate.minusDays(1))
                .flatMap(prev -> {
                    if (!prev) sa.setCurrentStreak(1L);
                    else {
                        sa.setCurrentStreak(sa.getCurrentStreak() + 1);
                        sa.setLongestStreak(Math.max(sa.getLongestStreak(), sa.getCurrentStreak()));
                    }
                    sa.setLastUpdated(currDate);
                    sa.setDayLearned(sa.getDayLearned() + 1);
                    return analysisRepository.save(sa);
                });
    }

    private Mono<Boolean> handleSendMessageReactive(AppUser user,
                                                    StreakAnalysis sa,
                                                    StreakDetails sd) {
        StreakNotificationData data = StreakNotificationData.builder()
                .userId(user.getUserId().toString())
                .lastDateLearned(sa.getLastUpdated())
                .currentStreak(sa.getCurrentStreak())
                .payload(Map.of(
                        "userTz", user.getUserTz(),
                        "user_tz", user.getUserTz(),
                        "userName", user.getUsername(),
                        "user_name", user.getUsername()
                )).build();
        kafkaTemplate.send("on-learning-done-event", data);
        return Mono.just(true);
    }

    private StreakAnalysisResponse buildResponse(StreakAnalysis sa,
                                                 boolean todayLearned,
                                                 boolean ytdLearned,
                                                 List<StreakDetails> weekList,
                                                 Locale loc) {
        List<StreakStatus> sts = weekList.stream()
                .map(d -> StreakStatus.builder()
                        .date(d.getDateLearned())
                        .dayFull(d.getDateLearned().getDayOfWeek().getDisplayName(TextStyle.FULL, loc))
                        .dayShort(d.getDateLearned().getDayOfWeek().getDisplayName(TextStyle.SHORT, loc))
                        .dayFullEn(d.getDateLearned().getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.ENGLISH))
                        .dayShortEn(d.getDateLearned().getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.ENGLISH))
                        .dayRank(d.getDateLearned().getDayOfWeek().getValue())
                        .build())
                .toList();

        return StreakAnalysisResponse.builder()
                .longestStreak(sa.getLongestStreak())
                .currentStreak(todayLearned || ytdLearned ? sa.getCurrentStreak() : 0L)
                .isCurrentDateLearned(todayLearned)
                .dayLearned(sa.getDayLearned())
                .streakOneWeek(sts)
                .build();
    }

    private StreakStatus decorateStatus(StreakStatus s, String localeCode) {
        Locale loc = ibmTzLocaleUtils.getLocale(localeCode);
        return StreakStatus.builder()
                .date(s.getDate())
                .dayFull(s.getDate().getDayOfWeek().getDisplayName(TextStyle.FULL, loc))
                .dayShort(s.getDate().getDayOfWeek().getDisplayName(TextStyle.SHORT, loc))
                .dayFullEn(s.getDate().getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.ENGLISH))
                .dayShortEn(s.getDate().getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.ENGLISH))
                .dayRank(s.getDate().getDayOfWeek().getValue())
                .build();
    }
}
