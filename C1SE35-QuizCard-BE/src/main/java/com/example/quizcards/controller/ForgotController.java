package com.example.quizcards.controller;

import com.example.quizcards.service.impl.ForgotService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class ForgotController {
    @Autowired
    private ForgotService forgotService;

    @GetMapping("/fake")
    public ResponseEntity<String> fake() {
        return ResponseEntity.ok("Fake endpoint");
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@RequestBody Map<String, String> request) {
        forgotService.sendVerificationCode(request.get("email"));
        return ResponseEntity.ok("Verification code sent");
    }

    @PostMapping("/verify-code")
    public ResponseEntity<String> verifyCode(@RequestBody Map<String, String> request) {
        try {
            forgotService.verifyCode(request.get("email"), request.get("code"));
            return ResponseEntity.ok("Verification successful!");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@RequestBody Map<String, String> request) {
        forgotService.resetPassword(request.get("email"), request.get("newPassword"));
        return ResponseEntity.ok("Password reset successfully");
    }
}
