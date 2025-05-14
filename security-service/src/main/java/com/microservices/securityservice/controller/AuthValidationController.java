package com.microservices.securityservice.controller;


import com.microservices.dto.security.UserInfo;
import com.microservices.securityservice.service.JwtAuthenticationService;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/internal")
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class AuthValidationController {
    JwtAuthenticationService authService;

    @PostMapping("/validate-token")
    public Mono<ResponseEntity<UserInfo>> validateToken(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader
    ) {
        return authService.validateAccessToken(authHeader)
                .map(ResponseEntity::ok)
                .onErrorResume(ExpiredJwtException.class, e ->
                        Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                .body(UserInfo.builder().isEnabled(false).build()))
                )
                .onErrorResume(JwtException.class, e ->
                        Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                .body(UserInfo.builder().isEnabled(false).build()))
                )
                .onErrorResume(IllegalArgumentException.class, e ->
                        Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                .body(UserInfo.builder().isEnabled(false).build()))
                )
                .onErrorResume(IllegalStateException.class, e ->
                        Mono.just(ResponseEntity.status(HttpStatus.FORBIDDEN)
                                .body(UserInfo.builder().isEnabled(false).build()))
                )
                .onErrorResume(Exception.class, e -> {
                    log.error("Unexpected error during validation", e);
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(UserInfo.builder().isEnabled(false).build()));
                });
    }
}
