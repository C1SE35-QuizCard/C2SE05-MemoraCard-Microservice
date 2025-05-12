package com.example.quizcards.service.impl;

import com.example.quizcards.dto.GoogleInfoUser;
import com.example.quizcards.dto.request.GoogleLoginRequest;
import com.example.quizcards.dto.request.LoginRequest;
import com.example.quizcards.dto.request.RefreshTokenRequest;
import com.example.quizcards.dto.request.SignupRequest;
import com.example.quizcards.dto.response.JwtAuthenticationResponse;
import com.example.quizcards.entities.AppRole;
import com.example.quizcards.entities.AppUser;
import com.example.quizcards.entities.role.RoleName;
import com.example.quizcards.exception.ErrorsDataException;
import com.example.quizcards.exception.ResourceNotFoundException;
import com.example.quizcards.exception.TokenRefreshException;
import com.example.quizcards.repository.IAppUserRepository;
import com.example.quizcards.security.JwtTokenProvider;
import com.example.quizcards.security.UserPrincipal;
import com.example.quizcards.service.IAppRoleService;
import com.example.quizcards.service.IAuthService;
import com.example.quizcards.service.IGoogleHandleService;
import com.example.quizcards.service.IRefreshTokenService;
import com.example.quizcards.utils.CodeRandom;
import com.example.quizcards.utils.CookieUtils;
import com.example.quizcards.utils.RedisUtils;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.text.MessageFormat;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthServiceImpl implements IAuthService {
    @Value("TOKEN_BLACKLIST")
    @NonFinal
    String tokenBlacklistPrefix;

    @Value("TOKEN_IAT_AVAILABLE")
    @NonFinal
    String tokenIatPrefix;

    IGoogleHandleService googleHandleService;

    IAppRoleService appRoleService;

    IAppUserRepository appUserService;

    IRefreshTokenService rfService;

    PasswordEncoder passwordEncoder;

    AuthenticationManager authenticationManager;

    JwtTokenProvider jwtTokenProvider;

    CookieUtils cookieUtils;

    RedisUtils redisUtils;

    @Value("${jwt.jwtExpirationInSec}")
    @NonFinal
    private Long jwtExpirationInSec;

    @Value("${jwt.refreshTokenExpirationInSec}")
    @NonFinal
    Long refreshTokenDurationSec;

    @Override
    @Transactional
    public JwtAuthenticationResponse registerUser(SignupRequest signupRequest, HttpServletResponse response) {
        if (appUserService.existsByUsername(signupRequest.getUsername())) {
            throw new ErrorsDataException("Register failed", Map.of("username", "Username is already taken"),
                    HttpStatus.BAD_REQUEST);
        }
        if (appUserService.existsByEmail(signupRequest.getEmail())) {
            throw new ErrorsDataException("Register failed", Map.of("email", "Email is already registered"),
                    HttpStatus.BAD_REQUEST);
        }
        AppRole role = appRoleService.findByRoleName(RoleName.ROLE_FREE_USER.name())
                .orElseThrow(() -> new ResourceNotFoundException("Role", "name", "Free user"));

        AppUser user = new AppUser();
        user.setFirstName(signupRequest.getFirstName());
        user.setLastName(signupRequest.getLastName());
        user.setDateOfBirth(signupRequest.getDateOfBirth());
        user.setUsername(signupRequest.getUsername());
        user.setEmail(signupRequest.getEmail());
        user.setHashPassword(passwordEncoder.encode(signupRequest.getPassword()));
        user.setUserCode("USER-DATA-" + CodeRandom.generateRandomCode(20));
        user.setRole(role);
        user.setGender(true);
        user.setEnabled(true);

        appUserService.save(user);

        UserPrincipal up = UserPrincipal.create(user);

        String accessToken = jwtTokenProvider.generateAccessToken(up);

        String refreshToken = jwtTokenProvider.generateRefreshToken(up);

        return cookieUtils.generateTokenToCookie(response, accessToken, refreshToken);

//        RefreshToken refreshToken = rfService.createRefreshToken(up.getId());

//        return new JwtAuthenticationResponse(accessToken, refreshToken.getToken(), "success");
    }

    @Override
    @Transactional
    public JwtAuthenticationResponse loginUser(LoginRequest loginRequest, HttpServletResponse response) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsernameOrEmail(), loginRequest.getPassword())
        );

        UserPrincipal up = (UserPrincipal) authentication.getPrincipal();

        String accessToken = jwtTokenProvider.generateAccessToken(up);

        String refreshToken = jwtTokenProvider.generateRefreshToken(up);

        return cookieUtils.generateTokenToCookie(response, accessToken, refreshToken);

//        RefreshToken refreshToken = rfService.createRefreshToken(up.getId());

//        return new JwtAuthenticationResponse(accessToken, refreshToken.getToken(), "success");
    }

    @Override
    @Transactional
    public void logoutUser_old(HttpServletRequest request, HttpServletResponse response) {
        String rft = null;
        Cookie refreshTokenCookie = cookieUtils.findCookie(request.getCookies(), "refresh_token");

        if (refreshTokenCookie != null) {
            rft = refreshTokenCookie.getValue();
        }

        if (rft != null) {
            cookieUtils.removeTokenInClient(response);

            rfService.deleteByToken(rft);
        }
    }

    @Override
    @Transactional
    public void logoutUser(String accessToken,
                           HttpServletRequest request,
                           HttpServletResponse response) {
        String refreshToken = "";
        Cookie rft_cookie = cookieUtils.findCookie(request.getCookies(), "refresh_token");
        if (rft_cookie != null && rft_cookie.getValue() != null) {
            refreshToken = rft_cookie.getValue();
        }
        UserPrincipal user = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long currentUserId = user.getId();

        Map<String, Object> refreshClaims = safelyExtractClaims(refreshToken, "refresh_token");
        Map<String, Object> accessClaims = safelyExtractClaims(accessToken, "access_token");

        Long refreshUserId = refreshClaims != null ? Long.valueOf(refreshClaims.get("uid").toString()) : null;
        Long accessUserId = accessClaims != null ? Long.valueOf(accessClaims.get("uid").toString()) : null;

        if (refreshUserId != null && accessUserId != null && !refreshUserId.equals(accessUserId)) {
            throw new JwtException("Invalid token paired");
        }

        if ((refreshUserId != null && !currentUserId.equals(refreshUserId)) ||
                (accessUserId != null && !currentUserId.equals(accessUserId))) {
            throw new JwtException("You don't have permission to logout this token");
        }

        blacklistToken(refreshClaims, currentUserId, refreshTokenDurationSec);
        blacklistToken(accessClaims, currentUserId, jwtExpirationInSec);

        cookieUtils.removeTokenInClient(response);
    }

    @Override
    @Transactional
    public void logoutAll(HttpServletRequest request,
                          HttpServletResponse response) {
        UserPrincipal user = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long currentUserId = user.getId();

        redisUtils.saveToRedis(MessageFormat.format("{0}_{1}", tokenIatPrefix, currentUserId), Instant.now(),
                30, TimeUnit.DAYS); // Tạm lưu 30 ngày

        cookieUtils.removeTokenInClient(response);
    }


    private Map<String, Object> safelyExtractClaims(String token, String expectedType) {
        try {
            Map<String, Object> claims = jwtTokenProvider.getPropertiesFromClaims(token);
            if (expectedType.equals(claims.get("type"))) {
                return claims;
            } else {
                throw new JwtException("Invalid token type: expected " + expectedType);
            }
        } catch (Exception e) {
            log.error("Error extracting {} token: {}", expectedType, e.getMessage());
            return null;
        }
    }

    private void blacklistToken(Map<String, Object> claims, Long userId, long duration) {
        if (claims != null && claims.containsKey("jti")) {
            String jti = claims.get("jti").toString();
            if (StringUtils.hasText(jti)) {
                String key = MessageFormat.format("{0}_{1}_{2}", tokenBlacklistPrefix, userId, jti);
                redisUtils.saveToRedis(key, Instant.now(), duration, TimeUnit.SECONDS);
            }
        }
    }

    @Override
    public String getUserRole() {
        String role = "ROLE_GUEST";
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && authentication.getPrincipal() != null
                && authentication.getPrincipal() instanceof UserPrincipal) {
            String username = authentication.getName();
            Optional<AppUser> user = appUserService.findByUsername(username);
            if (user.isPresent()) {
                role = user.get().getRole().getRoleName();
            }
        }
        return role;
    }

    @Override
    public boolean isFreeUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserPrincipal up = (UserPrincipal) authentication.getPrincipal();
        boolean isFreeUser = false;
        if (!up.getRolesBaseAuthorities().contains(RoleName.ROLE_ADMIN.name())
                && !up.getRolesBaseAuthorities().contains(RoleName.ROLE_PREMIUM_USER.name())) {
            if (up.getRolesBaseAuthorities().contains(RoleName.ROLE_FREE_USER.name())) {
                isFreeUser = true;
            } else {
                throw new RuntimeException("Invalid role");
            }
        }
        return isFreeUser;
    }

    @Override
    @Transactional
    public JwtAuthenticationResponse getAccessToken_old(RefreshTokenRequest request,
                                                        HttpServletResponse response) {
        String rft = request.getRefreshToken();
        return rfService.findByToken(rft)
                .map(rfService::verifyExpiration)
                .map(rfService::updateRefreshTokenWithCurrentExpiredDate)
                .flatMap(refreshToken -> {
                    AppUser user = refreshToken.getUser();
                    UserPrincipal up = UserPrincipal.create(user);
                    String token = jwtTokenProvider.generateAccessToken(up);

                    JwtAuthenticationResponse jwtResponse = cookieUtils.generateTokenToCookie(response,
                            token,
                            refreshToken.getToken());

                    return Optional.of(jwtResponse);
                })
                .orElseThrow(() -> new TokenRefreshException(rft, "Invalid refresh token!"));
    }

    @Override
    @Transactional
    public JwtAuthenticationResponse getAccessToken(HttpServletRequest request,
                                                    HttpServletResponse response) {
        Cookie rft_cookie = cookieUtils.findCookie(request.getCookies(), "refresh_token");
        try {
            if (rft_cookie == null || rft_cookie.getValue() == null) {
                throw new TokenRefreshException(null, "Invalid refresh token!");
            }
            String rft = rft_cookie.getValue();
            // validate refresh token
            if (!jwtTokenProvider.validateToken(rft)) {
                throw new TokenRefreshException(rft, "Invalid refresh token!");
            }
            // lấy các thuộc tính claims từ refresh token
            Map<String, Object> getPropertiesFromClaims = jwtTokenProvider.getPropertiesFromClaims(rft);

            // lấy type và kiểm tra xem nó có phải là refresh token hay không
            String type = getPropertiesFromClaims.get("type").toString();
            if (!type.equals("refresh_token")) {
                throw new TokenRefreshException(rft, "Invalid refresh token!");
            }

            // lấy uid và jti từ refresh token
            Long userId = Long.parseLong(getPropertiesFromClaims.get("uid").toString());
            String jti = getPropertiesFromClaims.get("jti").toString();
            long created_at = Long.parseLong(getPropertiesFromClaims.get("created_at").toString());

            String key = MessageFormat.format("{0}_{1}_{2}", tokenBlacklistPrefix, userId, jti);
            // kiểm tra xem refresh token có trong blacklist hay không
            if (redisUtils.hasKey(key)) {
                throw new TokenRefreshException(rft, "Token is blacklisted!");
            }

            // Kiểm tra thời gian logout all lần cuối
            String keyIat = MessageFormat.format("{0}_{1}", tokenIatPrefix, userId);

            Instant iat = redisUtils.getFromRedis(keyIat, Instant.class);

            // lấy thời gian đó và so sánh với thời gian tạo token
            if (iat != null && created_at < iat.toEpochMilli()) {
                throw new TokenRefreshException(rft, "Token is expired!");
            }

            // lấy user từ id
            AppUser au = appUserService.findById(userId).orElseThrow();

            // tạo access token mới từ user principal
            String newAccessToken = jwtTokenProvider.generateAccessToken(UserPrincipal.create(au));

            // tạo refresh token mới từ user principal
            String newRefreshToken = jwtTokenProvider.generateRefreshToken(UserPrincipal.create(au));

            // đưa refresh token cũ từ request vào redis để nó thành danh sách đen
            redisUtils.saveToRedis(key, Instant.now(), refreshTokenDurationSec, TimeUnit.SECONDS);

            // trả về cặp access token - refresh token mới
            return cookieUtils.generateTokenToCookie(response, newAccessToken, newRefreshToken);
        } catch (TokenRefreshException tre) {
            throw tre;
        } catch (Exception e) {
            throw new RuntimeException("Error from server");
        }
    }

    private JwtAuthenticationResponse authenOAuth2Login(String email,
                                                        HttpServletResponse response) {
        AppUser user = appUserService.findByEmail(email).get();

        if (!user.getEnabled()) {
            throw new DisabledException("User is disabled");
        }

        UserPrincipal up = UserPrincipal.create(user);

        String accessToken = jwtTokenProvider.generateAccessToken(up);

        String refreshToken = jwtTokenProvider.generateRefreshToken(up);

        return cookieUtils.generateTokenToCookie(response, accessToken, refreshToken);

//        RefreshToken refreshToken = rfService.createRefreshToken(up.getId());
//
//        return cs.generateTokenToCookie(response, accessToken, refreshToken.getToken());
    }

    private JwtAuthenticationResponse registerByGoogleInfo(GoogleInfoUser g_user,
                                                           HttpServletResponse response) {
        AppRole role = appRoleService.findByRoleName(RoleName.ROLE_FREE_USER.name())
                .orElseThrow(() -> new ResourceNotFoundException("Role", "name", "Free user"));

        AppUser user = new AppUser();
        user.setFirstName(g_user.getFirstName());
        user.setLastName(g_user.getLastName());
        user.setAvatar(g_user.getAvatarUrl());
        user.setEmail(g_user.getEmail());
        user.setHashPassword("");
        user.setRole(role);
        user.setGender(true);
        user.setEnabled(g_user.getEnabled());

        while (appUserService.existsByUsername(g_user.getUserName())) {
            g_user.setUserName(g_user.getUserName() + "-" + CodeRandom.generateRandomCode(10));
        }

        user.setUsername(g_user.getUserName());
        user.setUserCode("USER-DATA-" + CodeRandom.generateRandomCode(20));

        appUserService.save(user);

        UserPrincipal up = UserPrincipal.create(user);

        String accessToken = jwtTokenProvider.generateAccessToken(up);

        String refreshToken = jwtTokenProvider.generateRefreshToken(up);

        return cookieUtils.generateTokenToCookie(response, accessToken, refreshToken);

//        RefreshToken refreshToken = rfService.createRefreshToken(up.getId());
//
//        return cs.generateTokenToCookie(response, accessToken, refreshToken.getToken());
    }

    @Override
    @Transactional
    public JwtAuthenticationResponse googleLogin(GoogleLoginRequest request, HttpServletResponse response)
            throws Exception {
        GoogleInfoUser user = googleHandleService.extractDataFromCode(request);
        if (appUserService.existsByEmail(user.getEmail())) {
            return authenOAuth2Login(user.getEmail(), response);
        } else {
            return registerByGoogleInfo(user, response);
        }
    }

//    @Override
//    @Transactional
//    public void updatePasswordUser(Long id, UpdatePasswordRequest updatePasswordRequest, HttpServletResponse response) {
//        AppUser user = appUserService.findById(id).orElseThrow();
//
//        if ((user.getHashPassword() != null && !user.getHashPassword().isEmpty()) &&
//                !passwordEncoder.matches(updatePasswordRequest.getOldPassword(), user.getHashPassword())) {
//            throw new UnauthorizedException("Wrong old password!");
//        }
//
//        user.setHashPassword(passwordEncoder.encode(updatePasswordRequest.getNewPassword()));
//
//        appUserService.save(user);
//    }
}
