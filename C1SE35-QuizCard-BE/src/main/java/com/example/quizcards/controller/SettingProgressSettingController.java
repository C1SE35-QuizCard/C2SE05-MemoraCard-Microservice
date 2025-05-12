package com.example.quizcards.controller;


import com.example.quizcards.dto.request.SetProgressSettingRequest;
import com.example.quizcards.dto.response.SetProgressSettingResponse;
import com.example.quizcards.entities.SetProgressSetting;
import com.example.quizcards.security.UserPrincipal;
import com.example.quizcards.service.impl.SetProgressSettingServiceImpl;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/setting-progress")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class SettingProgressSettingController {
    SetProgressSettingServiceImpl settingService;

    @GetMapping("/get-setting")
    public ResponseEntity<?> getDefaultSetting(@RequestParam Long setId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserPrincipal up = (UserPrincipal) auth.getPrincipal();
        SetProgressSetting setting = settingService.getSettings(setId, up.getId());
        return ResponseEntity.ok(SetProgressSettingResponse.from(setting));
    }

    @GetMapping("/get-effective-setting")
    public ResponseEntity<?> getEffectiveSetting(@RequestParam Long setId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserPrincipal up = (UserPrincipal) auth.getPrincipal();
        SetProgressSetting setting = settingService.getEffectiveSettings(setId, up.getId());
        return ResponseEntity.ok(SetProgressSettingResponse.from(setting));
    }

    @PostMapping("/save-setting")
    public ResponseEntity<?> saveSetting(@Valid @RequestBody SetProgressSettingRequest request) {
        settingService.saveOrUpdateSettings(request);
        return ResponseEntity.ok("Setting saved successfully");
    }
}
