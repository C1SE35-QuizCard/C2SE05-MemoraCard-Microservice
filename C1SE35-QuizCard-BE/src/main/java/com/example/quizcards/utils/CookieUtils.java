package com.example.quizcards.utils;

import com.example.quizcards.dto.response.JwtAuthenticationResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CookieUtils {
    @Value("${jwt.jwtExpirationInSec}")
    @NonFinal
    Long jwtExpirationInSec;

    @Value("${jwt.refreshTokenExpirationInSec}")
    @NonFinal
    Long refreshTokenDurationSec;

    @Value("access_token")
    @NonFinal
    String accessTokenName;

    @Value("refresh_token")
    @NonFinal
    String refreshTokenName;

    public JwtAuthenticationResponse generateTokenToCookie(HttpServletResponse response,
                                                           String accessToken,
                                                           String refreshToken) {
        ResponseCookie cookie = ResponseCookie.from(accessTokenName, accessToken)
                .httpOnly(true)
                .secure(true)
                .sameSite("Strict")
                .path("/")
                .maxAge(jwtExpirationInSec)
                .build();

        ResponseCookie newRefreshTokenCookie = ResponseCookie.from(refreshTokenName, refreshToken)
                .httpOnly(true)
                .secure(true)
                .sameSite("Strict")
                .path("/")
                .maxAge(refreshTokenDurationSec)
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
        response.addHeader(HttpHeaders.SET_COOKIE, newRefreshTokenCookie.toString());

        return new JwtAuthenticationResponse(accessToken, refreshToken, "success");
    }

    public void removeTokenInClient(HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from(accessTokenName, "")
                .httpOnly(true)
                .secure(true)
                .sameSite("None")
                .path("/")
                .maxAge(0)
                .build(); // Thời gian tồn tại của cookie (0)

        ResponseCookie newRefreshTokenCookie = ResponseCookie.from(refreshTokenName, "")
                .httpOnly(true)
                .secure(true)
                .sameSite("None")
                .path("/")
                .maxAge(0)
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
        response.addHeader(HttpHeaders.SET_COOKIE, newRefreshTokenCookie.toString());
    }

    public Cookie findCookie(Cookie[] cookies, String key) {
        if (cookies == null) return null;

        for (Cookie cookie : cookies) {
            if (cookie.getName().equals(key)) {
                return cookie;
            }
        }

        return null;
    }
}
