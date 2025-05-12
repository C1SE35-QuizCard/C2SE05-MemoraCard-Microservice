package com.microservices.securityservice.utils;

import org.springframework.util.StringUtils;

public class TokenUtils {
    private TokenUtils() {
        // no–op
    }

    /** Trích phần token từ header "Bearer ..." */
    public static String extractToken(String authorizationHeader) {
        if (StringUtils.hasText(authorizationHeader) && authorizationHeader.startsWith("Bearer ")) {
            return authorizationHeader.substring(7);
        }
        return null;
    }

    /** Che dấu một phần token khi log */
    public static String maskToken(String token) {
        if (token == null || token.length() < 12) return "****";
        return token.substring(0, 6) + "..." + token.substring(token.length() - 6);
    }
}
