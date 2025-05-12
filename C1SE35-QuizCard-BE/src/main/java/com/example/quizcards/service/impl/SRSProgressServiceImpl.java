package com.example.quizcards.service.impl;

import com.example.quizcards.dto.ISrsProgressAnalysisDTO;
import com.example.quizcards.dto.request.ProgressSrsRequest;
import com.example.quizcards.dto.response.SRSProgressResponse;
import com.example.quizcards.entities.SetProgressSetting;
import com.example.quizcards.exception.ResourceNotFoundException;
import com.example.quizcards.helpers.ProgressHelpers.ProgressHelpers;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;


/*
 * - SRS Hiện tại đang chưa xử lý được trường hợp người dùng chuyển đổi qua lại giữa
 * các múi giờ khác nhau, vì thế nên số lượng new card mới được lấy ra có thể khác nhau
 * dựa theo múi giờ của client.
 *
 * - Đã có cách xử lý vấn đề đó bằng cách lưu lại thời gian tối đa mà người dùng cập nhật
 * thời gian học, để sau này ví dụ có giảm múi giờ để lấy dữ liệu các card, thì phải chấp
 * nhận là sử dụng thời gian cập nhật cuối cùng vì nó là thời gian cập nhật tiến độ học mới
 * nhất
 *
 * - Có thể lưu thêm trường last_updated vào trong cái setting analysis, hoặc sử dụng redis
 * để lưu một bản ghi số card đã học tại thời điểm cập nhật cuối cùng, nhưng hiện tại chưa
 * làm vậy
 * */
@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SRSProgressServiceImpl {
    SetProgressSettingServiceImpl settingProgressService;

    ProgressHelpers progressHelpers;

    // LẤY SỐ CARD REVIEW DỰA TRÊN SỐ CARD MỖI ROUND
    // DỰA TRÊN TIME CLIENT ĐỂ TÍNH SỐ CARD NEW
    // GIẢ SỬ MÚI GIỜ ĐÃ ĐƯỢC VALIDATE MÚI GIỜ TRONG KHOẢNG TỪ -12 TỚI +14 GIỜ
    public SRSProgressResponse getProgressCardBaseOnRound(Long userId,
                                                          Long setId,
                                                          int offsetHours,
                                                          int offsetMinutes) {
        SetProgressSetting settingAnalysis =
                settingProgressService.getEffectiveSettings(setId, userId);
        return progressHelpers.getProgressCardBaseOnRound(
                userId,
                setId,
                offsetHours,
                offsetMinutes,
                settingAnalysis
        );
    }

    public ISrsProgressAnalysisDTO getSrsProgressAnalysis(Long userId,
                                                          Long setId) {
        SetProgressSetting settingAnalysis =
                settingProgressService.getEffectiveSettings(setId, userId);
        return progressHelpers.getSrsProgressAnalysis(
                userId,
                setId,
                settingAnalysis
        );
    }

    public Map<String, List<Object>> submitSRSProgresses(Long userId, ProgressSrsRequest srsRequests) {
        return progressHelpers.submitSRSProgresses(
                userId,
                srsRequests
        );
    }

    public void resetSrsProgress(Long userId, Long setId) {
        try {
            settingProgressService.softResetSrsSetting(userId, setId);
        } catch (ResourceNotFoundException e) {
            // Cannot found, nothing to do
        }
    }

    // LẤY INTERVAL GẦN NHẤT CỦA PROGRESS ÁP DỤNG CHO NHỮNG CARD CHƯA TỚI HẠN REVIEW
//   NO CODE: public long getNearestIntervalProgressInSRS(Long userId, Long setId) {
//        return getNearestIntervalProgressInSRS(userId, setId, 0);
//    }
//
//    public long getNearestIntervalProgressInSRS(Long userId, Long setId, int intervalSecondsCanSkip) {
//        return progressHelpers.getNearestIntervalProgressInSRS(
//                setId,
//                userId,
//                intervalSecondsCanSkip
//        );
//    }
//


    // CÁC ĐOẠN CODE NÀY ĐƯỢC XỬ LÝ TRƯỚC ĐÓ NHƯNG CỨ TỪ TỪ ƯU TIÊN
    /*
     * Chọn thẻ cho chế độ SRS ưu tiên Learning/Lapsed rồi Review/New(ngẫu nhiên có trọng số).
     */
//    NO CODE: private Optional<NextCardDTO> getNextCardSRSWithPriority(long userId, long deckId) {
//        Timestamp now = Timestamp.from(Instant.now());
//        int newLimit = 20;
//
//        // Learning + Lapsed đến hạn
//        List<UserCardProgress> learningLapsed =
//                progressDao.findCardsInStatesByUserIdAndDeck(userId, deckId,
//                                List.of(CardState.Learning, CardState.Lapsed))
//                        .stream()
//                        .filter(p -> p.getDueDate() != null && !p.getDueDate().after(now))
//                        .sorted(Comparator.comparing(UserCardProgress::getDueDate))
//                        .collect(Collectors.toList());
//        if (!learningLapsed.isEmpty()) {
//            UserCardProgress p = learningLapsed.getFirst();
//            return cardDao.findById(p.getCardId())
//                    .map(c -> new NextCardDTO(c, cloneProgress(p)));
//        }
//
//        // Review đến hạn
//        List<UserCardProgress> reviewDue =
//                progressDao.findDueCardsByUserIdAndDeck(userId, deckId, now)
//                        .stream()
//                        .filter(p -> p.getCardState() == CardState.Review)
//                        .collect(Collectors.toList());
//
//        // Chưa bao giờ học
//        List<Card> neverStudied =
//                progressDao.findNeverStudiedCardsByUserIdAndDeck(userId, deckId, newLimit);
//
//        // không còn gì
//        if (reviewDue.isEmpty() && neverStudied.isEmpty()) return Optional.empty();
//
//        boolean chooseReview = (!reviewDue.isEmpty()) &&
//                (neverStudied.isEmpty() ||
//                        random.nextInt(TOTAL_WEIGHT) < REVIEW_PRIORITY_WEIGHT);
//
//        if (chooseReview) {
//            UserCardProgress p = reviewDue.get(random.nextInt(reviewDue.size()));
//            return cardDao.findById(p.getCardId())
//                    .map(c -> new NextCardDTO(c, cloneProgress(p)));
//        } else {
//            Card nc = neverStudied.get(random.nextInt(neverStudied.size()));
//            UserCardProgress temp = new UserCardProgress();
//            temp.setUserId(userId);
//            temp.setCardId(nc.getCardId());
//            temp.setDeckId(deckId);
//            temp.setCardState(CardState.New);
//            temp.setDueDate(Timestamp.from(Instant.now()));
//            temp.setEaseFactor(settingsService
//                    .getEffectiveSettings(userId, deckId).getSm2StartingEase());
//            return Optional.of(new NextCardDTO(nc, temp));
//        }
//    }

//    /**
//     * Chế độ Simple – chia Unknown / Known.
//     */
//    NO CODE: private Optional<NextCardDTO> getNextCardSimpleMode(long userId, long deckId) {
//        UserDeckSettings st = settingsService.getEffectiveSettings(userId, deckId);
//
//        List<UserCardProgress> learningLapsed =
//                progressDao.findCardsInStatesByUserIdAndDeck(userId, deckId,
//                        List.of(CardState.Learning, CardState.Lapsed));
//
//        List<UserCardProgress> review =
//                progressDao.findCardsInStatesByUserIdAndDeck(userId, deckId,
//                        List.of(CardState.Review));
//
//        List<UserCardProgress> unknown = new ArrayList<>(learningLapsed);
//        List<UserCardProgress> known = new ArrayList<>();
//
//        for (UserCardProgress p : review) {
//            if (isCardUnknownBasedOnSRS(p, st)) unknown.add(p);
//            else known.add(p);
//        }
//
//        if (!unknown.isEmpty()) {
//            UserCardProgress p = unknown.get(random.nextInt(unknown.size()));
//            return cardDao.findById(p.getCardId()).map(c -> new NextCardDTO(c, cloneProgress(p)));
//        }
//
//        List<Card> never = progressDao.findNeverStudiedCardsByUserIdAndDeck(userId, deckId, Integer.MAX_VALUE);
//        if (!never.isEmpty()) {
//            Card c = never.get(random.nextInt(never.size()));
//            UserCardProgress temp = new UserCardProgress();
//            temp.setUserId(userId);
//            temp.setCardId(c.getCardId());
//            temp.setDeckId(deckId);
//            temp.setCardState(CardState.New);
//            temp.setDueDate(Timestamp.from(Instant.now()));
//            temp.setEaseFactor(st.getSm2StartingEase());
//            return Optional.of(new NextCardDTO(c, temp));
//        }
//
//        if (!known.isEmpty()) {
//            UserCardProgress p = known.get(random.nextInt(known.size()));
//            return cardDao.findById(p.getCardId()).map(c -> new NextCardDTO(c, cloneProgress(p)));
//        }
//
//        return Optional.empty();
//    }
//
//    /**
//     * Quy tắc đơn giản hoá để phân loại Unknown/Known.
//     */
//    NO CODE: private boolean isCardUnknownBasedOnSRS(UserCardProgress p, UserDeckSettings s) {
//        if (p == null) return true;
//        if (p.getCardState() == CardState.New ||
//                p.getCardState() == CardState.Learning ||
//                p.getCardState() == CardState.Lapsed) return true;
//
//        if (p.getCardState() == CardState.Review) {
//            if (p.getLapses() > 0) return true;
//            if (p.getCurrentInterval() < 7 * 86400) return true;    // <7 ngày
//            if (p.getEaseFactor() != null && p.getEaseFactor() < 1.8f) return true;
//        }
//        return false;
//    }
}
