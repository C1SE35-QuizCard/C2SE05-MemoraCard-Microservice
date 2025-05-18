package com.microservices.securityservice.controller;


import com.microservices.dto.security.UserInfo;
import com.microservices.security.JwtTokenProvider;
import com.microservices.securityservice.service.JwtAuthenticationService;
import com.microservices.securityservice.utils.TokenUtils;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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

    @Value("${jwt.secret}")
    @NonFinal
    String jwtSecret;

    @PostMapping("/validate-token")
    public Mono<ResponseEntity<UserInfo>> validateToken(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader
    ) {
        return authService.validateAccessToken(authHeader)
                .map(result ->
                        ResponseEntity.ok()
                                .header("X-Token-ID", result.jti())   // ← đính jti
                                .body(result.userInfo())
                )
                .onErrorResume(ExpiredJwtException.class, e ->
                        Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                .header("X-Token-ID", extractJtiSafely(authHeader))
                                .body(UserInfo.builder().isEnabled(false).build()))
                )
                .onErrorResume(JwtException.class, e ->
                        Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                .header("X-Token-ID", extractJtiSafely(authHeader))
                                .body(UserInfo.builder().isEnabled(false).build()))
                )
                .onErrorResume(IllegalArgumentException.class, e ->
                        Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                .header("X-Token-ID", extractJtiSafely(authHeader))
                                .body(UserInfo.builder().isEnabled(false).build()))
                )
                .onErrorResume(IllegalStateException.class, e ->
                        Mono.just(ResponseEntity.status(HttpStatus.FORBIDDEN)
                                .header("X-Token-ID", extractJtiSafely(authHeader))
                                .body(UserInfo.builder().isEnabled(false).build()))
                )
                .onErrorResume(Exception.class, e -> {
                    log.error("Unexpected error during validation", e);
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .header("X-Token-ID", extractJtiSafely(authHeader))
                            .body(UserInfo.builder().isEnabled(false).build()));
                });
    }

    private String extractJtiSafely(String authHeader) {
        try {
            String token = TokenUtils.extractToken(authHeader);
            JwtTokenProvider p = new JwtTokenProvider(jwtSecret);
            Object j = p.getPropertiesFromClaimsAllowExpired(token).get("jti");
            return j != null ? j.toString() : "";
        } catch (Exception ex) {
            return "";
        }
    }
}
