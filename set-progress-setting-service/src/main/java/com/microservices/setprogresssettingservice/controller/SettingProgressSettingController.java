package com.microservices.setprogresssettingservice.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microservices.dto.security.UserPrincipal;
import com.microservices.setprogresssettingservice.dto.request.SetProgressSettingRequest;
import com.microservices.setprogresssettingservice.dto.response.SetProgressSettingResponse;
import com.microservices.setprogresssettingservice.service.SetProgressSettingServiceImpl;
import com.microservices.setprogresssettingservice.service.SetServiceImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

import static lombok.AccessLevel.PRIVATE;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@RestController
@RequestMapping("")
@RequiredArgsConstructor
@FieldDefaults(level = PRIVATE, makeFinal = true)
@Slf4j
public class SettingProgressSettingController {

    SetProgressSettingServiceImpl settingService;

    SetServiceImpl setService;

    ObjectMapper objectMapper;

//    private Mono<ResponseEntity<?>> validationError(BindingResult br) {
//        if (!br.hasErrors()) {
//            return Mono.empty();
//        }
//        Map<String, String> detail = br.getFieldErrors()
//                .stream()
//                .collect(Collectors.toMap(FieldError::getField, FieldError::getDefaultMessage));
//        Map<String, Object> body = Map.of(
//                "message", "Validation errors",
//                "errors", detail
//        );
//        return Mono.just(ResponseEntity.badRequest().body(body));
//    }

    @GetMapping("/get-setting")
    @PreAuthorize("hasAnyRole('ROLE_FREE_USER','ROLE_PREMIUM_USER','ROLE_ADMIN')")
    public Mono<ResponseEntity<Object>> getDefaultSetting(
            @RequestParam Long setId,
            @RequestHeader("X-Set-Password") String rawPwd,
            @RequestHeader(value = "X-Set-Password-Valid-At", required = false) String rawValidAt
    ) {
        return ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .filter(Authentication::isAuthenticated)
                .flatMap(auth -> {
                    UserPrincipal up = (UserPrincipal) auth.getPrincipal();
                    // 1) checkAccess -> 2) getSettings -> 3) map to ResponseEntity
                    return setService
                            .checkAccess(up, setId, rawPwd, rawValidAt)
                            .then(settingService.getSettings(setId, up.getId()))
                            .map(SetProgressSettingResponse::from)
                            .map(data -> ResponseEntity.ok().body((Object) data));
                })
                .switchIfEmpty(Mono.error(new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.UNAUTHORIZED, "Unauthenticated"
                )))
                .onErrorResume(e -> e instanceof ResponseStatusException
                        ? Mono.just(
                        ResponseEntity
                                .status(((ResponseStatusException) e).getStatusCode())
                                .body(((ResponseStatusException) e).getBody()))
                        : Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.emptyList()))
                );
    }

    @GetMapping("/get-effective-setting")
    @PreAuthorize("hasAnyRole('ROLE_FREE_USER','ROLE_PREMIUM_USER','ROLE_ADMIN')")
    public Mono<ResponseEntity<Object>> getEffectiveSetting(
            @RequestParam Long setId,
            @RequestHeader("X-Set-Password") String rawPwd,
            @RequestHeader(value = "X-Set-Password-Valid-At", required = false) String rawValidAt
    ) {
        return ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .filter(Authentication::isAuthenticated)
                .flatMap(auth -> {
                    UserPrincipal up = (UserPrincipal) auth.getPrincipal();
                    return setService
                            .checkAccess(up, setId, rawPwd, rawValidAt)
                            .then(settingService.getEffectiveSettings(setId, up.getId()))
                            .map(SetProgressSettingResponse::from)
                            .map(data -> ResponseEntity.ok().body((Object) data));
                })
                .switchIfEmpty(Mono.error(new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.UNAUTHORIZED, "Unauthenticated"
                )))
                .onErrorResume(e -> e instanceof ResponseStatusException
                        ? Mono.just(
                        ResponseEntity
                                .status(((ResponseStatusException) e).getStatusCode())
                                .body(((ResponseStatusException) e).getBody()))
                        : Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.emptyList()))
                );
    }

    @PostMapping("/save-setting")
    @PreAuthorize("hasAnyRole('ROLE_FREE_USER','ROLE_PREMIUM_USER','ROLE_ADMIN')")
    public Mono<ResponseEntity<Map<String, Object>>> saveSetting(
            @Valid @RequestBody Mono<SetProgressSettingRequest> requestMono
    ) {
        return requestMono
                .flatMap(request ->
                        ReactiveSecurityContextHolder.getContext()
                                .map(SecurityContext::getAuthentication)
                                .filter(Authentication::isAuthenticated)
                                .flatMap(auth -> {
                                    Long userId = ((UserPrincipal) auth.getPrincipal()).getId();
                                    return settingService.saveOrUpdateSettings(request, userId)
                                            .thenReturn(ResponseEntity.ok(
                                                    Map.<String, Object>of("message", "Setting saved successfully")
                                            ));
                                })
                                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthenticated")))
                )
                .onErrorResume(WebExchangeBindException.class, ex -> {
                    Map<String, String> errors = ex.getFieldErrors().stream()
                            .collect(Collectors.toMap(FieldError::getField, FieldError::getDefaultMessage));
                    Map<String, Object> body = Map.of(
                            "message", "Validation errors",
                            "errors", errors
                    );
                    return Mono.just(ResponseEntity.badRequest().body(body));
                });
    }

    @GetMapping("/get-current-simple-mode-version")
    @PreAuthorize("hasAnyRole('ROLE_FREE_USER','ROLE_PREMIUM_USER','ROLE_ADMIN')")
    public Mono<ResponseEntity<Map<String, Long>>> getCurrentSimpleModeVersion(
            @RequestParam Long cardId
    ) {
        return ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .filter(Authentication::isAuthenticated)
                .flatMap(auth -> {
                    UserPrincipal up = (UserPrincipal) auth.getPrincipal();
                    return settingService.findCurrentSimpleModeVersionByCardIdAndUserId(cardId, up.getId())
                            .map(version -> ResponseEntity.ok(Map.of("currentSimpleModeVersion", version)))
                            .switchIfEmpty(Mono.error(new ResponseStatusException(
                                    org.springframework.http.HttpStatus.NOT_FOUND,
                                    "Current simple mode version not found for cardId=" + cardId
                            )));
                })
                .switchIfEmpty(Mono.error(new ResponseStatusException(
                        org.springframework.http.HttpStatus.UNAUTHORIZED, "Unauthenticated"
                )));
    }
}
