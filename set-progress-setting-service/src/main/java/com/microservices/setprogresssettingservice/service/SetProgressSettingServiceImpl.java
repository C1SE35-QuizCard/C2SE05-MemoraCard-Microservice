package com.microservices.setprogresssettingservice.service;

import com.microservices.setprogresssettingservice.dto.request.SetProgressSettingRequest;
import com.microservices.setprogresssettingservice.model.SetProgressSetting;
import com.microservices.setprogresssettingservice.repository.ISetProgressSettingRepository;
import com.microservices.setprogresssettingservice.repository.ISetRepository;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import static lombok.AccessLevel.PRIVATE;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = PRIVATE, makeFinal = true)
public class SetProgressSettingServiceImpl {

    ISetProgressSettingRepository settingRepo;
    ISetRepository setRepo;

    public Mono<SetProgressSetting> getSettings(Long setId, Long userId) {
        return settingRepo
                .findBySetFlashcardIdAndAppUserId(setId, userId)
                .switchIfEmpty(Mono.error(
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "SetProgressSetting not found for setId=" + setId + ", userId=" + userId
                        )
                ));
    }

    public Mono<SetProgressSetting> getEffectiveSettings(Long setId, Long userId) {
        return settingRepo
                .findBySetFlashcardIdAndAppUserId(setId, userId)
                .switchIfEmpty(
                        setRepo.existsById(setId)
                                .flatMap(exists -> {
                                    if (!exists) {
                                        return Mono.error(new ResponseStatusException(
                                                HttpStatus.NOT_FOUND,
                                                "SetFlashcard not found for id=" + setId
                                        ));
                                    }
                                    return Mono.just(
                                            SetProgressSetting.builder()
                                                    .setFlashcardId(setId)
                                                    .appUserId(userId)
                                                    .build()
                                    );
                                })
                );
    }

    @Transactional
    public Mono<SetProgressSetting> saveOrUpdateSettings(SetProgressSettingRequest request, Long userId) {
        request.setUserId(userId);
        SetProgressSetting converted = SetProgressSettingRequest.toEntity(request);

        return setRepo.existsById(request.getSetId())
                .flatMap(exists -> {
                    if (!exists) {
                        return Mono.error(new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "SetFlashcard not found for id=" + request.getSetId()
                        ));
                    }
                    return settingRepo
                            .findBySetFlashcardIdAndAppUserId(request.getSetId(), userId)
                            .switchIfEmpty(Mono.just(converted))
                            .flatMap(setting -> {
                                if (setting.getId() != null) {
                                    SetProgressSettingRequest.update(setting, request);
                                }
                                return settingRepo.save(setting);
                            });
                });
    }

    @Transactional
    public Mono<SetProgressSetting> softResetSrsSetting(Long userId, Long setId) {
        return getSettings(setId, userId)
                .flatMap(setting -> {
                    setting.setCurrentSrsVersion(setting.getCurrentSrsVersion() + 1);
                    return settingRepo.save(setting);
                });
    }

    public Mono<Long> findCurrentSimpleModeVersionByCardIdAndUserId(Long cardId, Long userId) {
        return settingRepo.findCurrentSimpleModeVersionByCardIdAndUserId(cardId, userId);
    }
}