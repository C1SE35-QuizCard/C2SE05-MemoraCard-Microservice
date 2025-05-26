package com.microservices.progressservice.service;

import com.microservices.dto.security.UserPrincipal;
import com.microservices.progressservice.client.SettingProgressReactiveClient;
import com.microservices.progressservice.dto.request.UserProgressRequest;
import com.microservices.progressservice.dto.response.IFlashcardProgressDTO;
import com.microservices.progressservice.dto.response.IProgressAnalysisDTO;
import com.microservices.progressservice.dto.response.IProgressDTO;
import com.microservices.progressservice.dto.response.SetProgressSettingResponse;
import com.microservices.progressservice.model.UserProgress;
import com.microservices.progressservice.repository.UserProgressR2dbcRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserProgressServiceImpl {
    SettingProgressReactiveClient sprClient;
    UserProgressR2dbcRepository repo;

    /**
     * Lấy flashcards progress với version từ setting service
     */
    public Flux<IFlashcardProgressDTO> findFlashcardsProgressBySetId(Long setId, Long userId) {
        return repo.findFlashcardsProgressBySetId(setId, userId);
    }

    public Flux<IFlashcardProgressDTO> findFlashcardsProgressBySetIdWithVersion(
            Long setId,
            Long userId,
            Map<String, String> headers
    ) {
        return sprClient.getEffectiveSetting(setId, headers)
                .flatMapMany(setting -> {
                    SetProgressSettingResponse settingResponse = setting.getBody();
                    assert settingResponse != null;
                    return repo.findFlashcardsProgressBySetId(
                            setId,
                            userId,
                            settingResponse.getCurrentSimpleModeVersion()
                    );
                });
    }

    @Deprecated
    public Flux<IFlashcardProgressDTO> findCardProgressesByCardIdsIn(Long userId,
                                                                     Long setId,
                                                                     List<Long> cardIds,
                                                                     long limit) {
        return repo.findProgCardsByCardIdsIn(userId, setId, cardIds, limit);
    }

    public Mono<IProgressAnalysisDTO> findAnalysisProgressBySetId(Long setId, Long userId) {
        return repo.findAnalysisProgressBySetId(setId, userId);
    }

    public Mono<Void> addUserProgress(Boolean progressType,
                                      Boolean isAttention,
                                      Long userId,
                                      Long cardId) {
        return repo.createUserProgress(progressType, isAttention, userId, cardId);
    }

    public Mono<Void> updateUserProgress(UserProgressRequest request) {
        return repo.updateUserProgress(
                request.getProgressId(),
                request.getProgressType(),
                request.getIsAttention(),
                request.getUserId(),
                request.getCardId()
        );
    }

    /**
     * Lấy progress analysis với version từ setting service
     */
    public Mono<IProgressAnalysisDTO> findAnalysisProgressBySetIdWithVersion(
            Long setId,
            Long userId,
            Map<String, String> headers
    ) {
        return sprClient.getEffectiveSetting(setId, headers)
                .flatMap(setting -> {
                    SetProgressSettingResponse settingResponse = setting.getBody();
                    assert settingResponse != null;
                    return repo.findAnalysisProgressBySetId(
                            setId,
                            userId,
                            settingResponse.getCurrentSimpleModeVersion()
                    );
                });
    }

    /**
     * Tạo / cập nhật progress (không gán version)
     */
    public Mono<Map<String, Object>> assignUserProgress(
            UserPrincipal up,
            UserProgressRequest req
    ) {
        Long userId = up.getId();
        return handleAssign(
                userId,
                req,
                Map.of(), // Không cần headers trong trường hợp này
                false // Không gán version
        );
    }

    /**
     * Tạo / cập nhật progress (gán version mới từ effective setting)
     */
    public Mono<Map<String, Object>> assignUserProgressWithVersion(
            UserPrincipal up,
            UserProgressRequest req,
            Map<String, String> headers
    ) {
        Long userId = up.getId();
        return handleAssign(
                userId,
                req,
                headers,
                true // Gán version mới từ setting service
        );
    }

    /**
     * Common upsert logic: find existing theo (userId, cardId, modeVersion), hoặc tạo mới,
     * rồi save và build Map kết quả.
     */
    private Mono<Map<String, Object>> handleAssign(
            Long userId,
            UserProgressRequest req,
            Map<String, String> headers,
            Boolean isAssignVersion
    ) {
        return findOrCreate(userId, req.getCardId())
                .flatMap(ups -> {
                    if (req.getProgressType() != null) ups.setProgressType(req.getProgressType());
                    if (req.getIsAttention() != null) ups.setIsAttention(req.getIsAttention());

                    // 2. Chọn version
                    Mono<Long> versionMono = Mono.just(ups.getModeVersion());
                    if (isAssignVersion) {
                        versionMono = sprClient.getCurrentSimpleModeVersion(req.getCardId(), headers)
                                .map(resp -> {
                                    Map<String, Long> body = resp.getBody();
                                    return (body != null && body.containsKey("currentSimpleModeVersion"))
                                            ? body.get("currentSimpleModeVersion")
                                            : ups.getModeVersion();
                                })
                                .onErrorResume(e -> Mono.just(ups.getModeVersion()));
                    }

                    // 3. Gán version và save
                    return versionMono.flatMap(version -> {
                        ups.setModeVersion(version);
                        ups.setUpdatedAt(Instant.now());
                        return repo.save(ups);
                    });
                })
                .map(saved -> Map.of(
                        "progressId", saved.getProgressId(),
                        "statusProgress", saved.getProgressType(),
                        "statusMark", saved.getIsAttention(),
                        "userId", saved.getUserId(),
                        "cardId", saved.getCardId()
                ));
    }

    private Mono<UserProgress> findOrCreate(Long userId, Long cardId) {
        return repo
                .findByUserIdAndCardId(userId, cardId)
                .defaultIfEmpty(UserProgress.builder()
                        .userId(userId)
                        .cardId(cardId)
                        .isAttention(false)
                        .build());
    }

    /**
     * Các thao tác còn lại: tìm theo ID, tồn tại, xóa.
     */
    public Mono<IProgressDTO> findUserProgressById(Long progressId) {
        return repo.findUserProgressById(progressId);
    }

    public Mono<Void> resetUserProgress(Long setId) {
        return ReactiveSecurityContextHolder.getContext()
                .flatMap(ctx -> {
                    Authentication auth = ctx.getAuthentication();
                    if (auth == null || !auth.isAuthenticated()) {
                        return Mono.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED));
                    }
                    Long userId = ((UserPrincipal) auth.getPrincipal()).getId();
                    return repo.deleteUserProgressByUserAndSetId(userId, setId);
                });
    }

    public Mono<Boolean> existsByUserIdAndCardId(Long userId, Long cardId) {
        return repo.existsByUserIdAndCardId(userId, cardId);
    }

    public Mono<Boolean> existsByUserIdAndCardIdAndNotId(Long userId, Long cardId, Long progressId) {
        return repo.existsByUserIdAndCardIdAndProgressIdNot(userId, cardId, progressId);
    }

    public Mono<Void> deleteUserProgressById(Long progressId) {
        return repo.deleteById(progressId);
    }
}
