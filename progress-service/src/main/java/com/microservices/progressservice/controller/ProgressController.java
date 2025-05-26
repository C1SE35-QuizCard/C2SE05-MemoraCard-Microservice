package com.microservices.progressservice.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microservices.dto.security.UserPrincipal;
import com.microservices.progressservice.dto.request.GetFlashcardRequest;
import com.microservices.progressservice.dto.request.UserProgressRequest;
import com.microservices.progressservice.service.SetServiceImpl;
import com.microservices.progressservice.service.UserProgressServiceImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

import static lombok.AccessLevel.PRIVATE;

@RestController
@RequestMapping("")
@RequiredArgsConstructor
@FieldDefaults(level = PRIVATE, makeFinal = true)
public class ProgressController {

    UserProgressServiceImpl userProgressService;
    SetServiceImpl setService;
    ObjectMapper objectMapper;

    private static final String FETCH_ERROR_MESSAGE = "An error occurred while fetching user progress";

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

    /**
     * GET /v1/progress/user/set/{set_id}
     * Forward X-Set-Password, X-Set-Password-Valid-At from headers.
     */
    @GetMapping("/set/{set_id}")
    @PreAuthorize("hasAnyRole('ROLE_FREE_USER','ROLE_PREMIUM_USER','ROLE_ADMIN')")
    public Mono<ResponseEntity<?>> findFlashcardsProgressBySetId(
            ServerHttpRequest request,
            @PathVariable("set_id") Long setId
    ) {
        return ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .filter(Authentication::isAuthenticated)
                .flatMap(auth -> {
                    UserPrincipal up = (UserPrincipal) auth.getPrincipal();
                    String rawPwd = request.getHeaders().getFirst("X-Set-Password");
                    String rawValidAt = request.getHeaders().getFirst("X-Set-Password-Valid-At");
                    Map<String, String> headers = request.getHeaders().toSingleValueMap();
                    return setService.checkAccess(up, setId, rawPwd, rawValidAt)
                            .thenMany(userProgressService.findFlashcardsProgressBySetId(setId, up.getId()))
                            .collectList()
                            .map(list -> list.isEmpty()
                                    ? ResponseEntity.noContent().build()
                                    : ResponseEntity.ok(list)
                            );
                })
                .onErrorResume(e -> e instanceof ResponseStatusException
                        ? Mono.just(
                                ResponseEntity
                                        .status(((ResponseStatusException) e).getStatusCode())
                                        .body(((ResponseStatusException) e).getBody()))
                        : Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.emptyList()))
                );
    }

    /**
     * POST /v1/progress/user/get-by-card-ids
     * Deprecated in original, still supported.
     */
    @PostMapping("/get-by-card-ids")
    @PreAuthorize("hasAnyRole('ROLE_FREE_USER','ROLE_PREMIUM_USER','ROLE_ADMIN')")
    public Mono<ResponseEntity<?>> findFlashcardsByCardIds(
            @Valid @RequestBody Mono<GetFlashcardRequest> requestMono
    ) {
        return requestMono
                .flatMap(request ->
                        ReactiveSecurityContextHolder.getContext()
                                .map(SecurityContext::getAuthentication)
                                .filter(Authentication::isAuthenticated)
                                .flatMapMany(auth -> {
                                    UserPrincipal up = (UserPrincipal) auth.getPrincipal();
                                    return userProgressService.findCardProgressesByCardIdsIn(
                                            up.getId(),
                                            request.getSetId(),
                                            request.getCardIds(),
                                            request.getCardIds().size()
                                    );
                                })
                                .collectList()
                                .map(list -> list.isEmpty()
                                        ? ResponseEntity.noContent().build()
                                        : ResponseEntity.ok(list)
                                )
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

    /**
     * GET /v1/progress/user/analysis/set/{set_id}
     */
    @GetMapping("/analysis/set/{set_id}")
    @PreAuthorize("hasAnyRole('ROLE_FREE_USER','ROLE_PREMIUM_USER','ROLE_ADMIN')")
    public Mono<ResponseEntity<?>> getAnalysisProgress(
            ServerHttpRequest request,
            @PathVariable("set_id") Long setId
    ) {
        return ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .filter(Authentication::isAuthenticated)
                .flatMap(auth -> {
                    UserPrincipal up = (UserPrincipal) auth.getPrincipal();
                    return userProgressService.findAnalysisProgressBySetId(setId, up.getId())
                            .map(dto -> dto != null
                                    ? ResponseEntity.ok(dto)
                                    : ResponseEntity.notFound().build()
                            );
                })
                .onErrorResume(e -> Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()));
    }

    /**
     * Deprecated create
     */
    @PostMapping("/create")
    @Deprecated
    public Mono<ResponseEntity<Map<String, Object>>> addUserProgress(
            @Valid @RequestBody Mono<UserProgressRequest> reqMono
    ) {
        return reqMono
                .flatMap(req ->
                        userProgressService.existsByUserIdAndCardId(req.getUserId(), req.getCardId())
                                .flatMap(exists -> {
                                    if (exists) {
                                        return Mono.just(ResponseEntity
                                                .status(HttpStatus.CONFLICT)
                                                .body(Map.<String, Object>of(
                                                        "message", "A progress for this user and card already exists."
                                                ))
                                        );
                                    }
                                    return userProgressService.addUserProgress(
                                                    req.getProgressType(),
                                                    req.getIsAttention(),
                                                    req.getUserId(),
                                                    req.getCardId()
                                            )
                                            .thenReturn(ResponseEntity
                                                    .status(HttpStatus.CREATED)
                                                    .body(Map.<String, Object>of(
                                                            "message", "User Progress created successfully"
                                                    ))
                                            );
                                })
                )
                // Reactive validation errors
                .onErrorResume(WebExchangeBindException.class, ex -> {
                    Map<String, String> errors = ex.getFieldErrors().stream()
                            .collect(Collectors.toMap(
                                    FieldError::getField,
                                    FieldError::getDefaultMessage
                            ));
                    // ép kiểu rõ ràng
                    Map<String, Object> body = Map.of(
                            "message", "Validation errors",
                            "errors", errors
                    );
                    return Mono.just(ResponseEntity
                            .badRequest()
                            .body(body)
                    );
                })
                // Other unexpected errors
                .onErrorResume(e -> Mono.just(ResponseEntity
                        .status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(Map.of(
                                "message", "An error occurred while creating the User Progress"
                        ))
                ));
    }


    /**
     * Deprecated delete
     */
    @DeleteMapping("/delete/{id}")
    public Mono<ResponseEntity<String>> deleteUserProgress(@PathVariable("id") Long progressId) {
        return userProgressService.findUserProgressById(progressId)
                .flatMap(dto -> userProgressService.deleteUserProgressById(progressId)
                        .thenReturn(ResponseEntity.ok("Deleted"))
                )
                .switchIfEmpty(Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).body("Not Found")))
                .onErrorResume(e -> Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error")));
    }

    /**
     * Deprecated update
     */
    @PutMapping("/update")
    @Deprecated
    public Mono<ResponseEntity<Map<String, Object>>> updateUserProgress(
            @Valid @RequestBody Mono<UserProgressRequest> reqMono
    ) {
        return reqMono
                .flatMap(req ->
                        userProgressService.existsByUserIdAndCardIdAndNotId(
                                        req.getUserId(), req.getCardId(), req.getProgressId()
                                )
                                .flatMap(conflict -> {
                                    if (conflict) {
                                        return Mono.just(ResponseEntity
                                                .status(HttpStatus.CONFLICT)
                                                .body(Map.<String, Object>of(
                                                        "message", "A progress for this user and card already exists."
                                                ))
                                        );
                                    }
                                    return userProgressService.updateUserProgress(req)
                                            .thenReturn(ResponseEntity
                                                    .ok(Map.<String, Object>of(
                                                            "message", "User Progress updated successfully"
                                                    ))
                                            );
                                })
                )
                .onErrorResume(WebExchangeBindException.class, ex -> {
                    Map<String, String> errors = ex.getFieldErrors().stream()
                            .collect(Collectors.toMap(
                                    FieldError::getField,
                                    FieldError::getDefaultMessage
                            ));
                    Map<String, Object> body = Map.of(
                            "message", "Validation errors",
                            "errors", errors
                    );
                    return Mono.just(ResponseEntity
                            .badRequest()
                            .body(body)
                    );
                })
                .onErrorResume(e -> Mono.just(ResponseEntity
                        .status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(Map.of(
                                "message", "An error occurred while updating the User Progress"
                        ))
                ));
    }

    /**
     * Assign progress endpoints
     */

    @PatchMapping("/assign-progress")
    @PreAuthorize("hasAnyRole('ROLE_FREE_USER','ROLE_PREMIUM_USER','ROLE_ADMIN')")
    public Mono<ResponseEntity<Map<String, Object>>> assignProgress(
            @Valid @RequestBody Mono<UserProgressRequest> requestMono
    ) {
        return requestMono
                .flatMap(request ->
                        ReactiveSecurityContextHolder.getContext()
                                .map(SecurityContext::getAuthentication)
                                .filter(Authentication::isAuthenticated)
                                .flatMap(auth -> userProgressService.assignUserProgress(
                                        (UserPrincipal) auth.getPrincipal(),
                                        request
                                ))
                                .map(ResponseEntity::ok)
                                .onErrorResume(e ->
                                        Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                                .body(Map.of("message", FETCH_ERROR_MESSAGE)))
                                )
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

    /**
     * Reset progress
     */
    @DeleteMapping("/reset-progress/{set_id}")
    @PreAuthorize("hasAnyRole('ROLE_FREE_USER','ROLE_PREMIUM_USER','ROLE_ADMIN')")
    public Mono<ResponseEntity<Void>> resetUserProgress(@PathVariable("set_id") Long setId) {
        return userProgressService.resetUserProgress(setId)
                .thenReturn(ResponseEntity.ok().<Void>build())
                .onErrorResume(e -> Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()));
    }
}
