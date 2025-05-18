package com.microservices.securityservice.service;

import com.microservices.dto.security.UserInfo;
import com.microservices.dto.security.UserPrincipal;
import com.microservices.security.JwtTokenProvider;

import com.microservices.securityservice.dto.TokenValidationResult;
import com.microservices.securityservice.utils.TokenUtils;
import com.microservices.utils.ReactiveRedisUtils;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.text.MessageFormat;
import java.util.Map;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class JwtAuthenticationService {
    ICustomUserDetailsService userDetailsService;

    ReactiveRedisTemplate<String, Object> reactiveRedisTemplate;

    @Value("${redis_config.token_blacklist_prefix}")
    @NonFinal
    String tokenBlacklistPrefix;

    @Value("${redis_config.token_iat_prefix}")
    @NonFinal
    String tokenIatPrefix;

    @Value("${jwt.secret}")
    @NonFinal
    String jwtSecret;

    public Mono<TokenValidationResult> validateAccessToken(String authorizationHeader) {
        JwtTokenProvider tokenProvider = new JwtTokenProvider(jwtSecret);
        ReactiveRedisUtils redisUtils = new ReactiveRedisUtils(reactiveRedisTemplate);
        String token = TokenUtils.extractToken(authorizationHeader);
        if (token == null) {
            return Mono.error(new IllegalArgumentException("Missing or malformed Authorization header"));
        }
        try {
            if (!tokenProvider.validateToken(token)) {
                return Mono.error(new JwtException("Invalid JWT"));
            }
            Map<String, Object> claims = tokenProvider.getPropertiesFromClaims(token);
            if (!"access_token".equals(claims.get("type"))) {
                return Mono.error(new JwtException("Wrong token type"));
            }
            Long userId = ((Number) claims.get("uid")).longValue();
            String jti = (String) claims.get("jti");
            long createdAt = ((Number) claims.get("created_at")).longValue();
            String username = tokenProvider.getUsernameFromJWT(token);

            String blacklistKey = MessageFormat.format("{0}_{1}_{2}", tokenBlacklistPrefix, userId, jti);
            String iatKey = MessageFormat.format("{0}_{1}", tokenIatPrefix, userId);

            Mono<Boolean> isBlacklistedMono = redisUtils.getFromRedis(blacklistKey, Boolean.class).defaultIfEmpty(false);
            Mono<Long> lastLogoutAllTsMono = redisUtils.getFromRedis(iatKey, Long.class).defaultIfEmpty(0L);

            return Mono.zip(isBlacklistedMono, lastLogoutAllTsMono)
                    .flatMap(tuple -> {
                        boolean isBlacklisted = tuple.getT1();
                        long lastLogoutAllTs = tuple.getT2();

                        if (isBlacklisted) {
                            return Mono.error(new JwtException("Token blacklisted"));
                        }

                        if (createdAt < lastLogoutAllTs) {
                            return Mono.error(new JwtException("Token expired"));
                        }

                        return userDetailsService.findByUsernameOnly(username)
                                .cast(UserPrincipal.class)
                                .flatMap(up -> {
                                    if (!up.isEnabled()) {
                                        return Mono.error(new IllegalStateException("User disabled"));
                                    }
                                    UserInfo info = UserInfo.from(up);
                                    return Mono.just(new TokenValidationResult(info, jti));
                                });
                    });
        } catch (ExpiredJwtException ex) {
            return Mono.error(ex);
        } catch (JwtException | IllegalArgumentException ex) {
            return Mono.error(ex);
        }
    }
}
