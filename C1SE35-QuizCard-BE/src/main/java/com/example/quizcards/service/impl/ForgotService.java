package com.example.quizcards.service.impl;

import com.example.quizcards.entities.AppUser;
import com.example.quizcards.repository.IAppUserRepository;
import com.example.quizcards.service.IEmailService;
import com.example.quizcards.service.ForgotPasswordService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


@Service
public class ForgotService implements ForgotPasswordService {
    @Autowired
    private IAppUserRepository appUserRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private IEmailService emailService;

    private final Map<String, String> verificationCodes = new HashMap<>();
    private final Map<String, LocalDateTime> codeExpiration = new HashMap<>();

    @Override
    public void sendVerificationCode(String email) {
        try {
            boolean result=appUserRepository.existsByEmail(email);
            if(!result){
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Email không tồn tại trong hệ thống.");
            }
            String verificationCode = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
            verificationCodes.put(email, verificationCode);
            codeExpiration.put(email, LocalDateTime.now().plusMinutes(10));
            emailService.sendEmail(email, "Verification Code",
                    "Your verification code is: " + verificationCode + "\nThis code is valid for 10 minutes.");
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error while sending verification code", e);
        }
    }

    @Override
    public void verifyCode(String email, String code) {
        if (!verificationCodes.containsKey(email)) {
            throw new IllegalArgumentException("No verification code found for this email.");
        }

        if (codeExpiration.get(email).isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Verification code expired.");
        }

        if (!verificationCodes.get(email).equals(code)) {
            throw new IllegalArgumentException("Invalid verification code.");
        }

        verificationCodes.remove(email);
        codeExpiration.remove(email);
    }

    @Override
    @Transactional
    public void resetPassword(String email, String newPassword) {
        AppUser user = appUserRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Email không tồn tại trong hệ thống."));

        // Hash the new password and update
        user.setHashPassword(passwordEncoder.encode(newPassword));
        appUserRepository.save(user);
    }
}
