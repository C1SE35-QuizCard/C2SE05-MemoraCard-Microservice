package com.example.quizcards.service;

import com.example.quizcards.dto.IFlashcardProgressDTO;
import com.example.quizcards.dto.IProgressDTO;
import com.example.quizcards.dto.request.UserProgressRequest;
import com.example.quizcards.dto.IProgressAnalysisDTO;

import java.util.List;
import java.util.Map;

public interface IUserProgressService {
//    List<IUserProgressDTO> findUserSetProgress(Long userId);

    List<IFlashcardProgressDTO> findFlashcardsProgressBySetId(Long setId, Long userId);

    List<IFlashcardProgressDTO> findCardProgressesByCardIdsIn(Long userId, List<Long> cardIds, long limit);

    List<IFlashcardProgressDTO> findFlashcardsProgressBySetIdWithVersion(Long setId, Long userId);

    IProgressAnalysisDTO findAnalysisProgressBySetId(Long setId, Long userId);

    void addUserProgress(Boolean progressType, Boolean isAttention, Long userId, Long cardId);

    void deleteUserProgressById(Long progressId);

    void updateUserProgress(UserProgressRequest request);

    Map<String, Object> assignUserProgress(UserProgressRequest request);

    Map<String, Object> assignUserProgressWithVersion(UserProgressRequest request);

    void resetUserProgress(Long setId);

    IProgressDTO findUserProgressById(Long progressId);

    boolean existsByUserIdAndCardId(Long userId, Long cardId);

    boolean existsByUserIdAndCardIdAndNotId(Long userId, Long cardId, Long progressId);
}
