package com.microservices.streakservice.controller.v1;

import com.microservices.dto.security.UserPrincipal;
import com.microservices.streakservice.dto.StreakStatus;
import com.microservices.streakservice.dto.request.streak.StreakRequest;
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

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static lombok.AccessLevel.PRIVATE;

@RestController
@RequestMapping("/v1/ms/streak-learning")
@RequiredArgsConstructor
@FieldDefaults(level = PRIVATE, makeFinal = true)
@Slf4j
public class StreakController {
    StreakServiceImpl streakService;

    @GetMapping("/get-learned-data-in-client-time")
    @PreAuthorize("hasAnyRole('ROLE_FREE_USER','ROLE_PREMIUM_USER','ROLE_ADMIN')")
    public Mono<ResponseEntity<StreakAnalysisResponse>> getLearnedDataInClientTime(
            @RequestParam int offsetHours,
            @RequestParam int offsetMinutes,
            @RequestParam(value = "locale", defaultValue = "en-US") String localeCode
    ) {
        if (offsetHours < -12 || offsetHours > 14 ||
                offsetMinutes < -59 || offsetMinutes > 59) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid offset");
        }

        return ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .filter(Authentication::isAuthenticated)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED)))
                .map(auth -> ((UserPrincipal) auth.getPrincipal()).getId())
                .flatMap(userId ->
                        streakService.getAnalysisStreak(userId, offsetHours, offsetMinutes, localeCode)
                )
                .flatMap(response ->
                        Mono.just(ResponseEntity.ok(response))
                );
    }

    @GetMapping("/get-learned-data-by-month-year")
    @PreAuthorize("hasAnyRole('ROLE_FREE_USER','ROLE_PREMIUM_USER','ROLE_ADMIN')")
    public Mono<ResponseEntity<List<StreakStatus>>> getLearnedDataByMonthYear(
            @RequestParam int month,
            @RequestParam int year,
            @RequestParam(value = "locale", defaultValue = "en-US") String localeCode
    ) {
        return ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .filter(Authentication::isAuthenticated)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED)))
                .flatMapMany(auth -> {
                    Long userId = ((UserPrincipal) auth.getPrincipal()).getId();
                    return streakService.getLearnedDetails(userId, month, year, localeCode);
                })
                .collectList()
                .map(ResponseEntity::ok);
    }

    @GetMapping("/get-all-learned-data")
    @PreAuthorize("hasAnyRole('ROLE_FREE_USER','ROLE_PREMIUM_USER','ROLE_ADMIN')")
    public Mono<ResponseEntity<List<StreakStatus>>> getAllLearnedData(
            @RequestParam(value = "locale", defaultValue = "en-US") String localeCode
    ) {
        return ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .filter(Authentication::isAuthenticated)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED)))
                .flatMapMany(auth -> {
                    Long userId = ((UserPrincipal) auth.getPrincipal()).getId();
                    return streakService.getAllLearnedDate(userId, localeCode);
                })
                .collectList()
                .map(ResponseEntity::ok);
    }

    @GetMapping("/get-learned-data-by-date-range")
    @PreAuthorize("hasAnyRole('ROLE_FREE_USER','ROLE_PREMIUM_USER','ROLE_ADMIN')")
    public Mono<ResponseEntity<Map<String, Object>>> getLearnedDataByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(value = "locale", defaultValue = "en-US") String localeCode,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "40") int size
    ) {
        return ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .filter(Authentication::isAuthenticated)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED)))
                .flatMap(auth -> {
                    Long userId = ((UserPrincipal) auth.getPrincipal()).getId();
                    return streakService.getLearnedByDateRange(
                            userId, startDate, endDate, localeCode, page, size
                    );
                })
                .map(result -> {
                    List<StreakStatus> statuses = result.getT1();
                    Long totalCount = result.getT2();
                    return ResponseEntity.ok(
                            Map.of(
                                    "content", statuses,
                                    "totalElements", totalCount
                            )
                    );
                });
    }

    @PatchMapping("/update-streak-data")
    @PreAuthorize("hasAnyRole('ROLE_FREE_USER','ROLE_PREMIUM_USER','ROLE_ADMIN')")
    public Mono<ResponseEntity<?>> updateStreakData(
            @Valid @RequestBody Mono<StreakRequest> requestMono
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
                                    return streakService.generateStreak(user, request.getTimeFromClient())
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