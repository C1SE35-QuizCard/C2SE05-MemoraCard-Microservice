package com.example.quizcards.controller;

import com.example.quizcards.dto.request.GoogleLoginRequest;
import com.example.quizcards.dto.request.LoginRequest;
import com.example.quizcards.dto.request.RefreshTokenRequest;
import com.example.quizcards.dto.request.SignupRequest;
import com.example.quizcards.dto.response.ApiResponse;
import com.example.quizcards.dto.response.JwtAuthenticationResponse;
import com.example.quizcards.dto.response.UserInfoResponse;
import com.example.quizcards.mapper.AppUserMapper;
import com.example.quizcards.security.UserPrincipal;
import com.example.quizcards.service.IAuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/v1/auth")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthController {
    IAuthService authService;

    AppUserMapper auMapper;

    @PostMapping("/signup")
    public ResponseEntity<JwtAuthenticationResponse> registerUser(
            @Valid @RequestBody SignupRequest signupRequest,
            HttpServletResponse response) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(authService.registerUser(signupRequest, response));
    }

    @PostMapping("/login")
    public ResponseEntity<JwtAuthenticationResponse> authenticateUser(
            @Valid @RequestBody LoginRequest loginRequest,
            HttpServletResponse response) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(authService.loginUser(loginRequest, response));
    }

    @PostMapping("/logout-old")
    @Deprecated
    public ResponseEntity<?> logoutUser_old(
            HttpServletRequest loginRequest,
            HttpServletResponse response) {
        authService.logoutUser_old(loginRequest, response);
        return ResponseEntity.ok(new ApiResponse(true, "User logout successfully"));
    }

    @PostMapping("/logout")
    @PreAuthorize("hasAnyRole('ROLE_FREE_USER', 'ROLE_PREMIUM_USER', 'ROLE_ADMIN')")
    public ResponseEntity<?> logoutUser(
            @RequestBody Map<String, Object> request,
            HttpServletRequest loginRequest,
            HttpServletResponse response) {
        authService.logoutUser(
                (String) request.get("access_token"),
                loginRequest,
                response);
        return ResponseEntity.ok(new ApiResponse(true, "User logout successfully"));
    }

    @PostMapping("/logout-all")
    @PreAuthorize("hasAnyRole('ROLE_FREE_USER', 'ROLE_PREMIUM_USER', 'ROLE_ADMIN')")
    public ResponseEntity<?> logoutAll(HttpServletRequest loginRequest, HttpServletResponse response) {
        authService.logoutAll(loginRequest, response);
        return ResponseEntity.ok(new ApiResponse(true, "User logout successfully"));
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<JwtAuthenticationResponse> getAccessToken(
            HttpServletRequest request,
            HttpServletResponse response) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(authService.getAccessToken(request, response));
    }

    @PostMapping("/oauth2-login")
    public ResponseEntity<JwtAuthenticationResponse> googleLogin(
            @Valid @RequestBody GoogleLoginRequest request, HttpServletResponse response) throws Exception {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(authService.googleLogin(request, response));
    }

    @GetMapping("/user-role")
    public ResponseEntity<?> getUserRole() {
        return ResponseEntity.status(200).body(authService.getUserRole());
    }

    @GetMapping(value = "/user-info", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole('ROLE_FREE_USER', 'ROLE_PREMIUM_USER', 'ROLE_ADMIN')")
    public ResponseEntity<?> getUserInfo() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserPrincipal up = (UserPrincipal) auth.getPrincipal();
        UserInfoResponse response = auMapper.toUserInfoResponse(up);
        response.setRole(up.getRolesBaseAuthorities());
        response.setHasPassword(StringUtils.hasText(up.getPassword()));
        return ResponseEntity.ok(response);
    }

//    @PostMapping("update-password")
//    @PreAuthorize("hasAnyRole('ROLE_FREE_USER', 'ROLE_PREMIUM_USER', 'ROLE_ADMIN')")
//    public ResponseEntity<?> updatePasswordUser(@Valid @RequestBody UpdatePasswordRequest updatePasswordRequest,
//                                                HttpServletResponse response) {
//        updatePasswordRequest.validate();
//        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//        UserPrincipal up = (UserPrincipal) authentication.getPrincipal();
//
//        return authService.updatePasswordUser(up.getId(), updatePasswordRequest, response);
//    }
}
