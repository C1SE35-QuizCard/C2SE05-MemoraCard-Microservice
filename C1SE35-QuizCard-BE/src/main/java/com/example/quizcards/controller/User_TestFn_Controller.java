//package com.example.quizcards.controller;
//
//import com.example.quizcards.service.IUser_TestFn_Service;
//import lombok.RequiredArgsConstructor;
//import lombok.experimental.FieldDefaults;
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.access.prepost.PreAuthorize;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//
//@RestController
//@RequestMapping("/api/v1/user-test")
//@RequiredArgsConstructor
//@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
//public class User_TestFn_Controller {
//    IUser_TestFn_Service testFn_service;
//
//    @PostMapping("/send-otp")
//    @PreAuthorize("hasAnyRole('FREE_USER', 'PREMIUM_USER', 'ADMIN')")
//    public ResponseEntity<?> sendOtp() {
//        testFn_service.getNewOtpInUser();
//        return ResponseEntity.ok("Send otp successfully");
//    }
//}
