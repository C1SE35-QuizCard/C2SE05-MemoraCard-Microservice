package com.example.quizcards.service.impl;

import com.example.quizcards.dto.StreakStatus;
import com.example.quizcards.dto.response.StreakAnalysisResponse;
import com.example.quizcards.entities.AppUser;
import com.example.quizcards.entities.StreakAnalysis;
import com.example.quizcards.entities.StreakDetails;
import com.example.quizcards.exception.BadRequestException;
import com.example.quizcards.repository.IStreakAnalysisRepository;
import com.example.quizcards.repository.IStreakDetailsRepository;
import com.example.quizcards.utils.IbmTzLocaleUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.*;
import java.time.format.TextStyle;
import java.time.temporal.TemporalAdjusters;
import java.time.temporal.WeekFields;
import java.util.*;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class StreakServiceImpl {
    IStreakDetailsRepository learningRepository;

    IStreakAnalysisRepository analysisRepository;

    IbmTzLocaleUtils ibmTzLocaleUtils;


    @Transactional
    public boolean generateStreakV2(AppUser user, int offsetHours, int offsetMinutes) {
        if (offsetHours < -12 || offsetHours > 14 || offsetMinutes < -59 || offsetMinutes > 59) {
            throw new BadRequestException("Invalid timezone offset");
        }
        Instant currentTime = Instant.now();
        LocalDate dateFromClient =
                currentTime.atZone(ZoneId.ofOffset("UTC", ZoneOffset.ofHoursMinutes(
                        offsetHours,
                        offsetMinutes))).toLocalDate();

        StreakAnalysis streakAnalysis = analysisRepository.findByUser(user)
                .orElse(StreakAnalysis.builder()
                        .user(user)
                        .longestStreak(1L)
                        .dayLearned(0L)
                        .build());

        LocalDate currentDate =
                streakAnalysis.getLastUpdated() == null ? dateFromClient :
                        Stream.of(streakAnalysis.getLastUpdated(), dateFromClient)
                                .max(LocalDate::compareTo)
                                .orElse(dateFromClient);

        Optional<StreakDetails> streakData = learningRepository.findByUserAndDateLearned(user, currentDate);

        if (streakData.isPresent()) {
            return false;
        }

        StreakDetails streakDetails = StreakDetails.builder()
                .user(user)
                .dateLearned(currentDate)
                .build();

        learningRepository.save(streakDetails);
        return handleUpdateStreakAnalysisV2(user, currentDate, streakAnalysis);
    }

    private boolean handleUpdateStreakAnalysisV2(AppUser user, LocalDate date, StreakAnalysis s) {
        boolean yDayLearned = learningRepository
                .existsByUserAndDateLearned(user, date.minusDays(1));

        if (!yDayLearned) {
            s.setCurrentStreak(1L);
        } else {
            s.setCurrentStreak(s.getCurrentStreak() + 1);
            s.setLongestStreak(Math.max(s.getLongestStreak(), s.getCurrentStreak()));
        }

        s.setLastUpdated(date);

        s.setDayLearned(s.getDayLearned() + 1L);

        analysisRepository.save(s);

        return true;
    }

    @Transactional
    public boolean generateStreak(AppUser user, OffsetDateTime clientDateLearned) {
        int totalSeconds = clientDateLearned.getOffset().getTotalSeconds();
        int offsetHours = totalSeconds / 3600;
        // Giữ nguyên dấu của phút
        int offsetMinutes = (totalSeconds % 3600) / 60;

        // Bây giờ offsetHours và offsetMinutes sẽ luôn cùng dấu (hoặc bằng 0)
        ZoneOffset calculatedOffset = ZoneOffset.ofHoursMinutes(offsetHours, offsetMinutes);

        Instant currentTime = Instant.now();
        LocalDate dateFromClient =
                currentTime.atZone(ZoneId.ofOffset("UTC", calculatedOffset)).toLocalDate();

        if (clientDateLearned.toLocalDate().isAfter(dateFromClient)) {
            throw new RuntimeException("Invalid date");
        }

        StreakAnalysis streakAnalysis = analysisRepository.findByUser(user)
                .orElse(StreakAnalysis.builder()
                        .user(user)
                        .longestStreak(0L)
                        .currentStreak(0L)
                        .dayLearned(0L)
                        .build());

        LocalDate currentDate =
                streakAnalysis.getLastUpdated() == null ? dateFromClient :
                        Stream.of(streakAnalysis.getLastUpdated(), dateFromClient)
                                .max(LocalDate::compareTo)
                                .orElse(dateFromClient);

//       NO CODE: Optional<StreakDetails> streakData = learningRepository.findByUserAndDateLearned(user, dateFromClient);
        Optional<StreakDetails> streakData = learningRepository.findByUserAndDateLearned(user, currentDate);

        if (streakData.isPresent()) {
            return false;
        }

//      NO CODE: StreakDetails streakDetails = StreakDetails.builder()
//                .user(user)
//                .dateLearned(dateFromClient)
//                .build();
        StreakDetails streakDetails = StreakDetails.builder()
                .user(user)
                .dateLearned(currentDate)
                .build();

        learningRepository.save(streakDetails);
//      NO CODE: handleUpdateStreakAnalysis(user, dateFromClient);
        handleUpdateStreakAnalysis(user, currentDate);
        return true;
    }

    private void handleUpdateStreakAnalysis(AppUser user, LocalDate dateFromClient) {
        StreakAnalysis streakAnalysis = analysisRepository.findByUser(user)
                .orElse(StreakAnalysis.builder()
                        .user(user)
                        .longestStreak(1L)
                        .dayLearned(0L)
                        .build());

        boolean yDayLearned = learningRepository.existsByUserAndDateLearned(user, dateFromClient.minusDays(1));

        if (!yDayLearned) {
            streakAnalysis.setCurrentStreak(1L);
        } else {
            streakAnalysis.setCurrentStreak(streakAnalysis.getCurrentStreak() + 1);
            streakAnalysis.setLongestStreak(Math.max(streakAnalysis.getLongestStreak(), streakAnalysis.getCurrentStreak()));
        }

        streakAnalysis.setDayLearned(streakAnalysis.getDayLearned() + 1L);
        streakAnalysis.setLastUpdated(dateFromClient);

        analysisRepository.save(streakAnalysis);
    }

    public StreakAnalysisResponse getAnalysisStreak(AppUser user, int offsetHours, int offsetMinutes, String localeStr) {
        Instant currentTime = Instant.now();

        LocalDate dateFromClient = currentTime.atZone(ZoneId.ofOffset("UTC",
                        ZoneOffset.ofHoursMinutes(offsetHours, offsetMinutes)))
                .toLocalDate();

        Locale locale = ibmTzLocaleUtils.getLocale(localeStr);

        return getAnalysisLearningBaseOnLocale(user, locale, dateFromClient);
    }

    private StreakAnalysisResponse getAnalysisLearningBaseOnLocale(AppUser user,
                                                                   Locale locale,
                                                                   LocalDate dateFromClient) {
        StreakAnalysis streakAnalysis = analysisRepository.findByUser(user)
                .orElse(StreakAnalysis.builder()
                        .user(user)
                        .longestStreak(0L)
                        .currentStreak(0L)
                        .dayLearned(0L)
                        .build());

        LocalDate currentDate =
                streakAnalysis.getLastUpdated() == null ? dateFromClient :
                        Stream.of(streakAnalysis.getLastUpdated(), dateFromClient)
                                .max(LocalDate::compareTo)
                                .orElse(dateFromClient);

        WeekFields weekFields = WeekFields.of(locale);
        DayOfWeek firstDayOfWeek = weekFields.getFirstDayOfWeek();

        // Calculate the first day of week
        LocalDate startOfWeek = dateFromClient.with(TemporalAdjusters.previousOrSame(firstDayOfWeek));

        // Calculate the last day of week
        LocalDate endOfWeek = startOfWeek.plusDays(6);

//       NO CODE: boolean currentDateLearned = learningRepository.existsByUserAndDateLearned(user, dateFromClient);
//
//        boolean yesterdayLearned = learningRepository.existsByUserAndDateLearned(user, dateFromClient.minusDays(1));

        boolean currentDateLearned = learningRepository.existsByUserAndDateLearned(user, currentDate);

        boolean yesterdayLearned = learningRepository.existsByUserAndDateLearned(user, currentDate.minusDays(1));

        streakAnalysis = analysisRepository.findByUser(user)
                .orElse(StreakAnalysis.builder()
                        .user(user)
                        .longestStreak(0L)
                        .currentStreak(0L)
                        .dayLearned(0L)
                        .build());

        return getCalculateStreakAnalysisResponse(user, locale, startOfWeek, endOfWeek, streakAnalysis, currentDateLearned, yesterdayLearned);
    }

    public List<StreakStatus> getLearnedDetails(AppUser user, int month, int year, String localeCode) {
        List<StreakStatus> statuses = learningRepository.getLearnedByMonthAndYear(user.getUserId(), month, year);

        Iterator<StreakStatus> statusIterator = statuses.iterator();

        Iterator<StreakStatus> transformedIterator = transformDateToDayOfWeek(statusIterator, localeCode);

        return StreamSupport.stream(
                        Spliterators.spliteratorUnknownSize(transformedIterator, Spliterator.ORDERED), false)
                .toList();
    }

    public List<StreakStatus> getAllLearnedDate(AppUser user, String localeCode) {
        List<StreakStatus> statuses = learningRepository.findAllStreakStatusByUserId(user.getUserId());

        Iterator<StreakStatus> statusIterator = statuses.iterator();

        Iterator<StreakStatus> transformedIterator = transformDateToDayOfWeek(statusIterator, localeCode);

        return StreamSupport.stream(
                        Spliterators.spliteratorUnknownSize(transformedIterator, Spliterator.ORDERED), false)
                .toList();
    }

    public Page<StreakStatus> getLearnedByDateRange(AppUser user, LocalDate startDate, LocalDate endDate,
                                                    String localeCode,
                                                    int page,
                                                    int size) {
        if (startDate.isAfter(endDate)) {
            throw new BadRequestException("Invalid date range");
        }

        if (page < 0) {
            throw new BadRequestException("Invalid page: cannot zero");
        }

        if (size <= 40 || size > 100) {
            throw new BadRequestException("Invalid size: in range [40, 100]");
        }

        Pageable pageable = PageRequest.of(page, size);

        Page<StreakStatus> statuses = learningRepository.getLearnedByDateRange(user.getUserId(), startDate, endDate, pageable);

        Iterator<StreakStatus> statusIterator = statuses.iterator();

        Iterator<StreakStatus> transformedIterator = transformDateToDayOfWeek(statusIterator, localeCode);

        List<StreakStatus> transformedStatuses = StreamSupport.stream(
                        Spliterators.spliteratorUnknownSize(transformedIterator, Spliterator.ORDERED), false)
                .toList();

        return new PageImpl<>(transformedStatuses, pageable, statuses.getTotalElements());
    }

    public StreakAnalysisResponse getAnalysisStreakV2(AppUser user,
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

        return getAnalysisLearningBaseOnLocaleV2(user, locale, dateFromClient);
    }

    private StreakAnalysisResponse getAnalysisLearningBaseOnLocaleV2(AppUser user, Locale locale, LocalDate dateFromClient) {
        StreakAnalysis streakAnalysis = analysisRepository.findByUser(user)
                .orElse(StreakAnalysis.builder()
                        .user(user)
                        .longestStreak(0L)
                        .currentStreak(0L)
                        .dayLearned(0L)
                        .build());

        LocalDate currentDate =
                streakAnalysis.getLastUpdated() == null ? dateFromClient :
                        Stream.of(streakAnalysis.getLastUpdated(), dateFromClient)
                                .max(LocalDate::compareTo)
                                .orElse(dateFromClient);

        DayOfWeek firstDayOfWeek = WeekFields.of(locale).getFirstDayOfWeek();

        // Calculate the first day of week
        LocalDate startOfWeek = dateFromClient.with(TemporalAdjusters.previousOrSame(firstDayOfWeek));

        // Calculate the last day of week
        LocalDate endOfWeek = startOfWeek.plusDays(6);

        boolean currentDateLearned = learningRepository.existsByUserAndDateLearned(user, currentDate);

        boolean yesterdayLearned = learningRepository.existsByUserAndDateLearned(user, currentDate.minusDays(1));

        return getCalculateStreakAnalysisResponse(user, locale, startOfWeek, endOfWeek, streakAnalysis, currentDateLearned, yesterdayLearned);
    }

    private StreakAnalysisResponse getCalculateStreakAnalysisResponse(AppUser user, Locale locale, LocalDate startOfWeek, LocalDate endOfWeek, StreakAnalysis streakAnalysis, boolean currentDateLearned, boolean yesterdayLearned) {
        List<StreakDetails> streaksOneWeek = learningRepository.findByUserAndDateLearnedBetween(user,
                startOfWeek, endOfWeek);

        List<StreakStatus> streaksStatuses = streaksOneWeek.stream()
                .map(streakDetails -> {
                    String dayFull = streakDetails.getDateLearned().getDayOfWeek().getDisplayName(TextStyle.FULL, locale);
                    String dayShort = streakDetails.getDateLearned().getDayOfWeek().getDisplayName(TextStyle.SHORT, locale);
                    return StreakStatus.builder()
                            .date(streakDetails.getDateLearned())
                            .dayFull(dayFull)
                            .dayShort(dayShort)
                            .dayFullEn(streakDetails.getDateLearned().getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.ENGLISH))
                            .dayShortEn(streakDetails.getDateLearned().getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.ENGLISH))
                            .dayRank(streakDetails.getDateLearned().getDayOfWeek().getValue())
                            .build();
                })
                .toList();

        return StreakAnalysisResponse.builder()
                .longestStreak(streakAnalysis.getLongestStreak())
                .currentStreak(currentDateLearned || yesterdayLearned ? streakAnalysis.getCurrentStreak() : 0)
                .isCurrentDateLearned(currentDateLearned)
                .dayLearned(streakAnalysis.getDayLearned())
                .streakOneWeek(streaksStatuses)
                .build();
    }


    private Iterator<StreakStatus> transformDateToDayOfWeek(Iterator<StreakStatus> streaksStatuses, String localeCode) {
        if (!StringUtils.hasText(localeCode)) {
            localeCode = "en-US";
        }
        Locale locale = ibmTzLocaleUtils.getLocale(localeCode);
        Stream<StreakStatus> transformedStream = StreamSupport.stream(
                        Spliterators.spliteratorUnknownSize(streaksStatuses, Spliterator.ORDERED), false)
                .map(streakStatus -> {
                    String dayFull = streakStatus.getDate().getDayOfWeek().getDisplayName(TextStyle.FULL, locale);
                    String dayShort = streakStatus.getDate().getDayOfWeek().getDisplayName(TextStyle.SHORT, locale);
                    return StreakStatus.builder()
                            .date(streakStatus.getDate())
                            .dayFull(dayFull)
                            .dayShort(dayShort)
                            .dayFullEn(streakStatus.getDate().getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.ENGLISH))
                            .dayShortEn(streakStatus.getDate().getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.ENGLISH))
                            .dayRank(streakStatus.getDate().getDayOfWeek().getValue())
                            .build();
                });
        return transformedStream.iterator();
    }
}
