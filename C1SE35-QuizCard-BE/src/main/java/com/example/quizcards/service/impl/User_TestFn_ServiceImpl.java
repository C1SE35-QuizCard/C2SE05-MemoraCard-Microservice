//package com.example.quizcards.service.impl;
//
//import com.example.quizcards.dto.request.email.MessageVersion;
//import com.example.quizcards.dto.request.email.OtpParam;
//import com.example.quizcards.dto.request.email.Recipient;
//import com.example.quizcards.dto.request.email.SenderTemplateEmailRequest;
//import com.example.quizcards.security.UserPrincipal;
//import com.example.quizcards.service.IUser_TestFn_Service;
//import lombok.RequiredArgsConstructor;
//import lombok.experimental.FieldDefaults;
//import lombok.experimental.NonFinal;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.data.redis.core.RedisTemplate;
//import org.springframework.kafka.core.KafkaTemplate;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.stereotype.Service;
//
//import java.util.List;
//import java.util.Random;
//import java.util.concurrent.TimeUnit;
//
//@Service
//@RequiredArgsConstructor
//@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
//public class User_TestFn_ServiceImpl implements IUser_TestFn_Service {
//
//    KafkaTemplate<String, Object> kafkaTemplate;
//
//    RedisTemplate<String, String> redisTemplate;
//
//    @Value("OTP_CHANGE_PASSWORD_")
//    @NonFinal
//    String otpStringPrefix;
//
//    @Value("10")
//    @NonFinal
//    Integer maxOtpAttempt;
//
//    @Value("${spring.brevo.template.id-otp}")
//    @NonFinal
//    Integer idTemplateOtp;
//
//    @Override
//    public void getNewOtpInUser() {
////        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
////        UserPrincipal up = (UserPrincipal) auth.getPrincipal();
////        String userEmail = up.getEmail();
////        String userName = up.getUsername();
////        String otpCode = String.format("%06d", new Random().nextInt(1000000));
////        redisTemplate.opsForValue().set(otpStringPrefix + userName, otpCode, maxOtpAttempt, TimeUnit.MINUTES);
////        SenderTemplateEmailRequest emailRequest = SenderTemplateEmailRequest.builder()
////                .templateId(idTemplateOtp)
////                .messageVersions(List.of(
////                        MessageVersion.builder()
////                                .to(List.of(
////                                        Recipient.builder()
////                                                .email(userEmail)
////                                                .name(up.getFirstName() + up.getLastName())
////                                                .build()
////                                ))
////                                .params(
////                                        OtpParam.builder()
////                                                .yourOtpCode(otpCode)
////                                                .emailAddress(userEmail)
////                                                .username(userName)
////                                                .build()
////                                )
////                                .build())
////                )
////                .build();
////        kafkaTemplate.send("otp-email-change-password-delievery", emailRequest);
//    }
//}
