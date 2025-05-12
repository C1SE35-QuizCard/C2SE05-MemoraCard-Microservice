package com.example.quizcards.service.impl;

import com.example.quizcards.dto.request.SetProgressSettingRequest;
import com.example.quizcards.entities.AppUser;
import com.example.quizcards.entities.SetFlashcard;
import com.example.quizcards.entities.SetProgressSetting;
import com.example.quizcards.exception.ResourceNotFoundException;
import com.example.quizcards.repository.ISetFlashcardRepository;
import com.example.quizcards.repository.ISetProgressSettingRepository;
import com.example.quizcards.security.UserPrincipal;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SetProgressSettingServiceImpl {
    ISetProgressSettingRepository settingRepo;

    ISetFlashcardRepository setFlashcardRepo;

    public SetProgressSetting getSettings(Long setId, Long userId) {
        // Assuming UserSetProgressSettingRepository is a repository for SetProgressSetting
        return settingRepo.findBySetFlashcard_SetIdAndAppUser_UserId(
                setId, userId).orElseThrow(() -> new ResourceNotFoundException("SetProgressSetting", "setId", setId));
    }

    public SetProgressSetting getEffectiveSettings(Long setId, Long userId) {
        Optional<SetProgressSetting> setting = settingRepo.findBySetFlashcard_SetIdAndAppUser_UserId(setId, userId);
        if (setting.isEmpty()) {
            if (!setFlashcardRepo.existsById(setId)) {
                throw new ResourceNotFoundException("Set", "id", setId);
            }
            return SetProgressSetting.builder()
                    .setFlashcard(SetFlashcard.builder().setId(setId).build())
                    .appUser(AppUser.builder().userId(userId).build())
                    .build();
        }
        return setting.get();
    }

    @Transactional
    public void saveOrUpdateSettings(SetProgressSettingRequest requestSetting) {
        if (!setFlashcardRepo.existsById(requestSetting.getSetId())) {
            throw new ResourceNotFoundException("Set", "id", requestSetting.getSetId());
        }
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserPrincipal up = (UserPrincipal) auth.getPrincipal();
        requestSetting.setUserId(up.getId());
        SetProgressSetting convertedSetting = SetProgressSettingRequest.toEntity(requestSetting);
        SetProgressSetting setting = settingRepo.findBySetFlashcard_SetIdAndAppUser_UserId(
                requestSetting.getSetId(), up.getId()).orElse(convertedSetting);
        if (setting.getId() != null) {
            SetProgressSettingRequest.update(setting, requestSetting);
        }
        settingRepo.save(setting);
    }

    @Transactional
    public void softResetSrsSetting(Long userId, Long setId) {
        SetProgressSetting settingAnalysis = getSettings(setId, userId);
        settingAnalysis.setCurrentSrsVersion(
                settingAnalysis.getCurrentSrsVersion() + 1
        );
        settingRepo.save(settingAnalysis);
    }

    public Long findCurrentSimpleModeVersionByCardIdAndUserId(Long cardId, Long userId) {
        return settingRepo.findCurrentSimpleModeVersionByCardIdAndUserId(cardId, userId);
    }

//    NO CODE: public Integer findCurrentSRSVersionByCardIdAndUserId(Long cardId, Long userId) {
//        return settingRepo.findCurrentSRSVersionByCardIdAndUserId(cardId, userId);
//    }
}
