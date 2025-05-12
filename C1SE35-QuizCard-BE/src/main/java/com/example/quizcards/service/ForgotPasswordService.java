package com.example.quizcards.service;

public interface ForgotPasswordService {
    void sendVerificationCode(String email);
    void verifyCode(String email, String code);
    void resetPassword(String email, String newPassword);
}
