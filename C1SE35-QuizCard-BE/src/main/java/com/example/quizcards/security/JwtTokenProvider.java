package com.example.quizcards.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class JwtTokenProvider {
    @Value("${jwt.secret}")
    @NonFinal
    String jwtSecret;

    @Value("${jwt.jwtExpirationInSec}")
    @NonFinal
    private Long jwtExpirationInSec;

    @Value("${jwt.refreshTokenExpirationInSec}")
    @NonFinal
    Long refreshTokenDurationSec;

    // Tạo Refresh Token
    public String generateRefreshToken(UserPrincipal userPrincipal) {
        Map<String, Object> claims = generateClaims(userPrincipal);
        claims.put("type", "refresh_token");
        return generateToken(claims, refreshTokenDurationSec, userPrincipal.getUserName(), null);
    }

    // Tạo Access Token
    public String generateAccessToken(UserPrincipal userPrincipal) {
        Map<String, Object> claims = generateClaims(userPrincipal);
        claims.put("type", "access_token");
        return generateToken(claims, jwtExpirationInSec, userPrincipal.getUserName(), null);
    }

    // Lấy Username từ JWT
    public String getUsernameFromJWT(String token) {
        return extractClaim(token, Claims::getSubject);
    }


    public Map<String, Object> getPropertiesFromClaims(String token) {
        return extractAllClaims(token, null);
    }

    public String generateToken(Map<String, Object> extraClaims,
                                long expiration,
                                String subject,
                                String key) {
        if (key == null) key = jwtSecret;
        return Jwts.builder()
                .claims(extraClaims)
                .subject(subject)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiration * 1000))
                .signWith(getSignInKey(key), Jwts.SIG.HS256)
                .compact();
    }

    public Claims extractAllClaims(String token, String key) {
        if (key == null) key = jwtSecret;
        return Jwts
                .parser()
                .verifyWith(getSignInKey(key))
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    // Xác thực JWT Token

    public boolean validateToken(String authToken) {
        try {
            Jwts.parser().verifyWith(getSignInKey(null)).build().parseSignedClaims(authToken);
            return true;
        } catch (SignatureException ex) {
            log.error("Invalid JWT signature");
        } catch (ExpiredJwtException ex) {
            log.error("Expired JWT token");
        } catch (UnsupportedJwtException ex) {
            log.error("Unsupported JWT token");
        } catch (IllegalArgumentException ex) {
            log.error("JWT claims string is empty");
        }
        return false;
    }

    // Tạo claims chứa thông tin người dùng
    private Map<String, Object> generateClaims(UserPrincipal userPrincipal) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("user_name", userPrincipal.getUserName());
        String roles = userPrincipal.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));
        claims.put("roles", roles);
        claims.put("uid", userPrincipal.getId());
        claims.put("jti", UUID.randomUUID().toString());
        claims.put("created_at", Instant.now().toEpochMilli());
        return claims;
    }


    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token, null);
        return claimsResolver.apply(claims);
    }

    private SecretKey getSignInKey(String secretKey) {
        if (secretKey == null) secretKey = jwtSecret;
        byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }


    @Deprecated
    public Object getDataFromClaims(String token, String key) {
        return extractClaim(token, claims -> claims.get(key));
    }

    @Deprecated
    public Long getUserIdFromJWT(String token) {
        return Long.valueOf(extractClaim(token, Claims::getSubject));
    }

    @Deprecated
    public boolean isTokenValid(String token, UserPrincipal userDetails) {
        if (!validateToken(token)) {
            return false;
        }
        final String username = getUsernameFromJWT(token);
        return username.equals(userDetails.getUserName());
    }

}

