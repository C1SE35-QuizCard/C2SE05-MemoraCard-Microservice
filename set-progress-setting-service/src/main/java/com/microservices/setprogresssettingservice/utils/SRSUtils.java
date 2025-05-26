package com.microservices.setprogresssettingservice.utils;

import com.microservices.setprogresssettingservice.dto.request.SetProgressSettingRequest;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.List;

public class SRSUtils {
    public static void validateSettings(SetProgressSettingRequest settings) {
        if (
                settings.getNewCardsRatio() < 5 ||
                        settings.getNewCardsRatio() > 95 ||
                        settings.getDueCardsRatio() < 5 ||
                        settings.getDueCardsRatio() > 95 ||
                        settings.getNewCardsRatio() + settings.getDueCardsRatio() != 100) {
            if (settings.getIsAutomaticSelectCard()) {
                settings.setNewCardsRatio(50);
                settings.setDueCardsRatio(50);
            } else {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        MessageFormat.format("Validation failed in new_cards_ratio ({0}) and due_cards_ratio ({1}): " +
                                        "each must be in range [5, 95] and sum = 100",
                                settings.getNewCardsRatio(), settings.getDueCardsRatio()));
            }
        }
        validateIntervals(settings, settings.getMinIntervalGap());
    }

    private static void validateIntervals(SetProgressSettingRequest settings, Long minGap) {
        // Đọc tất cả 4 giá trị vào mảng
        Long[] values = {
                settings.getCustomIntervalAgainSeconds(),
                settings.getCustomIntervalHardSeconds(),
                settings.getCustomIntervalGoodSeconds(),
                settings.getCustomIntervalEasySeconds()
        };
        String[] names = {
                "custom_interval_again",
                "custom_interval_hard",
                "custom_interval_good",
                "custom_interval_easy"
        };

        // Tính ngưỡng tối thiểu cho từng giá trị
        long[] thresholds = new long[values.length];
        thresholds[0] = 1;  // again phải >= 1
        for (int i = 1; i < values.length; i++) {
            thresholds[i] = values[i - 1] + minGap;
        }

        // Duyệt kiểm tra
        for (int i = 0; i < values.length; i++) {
            Long v = values[i];
            if (v == null || v < thresholds[i]) {
                if (i == 0) {
                    throw new ResponseStatusException(
                            HttpStatus.BAD_REQUEST,
                            String.format("Validation failed: %s must be >= %d",
                                    names[i], thresholds[i])
                    );
                }
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        String.format(
                                "Validation failed: %s (%d) must be >= %s (%d) + %d",
                                names[i], v, names[i - 1], values[i - 1], minGap
                        )
                );
            }
        }
    }

    public static List<Integer> parseRatioMix(String stepsString) {
        if (!StringUtils.hasText(stepsString)) {
            return List.of(50, 50); // Trả về mặc định nếu không hợp lệ
        }
        try {
            List<Integer> ratios = Arrays.stream(stepsString.trim().split("\\s+"))
                    .map(Integer::parseInt)
                    .toList();

            if (ratios.size() != 2 ||
                    ratios.stream().anyMatch(i -> i < 0) ||
                    ratios.stream().mapToInt(Integer::intValue).sum() != 100) {
                throw new IllegalArgumentException("Invalid input: must have 2 non-negative numbers summing to 100.");
            }

            return ratios;
        } catch (IllegalArgumentException i) {
            return List.of(50, 50); // Trả về mặc định nếu không hợp lệ
        }
    }
}
