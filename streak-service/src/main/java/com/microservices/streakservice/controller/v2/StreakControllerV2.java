package com.microservices.streakservice.controller.v2;

import com.microservices.dto.security.UserPrincipal;
import com.microservices.streakservice.dto.request.streak.StreakRequestV2;
import com.microservices.streakservice.dto.response.StreakAnalysisResponse;
import com.microservices.streakservice.model.AppUser;
import com.microservices.streakservice.service.StreakServiceImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.stream.Collectors;

import static lombok.AccessLevel.PRIVATE;

@RestController
@RequestMapping("/v2/ms/streak-learning")
@RequiredArgsConstructor
@FieldDefaults(level = PRIVATE, makeFinal = true)
@Slf4j
public class StreakControllerV2 {
    StreakServiceImpl streakService;

    @GetMapping("/get-learned-data-in-client-time")
    @PreAuthorize("hasAnyRole('ROLE_FREE_USER','ROLE_PREMIUM_USER','ROLE_ADMIN')")
    public Mono<ResponseEntity<StreakAnalysisResponse>> getLearnedDataInClientTimeV2(
            @RequestParam("timeFromClient")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            OffsetDateTime timeFromClient,
            @RequestParam(value = "locale", defaultValue = "en-US") String localeCode
    ) {
        ZoneOffset off = timeFromClient.getOffset();
        int h = off.getTotalSeconds() / 3600;
        if (h < -12 || h > 14) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid timezone offset");
        }

        return ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .filter(Authentication::isAuthenticated)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED)))
                .map(auth -> (UserPrincipal) auth.getPrincipal())
                .map(up -> AppUser.builder().userId(up.getId()).build())
                .flatMap(user -> streakService.getAnalysisStreakV2(user, timeFromClient, localeCode))
                .map(ResponseEntity::ok);
    }

    @GetMapping("/get-learned-data-in-client-time-v3")
    @PreAuthorize("hasAnyRole('ROLE_FREE_USER','ROLE_PREMIUM_USER','ROLE_ADMIN')")
    public Mono<ResponseEntity<StreakAnalysisResponse>> getLearnedDataInClientTimeV3(
            @RequestParam(value = "locale", defaultValue = "en-US") String localeCode
    ) {
        return ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .filter(Authentication::isAuthenticated)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED)))
                .map(auth -> {
                    UserPrincipal up = (UserPrincipal) auth.getPrincipal();
                    return AppUser.builder()
                            .userId(up.getId())
                            .userTz(String.valueOf(ZoneOffset.of(up.getUserTz())))
                            .build();
                })
                .flatMap(user -> streakService.getAnalysisStreakV3(user, localeCode))
                .map(ResponseEntity::ok);
    }

    @PatchMapping("/update-streak-data")
    @PreAuthorize("hasAnyRole('ROLE_FREE_USER','ROLE_PREMIUM_USER','ROLE_ADMIN')")
    public Mono<ResponseEntity<?>> updateStreakDataV2(
            @Valid @RequestBody Mono<StreakRequestV2> requestMono
    ) {
        return requestMono
                .flatMap(request ->
                        ReactiveSecurityContextHolder.getContext()
                                .map(SecurityContext::getAuthentication)
                                .filter(Authentication::isAuthenticated)
                                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED)))
                                .flatMap(auth -> {
                                    UserPrincipal up = (UserPrincipal) auth.getPrincipal();
                                    AppUser user = AppUser.builder()
                                            .userId(up.getId())
                                            .userTz(String.valueOf(ZoneOffset.of(up.getUserTz())))
                                            .username(up.getUsername())
                                            .build();
                                    return streakService.generateStreakV2(
                                                    user,
                                                    request.getOffsetHours(),
                                                    request.getOffsetMinutes()
                                            )
                                            .map(ok -> ok
                                                    ? ResponseEntity.ok("Streak updated")
                                                    : ResponseEntity.status(HttpStatus.NOT_MODIFIED).build()
                                            );
                                })
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

//    private Mono<ResponseEntity<?>> validationError(BindingResult br) {
//        if (!br.hasErrors()) {
//            return Mono.empty();
//        }
//        Map<String, String> detail = br.getFieldErrors().stream()
//                .collect(Collectors.toMap(FieldError::getField, FieldError::getDefaultMessage));
//        Map<String, Object> body = Map.of(
//                "message", "Validation errors",
//                "errors", detail
//        );
//        return Mono.just(ResponseEntity.badRequest().body(body));
//    }
}