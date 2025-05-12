package com.example.quizcards.service;

import com.example.quizcards.dto.request.*;
import com.example.quizcards.dto.response.JwtAuthenticationResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface IAuthService {
    JwtAuthenticationResponse registerUser(SignupRequest signupRequest, HttpServletResponse response);

    JwtAuthenticationResponse loginUser(LoginRequest loginRequest, HttpServletResponse response);

    JwtAuthenticationResponse googleLogin(GoogleLoginRequest googleLoginRequest, HttpServletResponse response)
            throws Exception;

    @Deprecated
    void logoutUser_old(HttpServletRequest request, HttpServletResponse response);

    void logoutUser(String accessToken, HttpServletRequest request, HttpServletResponse response);

    void logoutAll(HttpServletRequest request, HttpServletResponse response);

    String getUserRole();

    boolean isFreeUser();

    @Deprecated
    JwtAuthenticationResponse getAccessToken_old(RefreshTokenRequest request, HttpServletResponse response);

    JwtAuthenticationResponse getAccessToken(HttpServletRequest request, HttpServletResponse response);

    //    void updatePasswordUser(Long id, UpdatePasswordRequest updatePasswordRequest, HttpServletResponse response);
}
