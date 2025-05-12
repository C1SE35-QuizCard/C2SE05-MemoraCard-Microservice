package com.example.quizcards.service;

import com.example.quizcards.dto.request.ResetPasswordRequest;

public interface UserService {
    public interface AuthService {
        void sendVerificationCode(String email);
        void verifyCode(String email, String code);
        void resetPassword(String email, String newPassword);
    }
}

