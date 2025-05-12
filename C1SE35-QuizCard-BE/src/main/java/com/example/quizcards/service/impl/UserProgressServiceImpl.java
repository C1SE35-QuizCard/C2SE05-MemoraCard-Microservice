package com.example.quizcards.service.impl;

import com.example.quizcards.dto.IFlashcardProgressDTO;
import com.example.quizcards.dto.IProgressDTO;
import com.example.quizcards.dto.request.UserProgressRequest;
import com.example.quizcards.dto.IProgressAnalysisDTO;
import com.example.quizcards.entities.AppUser;
import com.example.quizcards.entities.Flashcard;
import com.example.quizcards.entities.UserProgress;
import com.example.quizcards.entities.SetProgressSetting;
import com.example.quizcards.repository.IUserProgressRepository;
import com.example.quizcards.security.UserPrincipal;
import com.example.quizcards.service.IUserProgressService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserProgressServiceImpl implements IUserProgressService {
    IUserProgressRepository userProgressRepository;

    SetProgressSettingServiceImpl setProgressService;

//    @Override
//    public List<IUserProgressDTO> findUserSetProgress(Long userId) {
//        return userProgressRepository.findUserSetProgress(userId);
//    }

    @Deprecated
    @Override
    public List<IFlashcardProgressDTO> findFlashcardsProgressBySetId(Long setId, Long userId) {
        return userProgressRepository.findFlashcardsProgressBySetId(setId, userId);
    }

    @Deprecated
    @Override
    public List<IFlashcardProgressDTO> findCardProgressesByCardIdsIn(Long userId, List<Long> cardIds, long limit) {
        return userProgressRepository.findProgCardsByCardIdsIn(
                userId, cardIds, limit);
    }

    @Override
    public List<IFlashcardProgressDTO> findFlashcardsProgressBySetIdWithVersion(Long setId, Long userId) {
        SetProgressSetting setting = setProgressService.getEffectiveSettings(setId, userId);
        return userProgressRepository.findFlashcardsProgressBySetId(
                setId, userId, setting.getCurrentSimpleModeVersion());
    }

    @Override
    public IProgressAnalysisDTO findAnalysisProgressBySetId(Long setId, Long userId) {
        return userProgressRepository.findAnalysisProgressBySetId(setId, userId);
    }

    @Override
    public void addUserProgress(Boolean progressType, Boolean isAttention, Long userId, Long cardId) {
        userProgressRepository.createUserProgress(progressType, isAttention, userId, cardId);
    }

    @Override
    public void deleteUserProgressById(Long progressId) {
        userProgressRepository.deleteUserProgressById(progressId);
    }

    @Override
    public void updateUserProgress(UserProgressRequest request) {
        userProgressRepository.updateUserProgress(request.getProgressId(), request.getProgressType(), request.getIsAttention(), request.getUserId(), request.getCardId());
    }

    private UserProgress findOrCreate(Long userId, Long cardId) {
        return userProgressRepository
                .findByAppUser_UserIdAndFlashcard_CardId(userId, cardId)
                .orElseGet(() -> {
                    UserProgress newUps = new UserProgress();
                    newUps.setAppUser(AppUser.builder().userId(userId).build());
                    newUps.setFlashcard(Flashcard.builder().cardId(cardId).build());
                    newUps.setIsAttention(false);
                    return newUps;
                });
    }

    private Map<String, Object> handleAssign(Long userId, Long cardId, UserProgressRequest request) {
        return handleAssign(userId, cardId, request, false);
    }

    private Map<String, Object> handleAssign(Long userId, Long cardId, UserProgressRequest request, boolean isAssignVersion) {
        // 1. Tìm hoặc khởi tạo mới UserProgress
        UserProgress ups = findOrCreate(userId, cardId);

        if (request.getProgressType() != null) {
            ups.setProgressType(request.getProgressType());
        }

        if (request.getIsAttention() != null) {
            ups.setIsAttention(request.getIsAttention());
        }

        if (isAssignVersion) {
            Long currentSimpleModeVersion = setProgressService.findCurrentSimpleModeVersionByCardIdAndUserId(cardId, userId);
            ups.setModeVersion(currentSimpleModeVersion);
        }

        // 4. Lưu vào DB
        ups = userProgressRepository.save(ups);

        // 5. Build kết quả
        Map<String, Object> result = new HashMap<>();
        result.put("progressId", ups.getProgressId());
        result.put("statusProgress", ups.getProgressType());
        result.put("statusMark", ups.getIsAttention());
        result.put("userId", userId);
        result.put("cardId", cardId);
        return result;
    }

    @Deprecated
    @Override
    public Map<String, Object> assignUserProgress(UserProgressRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserPrincipal up = (UserPrincipal) authentication.getPrincipal();
        return handleAssign(up.getId(), request.getCardId(), request);
    }

    @Deprecated
    @Override
    public void resetUserProgress(Long setId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserPrincipal up = (UserPrincipal) authentication.getPrincipal();
        userProgressRepository.deleteUserProgressByUserAndSetId(up.getId(), setId);
    }

    @Deprecated
    @Override
    public boolean existsByUserIdAndCardId(Long userId, Long cardId) {
        return userProgressRepository.existsByUserIdAndCardId(userId, cardId) != 0;
    }

    @Deprecated
    @Override
    public boolean existsByUserIdAndCardIdAndNotId(Long userId, Long cardId, Long progressId) {
        return userProgressRepository.existsByUserIdAndCardIdAndNotId(userId, cardId, progressId) > 0;
    }

    @Override
    public IProgressDTO findUserProgressById(Long progressId) {
        return userProgressRepository.findUserProgressById(progressId);
    }

    @Override
    public Map<String, Object> assignUserProgressWithVersion(UserProgressRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserPrincipal up = (UserPrincipal) authentication.getPrincipal();
        return handleAssign(up.getId(), request.getCardId(), request, true);
    }
}
