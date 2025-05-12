package com.example.quizcards.helpers.ProgressHelpers;

import com.example.quizcards.dto.IProgressDTO;
import com.example.quizcards.dto.ISrsProgressAnalysisDTO;
import com.example.quizcards.dto.IUserSRSProgressDTO;
import com.example.quizcards.dto.request.ProgressSrsRequest;
import com.example.quizcards.dto.request.UserProgressRequest;
import com.example.quizcards.dto.response.SRSProgressResponse;
import com.example.quizcards.entities.*;
import com.example.quizcards.entities.processEntities.enums.CardState;
import com.example.quizcards.entities.processEntities.enums.ReviewRating;
import com.example.quizcards.exception.ErrorsDataException;
import com.example.quizcards.repository.IFlashcardRepository;
import com.example.quizcards.repository.IUserProgressRepository;
import com.example.quizcards.repository.IUserSRSProgressRepository;
import com.example.quizcards.service.impl.SetProgressSettingServiceImpl;
import com.example.quizcards.utils.SRSUtils;
import com.ibm.icu.impl.Pair;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.data.projection.ProjectionFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProgressHelpers {
    float MIN_EASE_FACTOR = 1.3f;
    float MAX_EASE_FACTOR = 2.5f; // UPPER BOUND EASE FACTOR
    float EASE_PENALTY_AGAIN = 0.20f;
    float EASE_PENALTY_HARD = 0.15f;
    float EASE_BONUS_EASY = 0.15f;

    int FUZZ_FACTOR_PERCENT = 5;

    /**
     * Ngưỡng liên tiếp “Again” trong trạng thái Lapsed để quay lại Learning
     */
    int LAPSED_TO_LEARNING_THRESHOLD = 2;

    Long MINIMUM_MIN_GAP_SECONDS = 25L;

    Long MIN_DAY_SECONDS = 86400L;

    int MAX_CONSECUTIVE_BEFORE_KNOWN = 3;

    int MAX_CONSECUTIVE_HARD_BEFORE_UNKNOWN = 2;

    String jvmOffsetString = ZoneId.systemDefault().getRules()
            .getOffset(Instant.now()).getId();

    // Services

    IUserProgressRepository progressRepo;

    IUserSRSProgressRepository srsProgressRepo;

    SetProgressSettingServiceImpl settingProgressService;

    IFlashcardRepository flashcardRepository;

    ProjectionFactory projectionFactory;

    @PersistenceContext
    private EntityManager em;

    @Qualifier("securityContextExecutor")
    AsyncTaskExecutor taskExecutor;

    // END SERVICE

    // ===== SELECT FUNCTION =====
    // Get analysis srs progress
    public ISrsProgressAnalysisDTO getSrsProgressAnalysis(Long userId,
                                                          Long setId,
                                                          SetProgressSetting setting) {
        Long currentSrsVersion = setting.getCurrentSrsVersion();
        Future<Long> numNewCardsFut = taskExecutor.submit(() ->
                srsProgressRepo.countNewCardsByUserIdSetId(
                        userId,
                        setId,
                        currentSrsVersion
                )
        );
        Future<Long> numLearningCardsFut = taskExecutor.submit(() ->
                srsProgressRepo.countSrsProgressesWhereStatesIn(
                        userId,
                        setId,
                        currentSrsVersion,
                        List.of(CardState.New.name(), CardState.Learning.name())
                )
        );
        Future<Long> numAlmostDoneCardsFut = taskExecutor.submit(() ->
                srsProgressRepo.countSrsProgressesWhereStatesIn(
                        userId,
                        setId,
                        currentSrsVersion,
                        List.of(CardState.Lapsed.name())
                )
        );
        Future<Long> numMasteredCardsFut = taskExecutor.submit(() ->
                srsProgressRepo.countSrsProgressesWhereStatesIn(
                        userId,
                        setId,
                        currentSrsVersion,
                        List.of(CardState.Review.name())
                )
        );
        Long numNewCards = null;
        Long numLearningCards = null;
        Long numAlmostDoneCards = null;
        Long numMasteredCards = null;
        try {
            numNewCards = numNewCardsFut.get();
            numLearningCards = numLearningCardsFut.get();
            numAlmostDoneCards = numAlmostDoneCardsFut.get();
            numMasteredCards = numMasteredCardsFut.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new ErrorsDataException(
                    "Error while loading progresses",
//                    Map.of("error", e.getMessage()),
                    Map.of(),
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
        Map<String, Object> values = new HashMap<>();
        values.put("numNewCards", numNewCards);
        values.put("numLearningCards", numLearningCards);
        values.put("numAlmostDoneCards", numAlmostDoneCards);
        values.put("numMasteredCards", numMasteredCards);
        return projectionFactory.createProjection(
                ISrsProgressAnalysisDTO.class,
                values
        );
    }


    // Load simple card


    // Load card due date method base of round(SRS)
    public SRSProgressResponse getProgressCardBaseOnRound(Long userId,
                                                          Long setId,
                                                          int offsetHours,
                                                          int offsetMinutes,
                                                          SetProgressSetting setting) {
        /*
        *
        B1: ĐẾM SỐ CARD VỪA MỚI HỌC (TỨC LÀ BAN ĐẦU CARD ĐÓ CHƯA CÓ TRẠNG THÁI NHƯNG NGƯỜI DÙNG VỪA MỚI HỌC)
              CỦA NGƯỜI DÙNG DỰA TRÊN MÚI GIỜ HIỆN TẠI CỦA NGƯỜI DÙNG VÀ INSTANT NOW (ĐẾM TRONG DB)
         B2: LẤY MAX(NEW CARD TODAY TRONG SETTING - CARD VỪA MỚI HỌC SAU KHI ĐẾM, 0) -> CARD NEW CÒN LẠI TRONG NGÀY
         B3: ĐẾM SỐ CARD TỚI HẠN CẦN ÔN VỚI VERSION TRÙNG VỚI VERSION HIỆN TẠI TRONG SETTING
         B4: DỰA VÀO CARD PER ROUND, TỈ LỆ RATIO - MIX, VÀ 2 GIÁ TRỊ ĐÃ TÍNH TOÁN,
              SỬ DỤNG VÒNG FOR VỚI XÁC SUẤT SETUP TRƯỚC ĐỂ TÍNH TOÁN CÁI SỐ CARD NEW THỰC TẾ, VÀ
              SỐ CARD TỚI HẠN CẦN ÔN THỰC TẾ, CẦN LẤY RA
         B5: LẤY RA CÁC CARD NEW VÀ CARD TỚI HẠN VỚI LIMIT CHÍNH LÀ
              SỐ CARD NEW THỰC TẾ VÀ SỐ CARD TỚI HẠN THỰC TẾ, ĐÃ TÍNH TOÁN TỪ BƯỚC 4
         B6: SORT HIỂN THỊ THEO TỈ LỆ RATIO MIX BAN ĐẦU ĐÃ SETUP VÀ CÔNG THỨC XÁC SUẤT THỐNG KÊ Ở TRÊN
         B7: TRẢ VỀ KẾT QUẢ
        * */


        // Trước khi truy vấn, sử dụng cùng một currentUtcSkip cho các hàm truy vấn liên quan luôn

        Instant currentUtc = Instant.now();

        // Cộng thêm skip time
        Instant currentUtcSkip = currentUtc.plusSeconds(setting.getIntervalSecondsCanSkip());

//        // THỰC HIỆN B1:
//       NO CODE: Long countCardsJustLearned = countCardsJustLearnedAtGivenTime(
//                userId,
//                setId,
//                offsetHours,
//                offsetMinutes,
//                setting.getCurrentSrsVersion(),
//                currentUtc
//        );
//
//        // THỰC HIỆN B2:
//        NO CODE: long maxCardsInSet = flashcardRepository.countNumberOfCardsInSet(setId);
//
//        NO CODE: long realNewCardsPerDay = Math.min(
//                setting.getNewCardsPerDay(),
//                maxCardsInSet
//        );
//
//        NO CODE: long newCardsRemainToday = Math.max(
//                realNewCardsPerDay - countCardsJustLearned,
//                0L
//        );
//
//        // THỰC HIỆN B3:
//        NO CODE: Long countCardsToReview = srsProgressRepo.countCardsDueDateBeforeTime(
//                userId,
//                setId,
//                setting.getCurrentSrsVersion(),
//                currentUtcSkip
//        );

        Future<Long> countCardsJustLearnedFut = taskExecutor.submit(() ->
                countCardsJustLearnedAtGivenTime(
                        userId,
                        setId,
                        offsetHours,
                        offsetMinutes,
                        setting.getCurrentSrsVersion(),
                        currentUtc
                )
        );

        Future<Long> maxCardsInSetFut = taskExecutor.submit(() ->
                Long.valueOf(flashcardRepository.countNumberOfCardsInSet(setId))
        );

        Future<Long> countCardsToReviewFut = taskExecutor.submit(() ->
                srsProgressRepo.countCardsDueDateBeforeTime(
                        userId,
                        setId,
                        setting.getCurrentSrsVersion(),
                        currentUtcSkip
                )
        );

        Long countCardsJustLearned = null;
        Long maxCardsInSet = null;
        Long countCardsToReview = null;

        try {
            countCardsJustLearned = countCardsJustLearnedFut.get();
            maxCardsInSet = maxCardsInSetFut.get();
            countCardsToReview = countCardsToReviewFut.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new ErrorsDataException(
                    "Error while loading progresses",
//                    Map.of("error", e.getMessage()),
                    Map.of(),
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }

        long realNewCardsPerDay = Math.min(
                setting.getNewCardsPerDay(),
                maxCardsInSet
        );

        long newCardsRemainToday = Math.max(
                realNewCardsPerDay - countCardsJustLearned,
                0L
        );

        // Trước bước 4, nếu cả 2 là 0
        if (newCardsRemainToday == 0 && countCardsToReview == 0) {
            // CŨ: return mảng rỗng
            //      return List.of();
            // MỚI: lấy thời gian đến card ôn gần nhất với instant hiện tại, và quăng lỗi ErrorDataException

            long nearestInterval = internalGetNearestIntervalProgressInSRS(
                    userId,
                    setId,
                    currentUtcSkip,
                    setting.getCurrentSrsVersion()
            );
            throw new ErrorsDataException(
                    "No cards to review, please try again later",
                    Map.of(
                            "nearestInterval", nearestInterval,
                            "currentUtcSkip", currentUtcSkip
                    ),
                    HttpStatus.TOO_EARLY
            );
        }

        // THỰC HIỆN B4:
        // 1. Lấy tỉ lệ ratio mix đã setup trong setting, parse nó ra rồi lấy 2 giá trị tỉ lệ
        //      theo thứ tự: new card per day - card due date
        List<Integer> ratios = SRSUtils.parseRatioMix(setting.getRatioMix());

        // Giải nén tỷ lệ cả 2
        Integer newCardsRatio = ratios.get(0);
        Integer reviewCardsRatio = ratios.get(1);

        // 2. Biến đổi và tính toán dựa trên logic chọn theo phần trăm
        //     Hàm biến đổi đã được triển khai trong lớp SRSUtils, lấy ra mà dùng
        List<Long> newAndReviewCards = SRSUtils.getNumNewCardsAndNumReviewCards(
                newCardsRemainToday,
                countCardsToReview,
                setting.getCardsPerRound(),
                newCardsRatio,
                reviewCardsRatio,
                setting.getIsAutomaticSelectCard()
        );

        // 3. Lấy ra số lượng card new và card due date thực tế
        Long numNewCards = newAndReviewCards.getFirst();
        Long numReviewCards = newAndReviewCards.getLast();

        // THỰC HIỆN B5:
//        // 1. Lấy ra danh sách card new
//        NO CODE: List<IUserSRSProgressDTO> newCardsList = srsProgressRepo
//                .getNewCardsByUserIdSetId(
//                        userId,
//                        setId,
//                        setting.getCurrentSrsVersion(),
//                        numNewCards
//                );
//        NO CODE: newCardsList = handleAssignBonusFieldOnNewCards(newCardsList, setting);
//        // 2. Lấy ra danh sách card due date
////        NO CODE: List<IUserSRSProgressDTO> cardsToReview = upsrsRepository
////                .getDueCardsPriority(
////                        userId,
////                        setId,
////                        setting.getCurrentSrsVersion(),
////                        currentUtcSkip,
////                        numReviewCards
////                );
//
//        CardsToReviewDTO cardsToReview = loadDueDateCards(
//                userId,
//                setId,
//                setting.getCurrentSrsVersion(),
//                currentUtcSkip,
//                numReviewCards
//        );
//
//        List<IUserSRSProgressDTO> cardsToReviewMixing = cardsToReview.mixing();

        Future<List<IUserSRSProgressDTO>> newCardsListFut = taskExecutor.submit(() ->
                srsProgressRepo
                        .getNewCardsByUserIdSetId(
                                userId,
                                setId,
                                setting.getCurrentSrsVersion(),
                                numNewCards
                        )
        );

        Future<CardsToReviewDTO> cardsToReviewFut = taskExecutor.submit(() ->
                loadDueDateCards(
                        userId,
                        setId,
                        setting.getCurrentSrsVersion(),
                        currentUtcSkip,
                        numReviewCards
                )
        );


        List<IUserSRSProgressDTO> newCardsList = null;
        CardsToReviewDTO cardsToReview = null;

        try {
            newCardsList = newCardsListFut.get();
            cardsToReview = cardsToReviewFut.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new ErrorsDataException(
                    "Error while loading progresses",
//                    Map.of("error", e.getMessage()),
                    Map.of(),
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }

        newCardsList = handleAssignBonusFieldOnNewCards(newCardsList, setting);

        List<IUserSRSProgressDTO> cardsToReviewMixing = cardsToReview.mixing();

        // THỰC HIỆN B6:
        // - Ghép 2 danh sách lại với nhau, và hiển thị dựa theo tỷ lệ mix
        List<IUserSRSProgressDTO> progresses = SRSUtils.shuffleProgressesByRatioMix(
                newCardsList,
                cardsToReviewMixing,
                newCardsRatio,
                reviewCardsRatio,
                setting.getIsAutomaticSelectCard()
        );

        // B7 - Trả về kết quả
        return SRSProgressResponse.builder()
                .progresses(progresses)
                .numCardsPerRound(progresses.size())
                .userSetupNumCardsPerRound(setting.getCardsPerRound())
                .numCardsToReview(cardsToReview.size())
                .numNewCardsNow(newCardsList.size())
                .build();
    }

    // ===== select helper function =====
    // HÀM SỬ DỤNG CHO BƯỚC 1 - SRS
    private Long countCardsJustLearnedAtGivenTime(Long userId,
                                                  Long setId,
                                                  int offsetHours,
                                                  int offsetMinutes,
                                                  Long version,
                                                  Instant currentUtc) {
        // b1: tính toán giờ hiện tại của client dựa trên offset hours và offset minutes
        ZonedDateTime startTime = ZonedDateTime
                .ofInstant(currentUtc, ZoneOffset.ofHoursMinutes(offsetHours, offsetMinutes))
                .toLocalDate()
                .atStartOfDay(ZoneOffset.ofHoursMinutes(offsetHours, offsetMinutes));
        ZonedDateTime endTime = startTime.plusDays(1);

        // b3: chuyển đổi thời gian bắt đầu và kết thúc thành Instant
        Instant startOfDayInstant = startTime.toInstant();
        Instant endOfDayInstant = endTime.toInstant();

        // b4: lấy ra số lượng thẻ đã học trong khoảng thời gian này
        return srsProgressRepo.countNewCardsLearnedBetweenTimes(
                userId,
                setId,
                version,
                startOfDayInstant,
                endOfDayInstant
        );
    }

    private List<IUserSRSProgressDTO> handleAssignBonusFieldOnNewCards(List<IUserSRSProgressDTO> newProgresses,
                                                                       SetProgressSetting setting) {
        // convert to List UserSRSProgressDTO
        List<IUserSRSProgressDTO.UserSRSProgressDTO> newProgressesDTO = new ArrayList<>();
        for (IUserSRSProgressDTO progress : newProgresses) {
            var newDTO = IUserSRSProgressDTO.UserSRSProgressDTO.clone(progress);
            newDTO.setOldNextIntervalAgain(setting.getCustomIntervalAgainSeconds());
            newDTO.setOldNextIntervalHard(setting.getCustomIntervalHardSeconds());
            newDTO.setOldNextIntervalGood(setting.getCustomIntervalGoodSeconds());
            newDTO.setOldNextIntervalEasy(setting.getCustomIntervalEasySeconds());
            newProgressesDTO.add(newDTO);
        }
        return new ArrayList<>(newProgressesDTO);
    }

    private long internalGetNearestIntervalProgressInSRS(Long userId,
                                                         Long setId,
                                                         Instant instant,
                                                         Long srsVersion) {
        return srsProgressRepo
                .getNearestIntervalSecondsOnSRS(
                        userId,
                        setId,
                        srsVersion,
                        instant
                ).orElse(0L);
    }

    static class CardsToReviewDTO {
        List<IUserSRSProgressDTO> newCards;
        List<IUserSRSProgressDTO> lapsedCards;
        List<IUserSRSProgressDTO> reviewCards;

        public CardsToReviewDTO(List<IUserSRSProgressDTO> newCards,
                                List<IUserSRSProgressDTO> lapsedCards,
                                List<IUserSRSProgressDTO> reviewCards) {
            this.newCards = newCards;
            this.lapsedCards = lapsedCards;
            this.reviewCards = reviewCards;
        }

        public List<IUserSRSProgressDTO> mixing() {
            List<IUserSRSProgressDTO> allCards = new ArrayList<>();
            allCards.addAll(newCards);
            allCards.addAll(lapsedCards);
            allCards.addAll(reviewCards);
            return allCards;
        }

        public int size() {
            return newCards.size() + lapsedCards.size() + reviewCards.size();
        }

        public static CardsToReviewDTO EMPTY_DATA =
                new CardsToReviewDTO(
                        List.of(),
                        List.of(),
                        List.of()
                );
    }

    private CardsToReviewDTO loadDueDateCards(
            Long userId,
            Long setId,
            Long srsVersion,
            Instant currentUtc,
            Long numberOfCards
    ) {
        if (numberOfCards == null || numberOfCards <= 0) {
            return CardsToReviewDTO.EMPTY_DATA;
        }
        // Lấy new - learning card
        List<IUserSRSProgressDTO> learningCards =
                srsProgressRepo.getListCardProgressesDueDateAndStatesIn(
                        userId,
                        setId,
                        srsVersion,
                        List.of(CardState.New.name(), CardState.Learning.name()),
                        currentUtc,
                        numberOfCards,
                        jvmOffsetString
                );
        // Lấy lapsed card
        List<IUserSRSProgressDTO> lapsedCards =
                srsProgressRepo.getListCardProgressesDueDateAndStatesIn(
                        userId,
                        setId,
                        srsVersion,
                        List.of(CardState.Lapsed.name()),
                        currentUtc,
                        Math.max(0L, numberOfCards - learningCards.size()),
                        jvmOffsetString
                );
        // Lấy review card
        List<IUserSRSProgressDTO> reviewCards =
                srsProgressRepo.getListCardProgressesDueDateAndStatesIn(
                        userId,
                        setId,
                        srsVersion,
                        List.of(CardState.Review.name()),
                        currentUtc,
                        Math.max(0L, numberOfCards - learningCards.size() - lapsedCards.size()),
                        jvmOffsetString
                );
        return new CardsToReviewDTO(
                learningCards,
                lapsedCards,
                reviewCards
        );
    }

    // ===== END SELECT HELPER =====

    // ===== Clamp helper =====
    private float clampEase(float ef) {
        return Math.max(MIN_EASE_FACTOR, Math.min(MAX_EASE_FACTOR, ef));
    }

    // LOADING DATA
    private Map<Pair<Long, Long>, UserProgress> loadUserProgresses(
            Long userId,
            Long setId,
            Set<Long> cardIds
    ) {
        return progressRepo
                .getProgressByUserIdSetIdCardIds(
                        userId,
                        setId,
                        cardIds
                )
                .stream()
                .map(IProgressDTO::toUserProgress)
                .collect(Collectors.toMap(
                        k -> Pair.of(k.getAppUser().getUserId(), k.getFlashcard().getCardId()),
                        Function.identity()
                ));
    }

    private Map<Pair<Long, Long>, UserSRSProgress> loadUserProgressesWithSRS(
            Long userId,
            Long setId,
            Set<Long> cardIds
    ) {
        return srsProgressRepo
                .getSRSProgressByUserIdSetIdCardIds(
                        userId,
                        setId,
                        cardIds,
                        jvmOffsetString
                )
                .stream()
                .map(IUserSRSProgressDTO::toUserProgressWithSRS)
                .collect(Collectors.toMap(k -> Pair.of(
                                k.getAppUser().getUserId(), k.getFlashcard().getCardId())
                        , Function.identity()));
    }
    // ===== END LOADING DATA =====


    // PROCESS REVIEW (BATCH)
    // Review from simple mode
    @Transactional
    public void submitSimpleModeProgresses(Long userId, UserProgressRequest.SimpleModeUserProgressRequest
            smRequest) {
        SetProgressSetting settingAnalysis =
                settingProgressService.getEffectiveSettings(smRequest.setId(), userId);
        // Map to card ids to request
        Set<Long> cardIds = smRequest.progresses()
                .stream()
                .map(UserProgressRequest::getCardId)
                .collect(Collectors.toSet());
        // Loading setting analysis
        // Load info cards
        cardIds = flashcardRepository.getIdsCardBySetIdAndIdsIn(smRequest.setId(), cardIds);
//        Map<Pair<Long, Long>, UserProgress> mapProgresses = loadUserProgresses(
//                userId,
//                smRequest.setId(),
//                cardIds
//        );
//        Map<Pair<Long, Long>, UserSRSProgress> mapSrsProgresses = loadUserProgressesWithSRS(
//                userId,
//                smRequest.setId(),
//                cardIds
//        );
        Set<Long> finalCardIds = cardIds;
        Future<Map<Pair<Long, Long>, UserProgress>> mapProgressesFut = taskExecutor.submit(() ->
                loadUserProgresses(
                        userId,
                        smRequest.setId(),
                        finalCardIds
                )
        );
        Future<Map<Pair<Long, Long>, UserSRSProgress>> mapSrsProgressesFut = taskExecutor.submit(() ->
                loadUserProgressesWithSRS(
                        userId,
                        smRequest.setId(),
                        finalCardIds
                )
        );
        Map<Pair<Long, Long>, UserProgress> mapProgresses = null;
        Map<Pair<Long, Long>, UserSRSProgress> mapSrsProgresses = null;
        try {
            mapProgresses = mapProgressesFut.get();
            mapSrsProgresses = mapSrsProgressesFut.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new ErrorsDataException(
                    "Error while loading progresses",
//                    Map.of("error", e.getMessage()),
                    Map.of(),
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
        for (UserProgressRequest request : smRequest.progresses()) {
            request.setUserId(userId);
            if (cardIds.contains(request.getCardId())) {
                handleCalculateNewUserProgressWithSimpleMode(
                        request,
                        mapProgresses,
                        settingAnalysis
                );
            }
        }

        handleConvertSimpleModeRatingToSRSProgresses(
                smRequest,
                mapSrsProgresses,
                mapProgresses,
                settingAnalysis
        );

        // Save with future/async
        srsProgressRepo.saveAll(mapSrsProgresses.values());
        em.flush();
        progressRepo.saveAll(mapProgresses.values());
        em.flush();
    }

    // Review from srs mode
    // Sửa cái đoạn code trên này (nếu có logic)
    // Nhớ, chú ý trường hợp các thẻ mới đã bàn luận ở trên (đặc biệt là đối với mốc thời gian last_review_time > timeFromClient
    // Chưa bổ sung thêm, cần bổ sung: Thống kê có bao nhiêu thẻ đã thuộc và chưa thuộc
    // Trả về known/unknown với mỗi cái là integer hoặc long, đồng thời trả về mảng card id chứa các card đó
    // Lượt đánh giá Known/Unknown ở lượt chấm rất đơn giản:
    // - Known => Tương đương với Good/Easy
    // - Unknown => Tương đương với Hard/Again
    @Transactional
    public Map<String, List<Object>> submitSRSProgresses(Long userId, ProgressSrsRequest srsRequest) {
        // Map to card ids to request
        Set<Long> cardIds = srsRequest.getBatchRequests()
                .stream()
                .map(ProgressSrsRequest.SrsRequest::getCardId)
                .collect(Collectors.toSet());
        cardIds = flashcardRepository.getIdsCardBySetIdAndIdsIn(srsRequest.getSetId(), cardIds);
        // Loading setting analysis
        SetProgressSetting settingAnalysis =
                settingProgressService.getEffectiveSettings(srsRequest.getSetId(), userId);
        // Load info cards

        // Load map progresses with SRS
//        Map<Pair<Long, Long>, UserSRSProgress> mapSrsProgresses = loadUserProgressesWithSRS(
//                userId,
//                srsRequest.getSetId(),
//                cardIds
//        );
//        Map<Pair<Long, Long>, UserProgress> mapProgresses = loadUserProgresses(
//                userId,
//                srsRequest.getSetId(),
//                cardIds
//        );
        Set<Long> finalCardIds = cardIds;
        Future<Map<Pair<Long, Long>, UserSRSProgress>> mapSrsProgressesFut = taskExecutor.submit(() ->
                loadUserProgressesWithSRS(
                        userId,
                        srsRequest.getSetId(),
                        finalCardIds
                )
        );
        Future<Map<Pair<Long, Long>, UserProgress>> mapProgressesFut = taskExecutor.submit(() ->
                loadUserProgresses(
                        userId,
                        srsRequest.getSetId(),
                        finalCardIds
                )
        );
        Map<Pair<Long, Long>, UserSRSProgress> mapSrsProgresses = null;
        Map<Pair<Long, Long>, UserProgress> mapProgresses = null;
        try {
            mapSrsProgresses = mapSrsProgressesFut.get();
            mapProgresses = mapProgressesFut.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new ErrorsDataException(
                    "Error while loading progresses",
//                    Map.of("error", e.getMessage()),
                    Map.of(),
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }

        List<Object> idCardsKnown = new ArrayList<>();
        List<Object> idCardsUnknown = new ArrayList<>();

        for (ProgressSrsRequest.SrsRequest request : srsRequest.getBatchRequests()) {
            request.setUserId(userId);
            if (cardIds.contains(request.getCardId())) {
                handleCalculateNewUserProgressWithSrs(request,
                        mapSrsProgresses,
                        settingAnalysis,
                        true);
                if (request.getUserRating().equals(ReviewRating.Good.name()) ||
                        request.getUserRating().equals(ReviewRating.Easy.name())) {
                    idCardsKnown.add(request.getCardId());
                } else if (request.getUserRating().equals(ReviewRating.Hard.name()) ||
                        request.getUserRating().equals(ReviewRating.Again.name())) {
                    idCardsUnknown.add(request.getCardId());
                }
            }
        }

        handleConvertSRSRatingToSimpleModeProgresses(
                srsRequest,
                mapSrsProgresses,
                mapProgresses,
                settingAnalysis
        );

        srsProgressRepo.saveAll(mapSrsProgresses.values());
        em.flush();

        progressRepo.saveAll(mapProgresses.values());
        em.flush();

        return Map.of(
                "idCardsKnown", idCardsKnown,
                "idCardsUnknown", idCardsUnknown
        );
    }
    // END PROCESS REVIEW

    private void handleConvertSRSRatingToSimpleModeProgresses(
            ProgressSrsRequest requests,
            Map<Pair<Long, Long>, UserSRSProgress> srsProgresses,
            Map<Pair<Long, Long>, UserProgress> progresses,
            SetProgressSetting setting
    ) {
        for (ProgressSrsRequest.SrsRequest request : requests.getBatchRequests()) {
            if (srsProgresses.containsKey(Pair.of(
                    request.getUserId(),
                    request.getCardId()
            ))) {
                handleTransformSimpleModePropBaseOnRating(
                        request,
                        progresses,
                        setting
                );
            }
        }
    }

    private void handleTransformSimpleModePropBaseOnRating(
            ProgressSrsRequest.SrsRequest request,
            Map<Pair<Long, Long>, UserProgress> progresses,
            SetProgressSetting setting
    ) {
        ReviewRating r = ReviewRating.valueOf(request.getUserRating());
        UserProgress progress = progresses
                .getOrDefault(Pair.of(request.getUserId(), request.getCardId()),
                        UserProgress.builder()
                                .appUser(AppUser.builder().userId(request.getUserId()).build())
                                .flashcard(Flashcard.builder().cardId(request.getCardId()).build())
                                .progressType(false)
                                .isAttention(false)
                                .modeVersion(setting.getCurrentSimpleModeVersion())
                                .build());

        switch (r) {
            case ReviewRating.Again, ReviewRating.Good, ReviewRating.Easy -> progress.setCountConsecutiveHardPress(0);
        }

        switch (r) {
            case ReviewRating.Again -> progress.setConsecutiveCorrectSimpleMode(
                    Math.max(0, progress.getConsecutiveCorrectSimpleMode() - 1)
            );
            case ReviewRating.Hard -> {
                progress.setCountConsecutiveHardPress(
                        Math.min(progress.getCountConsecutiveHardPress() + 1,
                                MAX_CONSECUTIVE_HARD_BEFORE_UNKNOWN)
                );
                if (progress.getCountConsecutiveHardPress().equals(MAX_CONSECUTIVE_HARD_BEFORE_UNKNOWN)) {
                    progress.setConsecutiveCorrectSimpleMode(
                            Math.max(0, progress.getConsecutiveCorrectSimpleMode() - 1)
                    );
                }
            }
            case ReviewRating.Good -> progress.setConsecutiveCorrectSimpleMode(
                    Math.min(MAX_CONSECUTIVE_BEFORE_KNOWN,
                            progress.getConsecutiveCorrectSimpleMode() + 1)
            );
            case ReviewRating.Easy -> progress.setConsecutiveCorrectSimpleMode(MAX_CONSECUTIVE_BEFORE_KNOWN);
        }

        progress.setProgressType(
                progress.getConsecutiveCorrectSimpleMode().equals(MAX_CONSECUTIVE_BEFORE_KNOWN)
        );

        progress.setModeVersion(setting.getCurrentSimpleModeVersion());
        progresses.put(Pair.of(request.getUserId(), request.getCardId()), progress);
    }
    // ====================================================

    private void handleConvertSimpleModeRatingToSRSProgresses(
            UserProgressRequest.SimpleModeUserProgressRequest request,
            Map<Pair<Long, Long>, UserSRSProgress> srsProgresses,
            Map<Pair<Long, Long>, UserProgress> progresses,
            SetProgressSetting setting
    ) {
        for (UserProgressRequest r : request.progresses()) {
            if (progresses.containsKey(
                    Pair.of(r.getUserId(), r.getCardId())
            )) {
                handleTransformSRSModePropBaseOnRating(
                        r,
                        srsProgresses,
                        setting
                );
            }
        }
    }

    private void handleTransformSRSModePropBaseOnRating(
            UserProgressRequest request,
            Map<Pair<Long, Long>, UserSRSProgress> srsProgresses,
            SetProgressSetting setting
    ) {
        ReviewRating rating;

        if (!request.getProgressType()) {
            rating = ReviewRating.Again;
        } else {
            rating = ReviewRating.Good;
        }

        handleCalculateNewUserProgressWithSrs(
                ProgressSrsRequest.SrsRequest.builder()
                        .userId(request.getUserId())
                        .cardId(request.getCardId())
                        .userRating(rating.name())
                        .build(),
                srsProgresses,
                setting,
                false
        );
    }
    // ====================================================

    // Phương thức này sẽ không quăng bất cứ lỗi logic nào
    // Các lỗi như select dữ liệu từ db hay thiết lập thì có thể xuất hiện
    private void handleCalculateNewUserProgressWithSimpleMode(
            UserProgressRequest request,
            Map<Pair<Long, Long>, UserProgress> progresses,
            SetProgressSetting settings) {
        UserProgress progress =
                progresses.getOrDefault(Pair.of(request.getUserId(), request.getCardId()),
                        UserProgress.builder()
                                .appUser(AppUser.builder().userId(request.getUserId()).build())
                                .flashcard(Flashcard.builder().cardId(request.getCardId()).build())
                                .build()
                );

        progress.setConsecutiveCorrectSimpleMode(
                Math.min(MAX_CONSECUTIVE_BEFORE_KNOWN, progress.getConsecutiveCorrectSimpleMode() +
                        (Boolean.TRUE.equals(progress.getProgressType()) ? 1 : 0))
        );

        progress.setProgressType(
                progress.getConsecutiveCorrectSimpleMode().equals(MAX_CONSECUTIVE_BEFORE_KNOWN)
        );

        progress.setModeVersion(settings.getCurrentSimpleModeVersion());
        progress.setUpdatedAt(Instant.now());

        progresses.put(Pair.of(request.getUserId(), request.getCardId()), progress);
    }

    private boolean checkIsCardNew(UserSRSProgress srsProgress,
                                   SetProgressSetting settings) {
        if (srsProgress == null) {
            return true;
        }
        return !Objects.equals(srsProgress.getSrsVersion(), settings.getCurrentSrsVersion());
    }

    // Phương thức này sẽ không quăng bất cứ lỗi logic nào
    // Các lỗi như select dữ liệu từ db hay thiết lập thì có thể xuất hiện
    private void handleCalculateNewUserProgressWithSrs(
            ProgressSrsRequest.SrsRequest request,
            Map<Pair<Long, Long>, UserSRSProgress> srsProgresses,
            SetProgressSetting settings,
            Boolean isUpdateDueDate) {
        UserSRSProgress progress =
                srsProgresses.get(Pair.of(request.getUserId(), request.getCardId()));

        boolean isNew = checkIsCardNew(progress, settings);

        if (isNew) {
            progress = UserSRSProgress.builder()
                    .appUser(AppUser.builder().userId(request.getUserId()).build())
                    .flashcard(Flashcard.builder().cardId(request.getCardId()).build())
                    .easeFactor(MAX_EASE_FACTOR)
                    .dueDate(Instant.now())
                    .build();
        }

        UserSRSProgress before = cloneProgress(progress);

        ReviewRating userRating = ReviewRating.fromString(request.getUserRating());

        if (userRating == ReviewRating.Unknown ||
                userRating == ReviewRating.Known) {
            return;
        }

        // Tính sẵn 4 interval dự kiến (dựa trên state TRƯỚC review)
        Map<ReviewRating, Long> potential = getOldIntervalsInUser(before, settings);

        // Cập nhật trạng thái (đã bao gồm logic Relearning mới)
        updateProgressBasedOnRating(progress, userRating, settings);

        if (isUpdateDueDate) {
            Long chosenInterval = potential.getOrDefault(userRating, 60L);
            progress.setLastReviewTime(Instant.now());
            progress.setCurrentInterval(chosenInterval);
            progress.setDueDate(progress.getLastReviewTime().plusSeconds(chosenInterval));
        }

        // Bây chừ mới tính toán cái interval mới cho cái card đó
        potential = calculateAllNextIntervals(progress, settings);

        if (isNew) {
            progress.setCardState(CardState.New);
            progress.setCreatedAt(Instant.now());
        }

        // Cập nhật và gắn giá trị old next interval mới cho mỗi button
        progress.setOldNextIntervalAgain(potential.get(ReviewRating.Again));
        progress.setOldNextIntervalHard(potential.get(ReviewRating.Hard));
        progress.setOldNextIntervalGood(potential.get(ReviewRating.Good));
        progress.setOldNextIntervalEasy(potential.get(ReviewRating.Easy));

        progress.setSrsVersion(settings.getCurrentSrsVersion());
        progress.setUpdatedAt(Instant.now());

        srsProgresses.put(Pair.of(request.getUserId(), request.getCardId()), progress);
    }

    // calculateAllNextIntervals
    private Map<ReviewRating, Long> calculateAllNextIntervals(UserSRSProgress state,
                                                              SetProgressSetting set) {
        Map<ReviewRating, Long> intervals = new EnumMap<>(ReviewRating.class);

        long minGap = Math.max(MINIMUM_MIN_GAP_SECONDS, set.getMinIntervalGap());

        // ---- Các state khác: dùng mô phỏng SM‑2 ----
        List<Long> lSteps = SRSUtils.parseSteps(set.getLearningSteps());
        List<Long> rSteps = SRSUtils.parseSteps(set.getRelearningSteps());

        UserSRSProgress sim = cloneProgress(state);

        for (ReviewRating r : List.of(ReviewRating.Again, ReviewRating.Hard,
                ReviewRating.Good, ReviewRating.Easy)) {
            intervals.put(r, calculateSm2Interval(sim, r, set, lSteps, rSteps));
        }
        long prev = 0;
        for (ReviewRating r : List.of(ReviewRating.Again, ReviewRating.Hard,
                ReviewRating.Good, ReviewRating.Easy)) {
            long v = intervals.getOrDefault(r, 60L);
            v = Math.max(prev + minGap, v);
            intervals.put(r, v);
            prev = v;
        }
        return intervals;
    }

    public Map<ReviewRating, Long> getOldIntervalsInUser(UserSRSProgress before, SetProgressSetting settings) {
        if (Objects.isNull(before) || Objects.isNull(settings)) {
            return Collections.emptyMap();
        }

        Map<ReviewRating, Long> result = new EnumMap<>(ReviewRating.class);

        result.put(ReviewRating.Again, Objects.isNull(before.getOldNextIntervalAgain()) ?
                settings.getCustomIntervalAgainSeconds()
                : before.getOldNextIntervalAgain());
        result.put(ReviewRating.Hard, Objects.isNull(before.getOldNextIntervalHard()) ?
                settings.getCustomIntervalHardSeconds()
                : before.getOldNextIntervalHard());
        result.put(ReviewRating.Good, Objects.isNull(before.getOldNextIntervalGood()) ?
                settings.getCustomIntervalGoodSeconds()
                : before.getOldNextIntervalGood());
        result.put(ReviewRating.Easy, Objects.isNull(before.getOldNextIntervalEasy()) ?
                settings.getCustomIntervalEasySeconds()
                : before.getOldNextIntervalEasy());
        return result;
    }

    public Map<ReviewRating, Long> getPotentialIntervalsForDisplay(UserSRSProgress before,
                                                                   SetProgressSetting settings) {
        if (before == null) return Collections.emptyMap();
        return getOldIntervalsInUser(before, settings);
    }

    // ======================================================
    // ===================== UPDATE STATE ===================
    // ======================================================

    private void updateProgressBasedOnRating(UserSRSProgress p,
                                             ReviewRating r,
                                             SetProgressSetting set) {
        List<Long> lSteps = SRSUtils.parseSteps(set.getLearningSteps());
        List<Long> rSteps = SRSUtils.parseSteps(set.getRelearningSteps());

        switch (p.getCardState()) {
            case New, Learning -> updateLearningStateSm2(p, r, set, lSteps);
            case Review -> updateReviewStateSm2(p, r, set);
            case Lapsed -> updateRelearningStateSm2(p, r, set, rSteps);
        }
    }

    // ======================================================
    // ================ SM‑2 LOGIC ===================
    // ======================================================

    /**
     * Tính interval cho mọi state dựa trên SM‑2 (đã fuzz + phạt).
     */
    private Long calculateSm2Interval(UserSRSProgress p,
                                      ReviewRating r,
                                      SetProgressSetting set,
                                      List<Long> lSteps,
                                      List<Long> rSteps) {
        Long res;
        switch (p.getCardState()) {
            case New, Learning -> res = getSm2LearningInterval(p, r, p.getLearningStep(), lSteps, set);
            case Review -> res = getSm2ReviewInterval(p, r, set);
            case Lapsed -> res = getSm2RelearningInterval(r, p.getLearningStep(), rSteps, set, p);
            default -> res = 60L;
        }

        // Nếu như bất cứ nút nào mà có trạng thái lớn hơn 2 ngày (tức là card đó đã khá thuộc rồi,
        // cộng thêm cái bonus interval để gia tăng ngày học lại
        if (p.getCardState() == CardState.Review && res > 2 * MIN_DAY_SECONDS)
            res = applyFuzzFactor(res);

        return Math.max(60, res);
    }

    // ---------- Learning (New/Learning) ----------
    private Long getSm2LearningInterval(
            UserSRSProgress p,
            ReviewRating r,
            int step,
            List<Long> lSteps,
            SetProgressSetting set) {
        Long base = switch (r) {
            case Again -> lSteps.isEmpty() ? set.getCustomIntervalAgainSeconds() : lSteps.getFirst();
            case Hard -> step < lSteps.size()
                    ? lSteps.get(step)
                    : set.getCustomIntervalHardSeconds();
            case Good -> (step + 1) < lSteps.size()
                    ? lSteps.get(step + 1)
                    : set.getCustomIntervalGoodSeconds();
            case Easy -> set.getCustomIntervalEasySeconds();
            default -> 60L;
        };
        float penalty = getIntervalPenaltyLearning(set.getConsecutiveIncorrectPenaltyFactorLearning(),
                set.getConsecutiveIncorrectPenaltyFactorLearning() - 0.1F,
                p.getConsecutiveIncorrect());
        ;
        return Math.max(60L, (long) (base * penalty));
    }

    // ---------- Review ----------
    private Long getSm2ReviewInterval(UserSRSProgress p,
                                      ReviewRating r,
                                      SetProgressSetting set) {
        float e = p.getEaseFactor();
        float m = set.getSm2IntervalModifier();
        float hf = set.getSm2HardIntervalFactor();

        long li = Math.max(1L, p.getCurrentInterval());
        int cc = p.getConsecutiveCorrect();
        float bf = (cc > 1) ? set.getConsecutiveCorrectBonusFactor() : 1f;

        return switch (r) {
            case Again -> {
                List<Long> rs = SRSUtils.parseSteps(set.getRelearningSteps());
                yield rs.isEmpty() ? p.getOldNextIntervalAgain().longValue() : rs.getFirst();
            }
            case Hard -> (long) (li * hf * m * bf);
            case Good -> (long) (li * e * m * bf);
            case Easy -> (long) (li * e * EASE_BONUS_EASY * m * bf);
            default -> li;
        };
    }

    // ---------- Relearning ----------
    private Long getSm2RelearningInterval(ReviewRating r,
                                          int step,
                                          List<Long> rSteps,
                                          SetProgressSetting set,
                                          UserSRSProgress p) {

        /*
            KHÔNG THỂ CÓ HỆ SỐ BAN ĐẦU CỦA CÁI P.getOldNextInterval... của cả 4 button là NULL
            KHÔNG THỂ NÀO VỪA MỚI BẮT ĐẦU TẠO CARD LÀ THẺ ĐÓ TỐT NGHIỆP LUÔN
        * */
//        NO CODE: int base = switch (r) {
//            /*
//             * Tại đoạn code này, thay thế các cái hard, good, easy bằng các cái hệ số
//             * đã lưu từ trước đó (các cái oldNextInterval của mỗi button được lưu vào
//             * trong database) để tính thay vì sử dụng cái bước relearning như người dùng
//             * cài đặt. Còn không thì ưu tiên cái relearning của người học trước, cái nào
//             *  null thì mới lấy cái oldNextInterval trong db
//             *
//             * Không lấy các cái hệ số từ trong cái setting.
//             * */
////            case Again -> rSteps.isEmpty() ? set.getCustomIntervalAgain() : rSteps.getFirst();
////            case Hard, Good -> (step + 1) < rSteps.size()
////                    ? rSteps.get(step + 1)
////                    : set.getCustomIntervalGood();
////            case Easy -> set.getCustomIntervalEasy();
////            default -> 60;
//            case Again -> rSteps.isEmpty() ? (int) p.getOldNextIntervalAgain().longValue()
//                    : rSteps.getFirst();
//            case Hard, Good -> (step + 1) < rSteps.size()
//                    ? rSteps.get(step + 1)
//                    : r.equals(ReviewRating.Hard) ? (int) p.getOldNextIntervalHard().longValue()
//                    : (int) p.getOldNextIntervalGood().longValue();
//            case Easy -> (int) p.getOldNextIntervalEasy().longValue();
//            default -> 60;
//        };
        long base = switch (r) {
            /*
             * Tạm thời ưu tiên lấy cái old next interval đã được lưu trước đó
             * */
            case Again -> rSteps.isEmpty() ? p.getOldNextIntervalAgain()
                    : rSteps.getFirst();
            case Hard, Good -> r.equals(ReviewRating.Hard) ? p.getOldNextIntervalHard() : p.getOldNextIntervalGood();
            case Easy -> p.getOldNextIntervalEasy();
            default -> 60;
        };

        // Áp dụng phạt theo số lần sai liên tiếp trong trạng thái Lapsed
        float penalty = getIntervalPenaltyRelearning(set.getConsecutiveIncorrectPenaltyFactorRelearning(),
                set.getConsecutiveIncorrectPenaltyFactorRelearning() - 0.1F,
                p.getConsecutiveIncorrect());
        return Math.max(60L, (long) (base * penalty));
    }

    // ---------- Phạt interval trong chế độ relearning ----------
    private float getIntervalPenaltyRelearning(float intervalPenaltyReLearning,
                                               float minIntervalPenaltyReLearning,
                                               int consecutiveIncorrect) {
        return Math.max(minIntervalPenaltyReLearning, 1f - intervalPenaltyReLearning * consecutiveIncorrect);
    }

    // ---------- Phạt interval trong chế độ Learning ----------
    private float getIntervalPenaltyLearning(float intervalPenaltyLearning,
                                             float minIntervalPenaltyLearning,
                                             int consecutiveIncorrect) {
        return Math.max(minIntervalPenaltyLearning, 1f - intervalPenaltyLearning * consecutiveIncorrect);
    }

    // ---------- Cập nhật Learning ----------
    private void updateLearningStateSm2(UserSRSProgress p,
                                        ReviewRating r,
                                        SetProgressSetting set,
                                        List<Long> lSteps) {
        switch (r) {
            case Again -> {
                p.setLearningStep(0);
                p.setConsecutiveCorrect(0);
                p.setCardState(CardState.Learning);
                p.setConsecutiveIncorrect(
                        Objects.isNull(p.getConsecutiveIncorrect())
                                ?
                                1 :
                                p.getConsecutiveIncorrect() + 1
                );
            }
            case Hard -> {
                p.setConsecutiveCorrect(p.getConsecutiveCorrect() + 1);
                p.setCardState(CardState.Learning);
                p.setConsecutiveIncorrect(0);
            }
            case Good -> {
                if (p.getLearningStep() + 1 < lSteps.size()) {
                    p.setLearningStep(p.getLearningStep() + 1);
                    p.setCardState(CardState.Learning);
                } else {
                    p.setCardState(CardState.Review);
                    p.setLearningStep(0);
                }
                p.setConsecutiveCorrect(p.getConsecutiveCorrect() + 1);
                p.setConsecutiveIncorrect(0);
            }
            case Easy -> {
                p.setCardState(CardState.Review);
                p.setLearningStep(0);
                p.setConsecutiveCorrect(p.getConsecutiveCorrect() + 1);
                p.setConsecutiveIncorrect(0);
            }
        }
    }

    // ---------- Cập nhật Review ----------
    private void updateReviewStateSm2(UserSRSProgress p,
                                      ReviewRating r,
                                      SetProgressSetting set) {

        float ce = p.getEaseFactor();
        switch (r) {
            case Again -> {
//                p.setEaseFactor(Math.max(MIN_EASE_FACTOR, ce - EASE_PENALTY_AGAIN));
                p.setEaseFactor(clampEase(ce - EASE_PENALTY_AGAIN));
                p.setLapses(p.getLapses() + 1);
                p.setCardState(CardState.Lapsed);
                p.setLearningStep(0);
                p.setConsecutiveCorrect(0);
                p.setConsecutiveIncorrect(p.getConsecutiveIncorrect() + 1);
            }
            case Hard -> {
//                p.setEaseFactor(Math.max(MIN_EASE_FACTOR, ce - EASE_PENALTY_HARD));
                p.setEaseFactor(clampEase(ce - EASE_PENALTY_HARD));
                p.setConsecutiveIncorrect(0);
                p.setConsecutiveCorrect(p.getConsecutiveCorrect() + 1);
            }
            case Good -> {
                p.setConsecutiveIncorrect(0);
                p.setConsecutiveCorrect(p.getConsecutiveCorrect() + 1);
            }
            case Easy -> {
//                p.setEaseFactor(ce + EASE_BONUS_EASY);
                p.setEaseFactor(clampEase(ce + EASE_BONUS_EASY));
                p.setConsecutiveIncorrect(0);
                p.setConsecutiveCorrect(p.getConsecutiveCorrect() + 1);
            }
        }
    }

    // ---------- Cập nhật Relearning (Lapsed) ----------
    private void updateRelearningStateSm2(UserSRSProgress p,
                                          ReviewRating r,
                                          SetProgressSetting set,
                                          List<Long> rSteps) {

        float ce = p.getEaseFactor() != null ? p.getEaseFactor() : MAX_EASE_FACTOR;

        switch (r) {
            // ------- AGAIN -------
            case Again -> {
                p.setConsecutiveIncorrect(p.getConsecutiveIncorrect() + 1);
                p.setConsecutiveCorrect(0);
//                p.setEaseFactor(Math.max(MIN_EASE_FACTOR, ce - EASE_PENALTY_AGAIN));
                p.setEaseFactor(clampEase(ce - EASE_PENALTY_AGAIN));

                if (p.getConsecutiveIncorrect() >= LAPSED_TO_LEARNING_THRESHOLD) {
                    // Quay lại Learning sau quá ngưỡng
                    p.setCardState(CardState.Learning);
                    p.setLearningStep(0);
//                    p.setConsecutiveIncorrect(1);
                } else {
                    p.setLearningStep(0);
                }
            }

            // ------- HARD & GOOD -------
            case Hard, Good -> {
                p.setConsecutiveIncorrect(0);
                p.setConsecutiveCorrect(p.getConsecutiveCorrect() + 1);

                if (p.getLearningStep() + 1 < rSteps.size() + (r.equals(ReviewRating.Hard) ? 1 : 0)) {
                    p.setLearningStep(p.getLearningStep() + 1);   // vẫn Lapsed
                } else {
                    p.setCardState(CardState.Review);
                    p.setLearningStep(0);
                }
            }

            // ------- EASY -------
            case Easy -> {
                p.setConsecutiveIncorrect(0);
                p.setConsecutiveCorrect(p.getConsecutiveCorrect() + 1);
                p.setCardState(CardState.Review);
                p.setLearningStep(0);
            }
        }
    }


    // END UPDATE STATE

    // ======================================================
    // ==================== UTILITIES =======================
    // ======================================================

    /**
     * Áp dụng fuzz ngẫu nhiên range (%) cho interval > 2 ngày.
     */
    private long applyFuzzFactor(long seconds) {
//        if (seconds <= 0 || FUZZ_FACTOR_PERCENT <= 0) return seconds;
        if (seconds <= 0) return seconds; // Should check FUZZ_FACTOR_PERCENT <= 0 to avoid value in code can be changed
        long range = (long) Math.max(1, seconds * (FUZZ_FACTOR_PERCENT / 100.0) / 2.0);
        long fuzz = ThreadLocalRandom.current().nextLong(-range, range + 1);
        return Math.max(60L, seconds + fuzz);
    }

    /**
     * Clone progress (đủ dùng cho tính toán, không clone nextInterval cache).
     */
    private UserSRSProgress cloneProgress(UserSRSProgress o) {
        UserSRSProgress c = new UserSRSProgress();
        c.setId(o.getId());
        c.setAppUser(o.getAppUser());
        c.setFlashcard(o.getFlashcard());
        c.setCardState(o.getCardState());
        c.setCurrentInterval(o.getCurrentInterval());
        c.setEaseFactor(o.getEaseFactor());
        c.setLapses(o.getLapses());
        c.setLearningStep(o.getLearningStep());
        c.setLastReviewTime(o.getLastReviewTime());
        c.setDueDate(o.getDueDate());
        c.setConsecutiveCorrect(o.getConsecutiveCorrect());
        c.setConsecutiveIncorrect(o.getConsecutiveIncorrect());
        c.setOldNextIntervalAgain(o.getOldNextIntervalAgain());
        c.setOldNextIntervalHard(o.getOldNextIntervalHard());
        c.setOldNextIntervalGood(o.getOldNextIntervalGood());
        c.setOldNextIntervalEasy(o.getOldNextIntervalEasy());
        c.setSrsVersion(o.getSrsVersion());
        c.setCreatedAt(o.getCreatedAt());
        c.setUpdatedAt(o.getUpdatedAt());
        return c;
    }
}
