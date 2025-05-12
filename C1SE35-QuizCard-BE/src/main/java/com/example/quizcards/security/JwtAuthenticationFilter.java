package com.example.quizcards.security;

import com.example.quizcards.dto.response.ApiResponse;
import com.example.quizcards.exception.TokenRefreshException;
import com.example.quizcards.service.ICustomUserDetailsService;
import com.example.quizcards.utils.RedisUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.text.MessageFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    JwtTokenProvider tokenProvider;

    ICustomUserDetailsService customUserDetailsService;

    RedisUtils redisUtils;

    @Value("TOKEN_BLACKLIST")
    @NonFinal
    String tokenBlacklistPrefix;

    @Value("TOKEN_IAT_AVAILABLE")
    @NonFinal
    String tokenIatPrefix;

//    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
//            System.out.println(Thread.currentThread().threadId());
//            SecurityContextHolder.clearContext();
//            String jwt = getJwtFromRequest(request);
//            if (StringUtils.hasText(jwt)) {
//                UserDetails userDetails;
//                try {
//                    if (!tokenProvider.validateToken(jwt)) {
//                        throw new TokenRefreshException(jwt, "Invalid refresh token!");
//                    }
//
//                    Map<String, Object> getPropertiesFromClaims = tokenProvider.getPropertiesFromClaims(jwt);
//                    String type = getPropertiesFromClaims.get("type").toString();
//
//                    if (!type.equals("access_token")) {
//                        throw new TokenRefreshException(jwt, "Invalid access token!");
//                    }
//
//                    long userId = Long.parseLong(getPropertiesFromClaims.get("uid").toString());
//                    String jti = getPropertiesFromClaims.get("jti").toString();
//
//                    String key = MessageFormat.format("{0}_{1}_{2}",
//                            tokenBlacklistPrefix, userId, jti);
//
//                    if (redisUtils.hasKey(key)) {
//                        throw new TokenRefreshException(jwt, "Token is blacklisted!");
//                    }
//
//                    long created_at = Long.parseLong(getPropertiesFromClaims.get("created_at").toString());
//
//                    // Kiểm tra thời gian logout all lần cuối
//                    String keyIat = MessageFormat.format("{0}_{1}", tokenIatPrefix, userId);
//
//                    Instant iat = redisUtils.getFromRedis(keyIat, Instant.class);
//
//                    // lấy thời gian đó và so sánh với thời gian tạo token
//                    if (iat != null && created_at < iat.toEpochMilli()) {
//                        throw new TokenRefreshException(jwt, "Token is expired!");
//                    }
//
//                    String userName = tokenProvider.getUsernameFromJWT(jwt);
//
//                    userDetails = customUserDetailsService.loadUserByUsernameOnly(userName);
//
//                    if (!userDetails.isEnabled()) {
//                        setResponseApiReturn(response, "Username is banned", HttpStatus.FORBIDDEN);
//                        return;
//                    }
//
////                if (SecurityContextHolder.getContext().getAuthentication() == null) {
////                    setAuthentication(request, userDetails);
////                }
//
//                    // dùng cho async lẫn sync luôn
//                    SecurityContext context = SecurityContextHolder.createEmptyContext();
//                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
//                            userDetails,
//                            null,
//                            userDetails.getAuthorities()
//                    );
//
//                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
//                    context.setAuthentication(authToken);
//                    SecurityContextHolder.setContext(context);
//                } catch (Exception ex) {
//                    log.error("Could not set user authentication in security context", ex);
//                }
//            }
            SecurityContextHolder.clearContext();
            String userIdHeader = request.getHeader("X-User-Id");
            if (!StringUtils.hasText(userIdHeader)) {
                filterChain.doFilter(request, response);
                return;
            }
            // 2. Trích các thông tin người dùng từ header
            long userId = Long.parseLong(userIdHeader);
            String username   = request.getHeader("X-Username");
            String rolesHeader= request.getHeader("X-Authorities-Roles");
            String permsHeader= request.getHeader("X-Authorities-Permissions");
            boolean enabled   = Boolean.parseBoolean(request.getHeader("X-User-Enabled"));
            String firstName  = request.getHeader("X-User-FirstName");
            String lastName   = request.getHeader("X-User-LastName");
            String avatar     = request.getHeader("X-User-Avatar");
            String userCode   = request.getHeader("X-User-Code");
            boolean gender    = Boolean.parseBoolean(request.getHeader("X-User-Gender"));
            String email      = request.getHeader("X-User-Email");
            String phone      = request.getHeader("X-User-PhoneNumber");
            String address    = request.getHeader("X-User-Address");

            // 3. Khởi tạo authorities từ roles & permissions
            Collection<GrantedAuthority> authorities = new ArrayList<>();
            if (StringUtils.hasText(rolesHeader)) {
                for (String r : rolesHeader.split(",")) {
                    authorities.add(new SimpleGrantedAuthority(r.trim()));
                }
            }
            if (StringUtils.hasText(permsHeader)) {
                for (String p : permsHeader.split(",")) {
                    authorities.add(new SimpleGrantedAuthority(p.trim()));
                }
            }
            // 4. Tạo UserPrincipal (hoặc custom principal)
            UserPrincipal principal = UserPrincipal.builder()
                    .id(userId)
                    .userName(username)
                    .firstName(firstName)
                    .lastName(lastName)
                    .avatar(avatar)
                    .userCode(userCode)
                    .gender(gender)
                    .email(email)
                    .phoneNumber(phone)
                    .address(address)
                    .isEnabled(enabled)
                    .build();
            principal.setAuthorities(authorities);

            // 5. Tạo Authentication và set vào SecurityContext
            SecurityContext context = SecurityContextHolder.createEmptyContext();
            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                    principal,
                    null,
                    ((UserDetails) principal).getAuthorities()
            );

            authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            context.setAuthentication(authToken);
            SecurityContextHolder.setContext(context);

            filterChain.doFilter(request, response);
        } finally {
            // dùng cho async lẫn sync luôn, xóa để khỏi lẫn lộn với thread khác
            SecurityContextHolder.clearContext();
        }
    }

    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    private void setAuthentication(HttpServletRequest request, UserDetails userDetails) {
        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                userDetails,
                null,
                userDetails.getAuthorities()
        );
        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authToken);
    }

    private void setResponseApiReturn(HttpServletResponse response,
                                      String message,
                                      HttpStatus status) throws IOException {
        ApiResponse apiResponse = new ApiResponse(false, message);
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_UTF8_VALUE);
        new ObjectMapper().writeValue(response.getOutputStream(), apiResponse);
    }

//    private boolean byPassFilterIfThrows(HttpServletRequest request) throws ServletException {
//        return excludeIfThrows.stream()
//                .anyMatch(p -> pathMatcher.match(p, request.getServletPath()));
//    }
//
//    @Override
//    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
//        return excludeUrlPatterns.stream()
//                .anyMatch(p -> pathMatcher.match(p, request.getServletPath()));
//    }
}