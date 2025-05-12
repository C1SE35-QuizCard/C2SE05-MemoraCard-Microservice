package com.example.quizcards.service.impl;

import com.example.quizcards.dto.SortListDTO;
import com.example.quizcards.dto.request.FlashcardSettingRequest;
import com.example.quizcards.dto.response.FlashcardSettingResponse;
import com.example.quizcards.entities.AppUser;
import com.example.quizcards.entities.SetFlashcard;
import com.example.quizcards.entities.UserFlashcardSetting;
import com.example.quizcards.exception.ResourceNotFoundException;
import com.example.quizcards.mapper.UserFlashcardSettingMapper;
import com.example.quizcards.repository.IAppUserRepository;
import com.example.quizcards.repository.IFlashcardSettingRepository;
import com.example.quizcards.repository.ISetFlashcardRepository;
import com.example.quizcards.security.UserPrincipal;
import com.example.quizcards.service.IFlashcardSettingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;


@Service
public class FlashcardSettingServiceImpl implements IFlashcardSettingService {
    @Autowired
    private IFlashcardSettingRepository flashcardSettingRepository;
    @Autowired
    private ISetFlashcardRepository setFlashcardRepository;

    @Autowired
    private UserFlashcardSettingMapper userFlashcardSettingMapper;

    @Autowired
    private IAppUserRepository appUserRepository;
    @Override
    public List<SortListDTO> getSortedFlashcards(String sortBy) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserPrincipal up = (UserPrincipal) authentication.getPrincipal();
        if ("recent".equals(sortBy)) {
            return flashcardSettingRepository.findAllRecentByUserId(up.getId());
        } else if ("created".equals(sortBy)) {
            return flashcardSettingRepository.findSortedByCreated(up.getId());
        } else if ("learned".equals(sortBy)) {
            return flashcardSettingRepository.findSortedByLearned(up.getId());
        }
        return new ArrayList<>();
    }
    @Override
    public FlashcardSettingResponse getaFlashcardSettingByUserAndSetId(Long userId, Long setId) {
        if (!appUserRepository.existsById(userId)) throw new IllegalArgumentException("User not found with ID: " + userId);
        if (!setFlashcardRepository.existsById(setId)) throw  new IllegalArgumentException("Set not found with ID: " + setId);
        UserFlashcardSetting setting = flashcardSettingRepository.findByUser_UserIdAndSetFlashcard_SetId(userId, setId)
                .orElseThrow(() -> new ResourceNotFoundException("Flashcard setting", "set id", setId.toString()));
        return userFlashcardSettingMapper.toFlashcardSettingResponse(setting);
    }

    @Override
    @Transactional
    public FlashcardSettingResponse updateOrCreateNewFlashcardSetting(
            Long userId, FlashcardSettingRequest request) {
        Optional<UserFlashcardSetting> setting = flashcardSettingRepository.findByUser_UserIdAndSetFlashcard_SetId(userId,request.getSetId());
        System.out.println(setting);
        return setting.map(userFlashcardSetting -> updateSetting(userFlashcardSetting, request))
                .orElseGet(() -> createSetting(userId, request));
    }

    @Override
    public ResponseEntity<?> save(Long setId) {
        if(!existsUserFlashcardSetting(setId)){
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserPrincipal up = (UserPrincipal) authentication.getPrincipal();
            flashcardSettingRepository.save(up.getId(),setId);
            return ResponseEntity.status(HttpStatus.CREATED).body("Save successfully!");
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("You have already saved !");
    }

    @Override
    public Boolean existsUserFlashcardSetting(Long setId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserPrincipal up = (UserPrincipal) authentication.getPrincipal();
        Long exist= flashcardSettingRepository.existsUserFlashcardSetting(setId,up.getId());
        if(exist==null){
            return false;
        }
        return true;
    }


    private FlashcardSettingResponse updateSetting(UserFlashcardSetting setting, FlashcardSettingRequest request) {
        userFlashcardSettingMapper.updateUserFromDto(request, setting);
        UserFlashcardSetting new_setting = flashcardSettingRepository.save(setting);
        var response = userFlashcardSettingMapper.toFlashcardSettingResponse(new_setting);
        response.setUserId(new_setting.getUser().getUserId());
        return response;
    }

    private FlashcardSettingResponse createSetting(Long userId, FlashcardSettingRequest request) {
        AppUser au = AppUser.builder().userId(userId).build();
        SetFlashcard s = SetFlashcard.builder().setId(request.getSetId()).build();
        UserFlashcardSetting new_setting = flashcardSettingRepository.save(UserFlashcardSetting.builder()
                .user(au)
                .setFlashcard(s)
                .lastCardId(Objects.isNull(request.getLastCardId()) ? -1 : request.getLastCardId())
                .shuffleMode(!Objects.isNull(request.getShuffleMode()) && request.getShuffleMode())
                .flipCardMode(!Objects.isNull(request.getFlipCardMode()) && request.getFlipCardMode())
                .build());

        FlashcardSettingResponse response = userFlashcardSettingMapper.toFlashcardSettingResponse(new_setting);
        response.setUserId(userId);

        return response;
    }
}
