package com.example.quizcards.utils;

import com.example.quizcards.dto.IUserSRSProgressDTO;
import com.example.quizcards.dto.request.SetProgressSettingRequest;
import com.example.quizcards.exception.BadRequestException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

@Slf4j
public class SRSUtils {
    /**
     * Chuyển chuỗi steps (vd: "60 600") thành List<Long> giây
     */
    public static List<Long> parseSteps(String stepsString) {
        if (stepsString == null || stepsString.trim().isEmpty()) {
            return Collections.emptyList();
        }
        try {
            return Arrays.stream(stepsString.trim().split("\\s+"))
                    .map(Long::parseLong)
                    .filter(i -> i > 0) // Chỉ lấy step > 0
                    .toList();
        } catch (NumberFormatException n) {
//            log.error("Failed to parse steps string: '{}'", stepsString, n);
            return Collections.emptyList();
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

    /**
     * Chuyển List<Long> giây thành chuỗi steps
     */
    public static String formatSteps(List<Integer> stepsList) {
        if (stepsList == null || stepsList.isEmpty()) {
            return "";
        }
        return stepsList.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(" "));
    }

    /**
     * Format số giây thành chuỗi thân thiện (vd: 1m, 10m 30s, 1d, 4d 12h)
     */
    public static String formatInterval(Long seconds) {
        if (seconds == null || seconds <= 0) return "?";

        long days = TimeUnit.SECONDS.toDays(seconds);
        long hours = TimeUnit.SECONDS.toHours(seconds) % 24;
        long minutes = TimeUnit.SECONDS.toMinutes(seconds) % 60;
        long secs = seconds % 60;

        StringBuilder sb = new StringBuilder();
        if (days > 0) sb.append(days).append("d ");
        if (hours > 0) sb.append(hours).append("h ");
        // Hiển thị phút nếu lớn hơn 0 HOẶC nếu là đơn vị lớn nhất (vd: chỉ có phút và giây)
        if (minutes > 0 || (days == 0 && hours == 0 && secs > 0)) {
            if (days > 0 || hours > 0) { // Chỉ thêm nếu đã có ngày hoặc giờ
                sb.append(minutes).append("m ");
            } else { // Nếu phút là đơn vị lớn nhất
                sb.append(minutes).append("m");
                // Chỉ hiển thị giây nếu có cả phút và giây (và không có ngày/giờ)
                if (secs > 0) sb.append(" ").append(secs).append("s");
            }
        }
        // Chỉ hiển thị giây nếu nó là đơn vị duy nhất
        if (days == 0 && hours == 0 && minutes == 0 && secs > 0) {
            sb.append(secs).append("s");
        }

        // Xử lý trường hợp quá ngắn hoặc chỉ có giây lẻ sau khi làm tròn
        if (sb.isEmpty()) {
            sb.append(seconds).append("s"); // Fallback hiển thị giây
        }

        return sb.toString().trim();
    }

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
                throw new BadRequestException(
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
                    throw new BadRequestException(
                            String.format("Validation failed: %s must be >= %d",
                                    names[i], thresholds[i])
                    );
                }
                throw new BadRequestException(
                        String.format(
                                "Validation failed: %s (%d) must be >= %s (%d) + %d",
                                names[i], v, names[i - 1], values[i - 1], minGap
                        )
                );
            }
        }
    }

    private static Long validateMinGap(Long minGap) {
        if (Objects.isNull(minGap) || minGap < 0) {
            throw new BadRequestException(
                    MessageFormat.format("Validation failed: min_interval_gap ({0}) cannot be negative.", minGap)
            );
        }
        return minGap;
    }

    // ===== CÁC HÀM HELPERS HỖ TRỢ CHO VIỆC TRANSFORM =====

    // Helper: kiểm tra các giá trị không âm
    private static void validateNonNegative(long... values) {
        for (long v : values) {
            if (v < 0) {
                throw new IllegalArgumentException("Counts must be non-negative: " + v);
            }
        }
    }

    // Helper: kiểm tra ratio trong khoảng 0–100 và tổng ≤100
    private static void validateRatios(int ratioNew, int ratioDue) {
        if (ratioNew < 0 || ratioNew > 100
                || ratioDue < 0 || ratioDue > 100
                || ratioNew + ratioDue > 100
        ) {
            throw new IllegalArgumentException(
                    String.format("Invalid ratios: new=%d, due=%d (each 0–100, sum ≤100)",
                            ratioNew, ratioDue));
        }
    }

    // Helper: tính tỷ lệ phần trăm giữa hai số
    private static double getPercent(long first, long sec) {
        // validate
        if (first < 0 || sec < 0) {
            throw new IllegalArgumentException("Counts must be non-negative");
        }
        // Nếu first = 0 hoặc second = 0, trả về 0% hoặc 100%
        if (first == 0 || sec == 0) {
            return first == 0 ? 0.0 : 100.0;
        }
        // tính percent = first / (first+sec) * 100, làm tròn 2 chữ số
        BigDecimal bdFirst = BigDecimal.valueOf(first);
        BigDecimal bdTotal = BigDecimal.valueOf(first + sec);
        BigDecimal percent = bdFirst
                .multiply(BigDecimal.valueOf(100))
                .divide(bdTotal, 2, RoundingMode.HALF_UP);
        return percent.doubleValue();
    }

    // ==== TRANSFORM FUNCTION =====
    // Được sử dụng để hỗ trợ select số lượng new card mới - số lượng card tới hạn mới

    // HÀM CHUNG: phân phối new/due dựa trên hàm percentFunction cung cấp
    private static List<Long> distributeCards(
            long initNumNew,
            long initNumReview,
            long initNumCards,
            BiFunction<Long, Long, Double> percentFunction
    ) {
        // nếu không có thẻ nào cả
        if (initNumNew == 0 && initNumReview == 0) {
            return List.of(0L, 0L);
        }

        long numNew = initNumNew;
        long numReview = initNumReview;
        long maxCards = Math.min(initNumCards, numNew + numReview);

        long countNew = 0;
        long countReview = 0;

//        double initialPercent = percentFunction.apply(numNew, numReview);
//        System.out.println("Percent new cards per day: " + initialPercent);

        for (int i = 0; i < maxCards; i++) {
            double percent = percentFunction.apply(numNew, numReview);
            boolean pickNew = ThreadLocalRandom.current().nextDouble() <= (percent / 100.0);

            if (pickNew) {
                countNew++;
                numNew = Math.max(0, numNew - 1);
            } else {
                countReview++;
                numReview = Math.max(0, numReview - 1);
            }
        }

        return List.of(countNew, countReview);
    }

    // HÀM 1: Phân phối dựa trên ratio cố định (initRatioNew/initRatioDue)
    private static List<Long> distributeCardsByFixedRatio(
            long initNumNewCardsPerDay,
            long initNumReviewCards,
            long initNumCards,
            int initRatioNewCardsPerDay,
            int initRatioDueDateCards // không dùng, chỉ để validate
    ) {
        // validate đầu vào
        validateNonNegative(initNumNewCardsPerDay, initNumReviewCards, initNumCards);
        validateRatios(initRatioNewCardsPerDay, initRatioDueDateCards);

        // percentFunction cho fixed ratio:
        //   - nếu new = 0 ➔ 0%
        //   - nếu due = 0 ➔ 100%
        //   - else ➔ luôn initRatioNewCardsPerDay
        BiFunction<Long, Long, Double> v1Percent = (newCnt, dueCnt) -> {
            if (newCnt == 0) return 0.0;
            else if (dueCnt == 0) return 100.0;
            else return (double) initRatioNewCardsPerDay;
        };

        return distributeCards(
                initNumNewCardsPerDay,
                initNumReviewCards,
                initNumCards,
                v1Percent
        );
    }

    // HÀM 2: Phân phối dựa trên tỷ lệ giữa hai bên (theo số lượng còn lại)
    private static List<Long> distributeCardsByActualRatio(
            long initNumNewCardsPerDay,
            long initNumReviewCards,
            long initNumCards
    ) {
        // validate đầu vào
        validateNonNegative(initNumNewCardsPerDay, initNumReviewCards, initNumCards);

        BiFunction<Long, Long, Double> v2Percent = SRSUtils::getPercent;

        return distributeCards(
                initNumNewCardsPerDay,
                initNumReviewCards,
                initNumCards,
                v2Percent
        );
    }

    // HÀM LẤY PHÂN PHỐI THỰC SỰ: Nhận các tham số đầu vào và tạo số lượng card
    // lấy ra ở mỗi bên cuối cùng là như nào
    // Hàm quyết định sẽ chọn thuật toán phân phối nào dựa trên biến isAutomaticPriority
    public static List<Long> getNumNewCardsAndNumReviewCards(
            long initNumNewCards,
            long initNumReviewCards,
            long totalCards,
            int ratioNew,
            int ratioReview,
            boolean isAutomaticPriority
    ) {
        // 1. Validate chung
        if (initNumNewCards < 0 || initNumReviewCards < 0 || totalCards < 0) {
            throw new IllegalArgumentException("Counts must be non-negative");
        }
        // 2. Nếu dùng fixed‐ratio thì validate thêm
        if (!isAutomaticPriority) {
            if (ratioNew < 0 || ratioNew > 100 ||
                    ratioReview < 0 || ratioReview > 100 ||
                    ratioNew + ratioReview > 100
            ) {
                throw new IllegalArgumentException(
                        "Ratios must be 0–100 and sum ≤ 100");
            }
        }
        // 3. Không có thẻ nào cả
        if (initNumNewCards == 0 && initNumReviewCards == 0) {
            return List.of(0L, 0L);
        }
        // 4. Chọn thuật toán
        if (isAutomaticPriority) {
            return distributeCardsByActualRatio(initNumNewCards, initNumReviewCards, totalCards);
        } else {
            return distributeCardsByFixedRatio(
                    initNumNewCards, initNumReviewCards, totalCards, ratioNew, ratioReview);
        }
    }

    // HÀM TRỘN DỰA THEO TỶ LỆ MIX
    public static List<IUserSRSProgressDTO> shuffleProgressesByRatioMix(
            List<IUserSRSProgressDTO> newCards,
            List<IUserSRSProgressDTO> reviewCards,
            int ratioNew,
            int ratioReview,
            boolean isAutomaticPriority
    ) {
        // 1. Validate đầu vào
        if (newCards == null || reviewCards == null) {
            throw new IllegalArgumentException("New and due cards cannot be null");
        }
        if (!isAutomaticPriority && ratioNew < 0 || ratioNew > 100 ||
                ratioReview < 0 || ratioReview > 100 ||
                ratioNew + ratioReview > 100
        ) {
            throw new IllegalArgumentException(
                    "Ratios must be 0–100 and sum ≤ 100");
        }
        // Clone 2 danh sách
        List<IUserSRSProgressDTO> newCardsClone = new ArrayList<>(newCards);
        List<IUserSRSProgressDTO> reviewCardsClone = new ArrayList<>(reviewCards);

        // Tạo mảng kết quả
        List<IUserSRSProgressDTO> result = new ArrayList<>();

        // Tính tỉ lệ phân phối
        // Nếu là tự động thì dùng actual ratio
        // Nếu không thì dùng tỉ lệ cố định

        // Chọn hàm tính percent:
        BiFunction<Long, Long, Double> percentFn = isAutomaticPriority
                // tự động: new/(new+due)
                ? SRSUtils::getPercent
                // fixed: nếu new hết thì 0%, nếu due hết thì 100%, else luôn ưu tiên ratioNew
                : (n, d) -> n == 0 ? 0.0
                : d == 0 ? 100.0
                : (double) ratioNew;

        // Tính tổng số lượng thẻ
        int totalCards = newCardsClone.size() + reviewCardsClone.size();

        // Duyệt qua tổng tất cả thẻ, lấy thẻ mới hoặc thẻ đến hạn dựa trên tỉ lệ, rồi đưa vào mảng result
        for (int i = 0; i < totalCards; i++) {
            // Tính tỉ lệ
            double percent = percentFn.apply((long) newCardsClone.size(), (long) reviewCardsClone.size());
            boolean pickNew = ThreadLocalRandom.current().nextDouble() <= (percent / 100.0);
            if (pickNew && !newCardsClone.isEmpty()) {
                result.add(newCardsClone.removeLast());
            } else if (!reviewCardsClone.isEmpty()) {
                result.add(reviewCardsClone.removeLast());
            }
        }

        return result;
    }

}
