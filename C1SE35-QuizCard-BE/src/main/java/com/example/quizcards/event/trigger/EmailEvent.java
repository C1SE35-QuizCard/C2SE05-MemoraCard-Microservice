package com.example.quizcards.event.trigger;

import com.example.quizcards.dto.request.email.SenderTemplateEmailRequest;
import com.example.quizcards.service.IEmailService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class EmailEvent {
    IEmailService emailService;

    @KafkaListener(topics = "otp-email-change-password-delivery", groupId = "group-consumer-email-notification")
    public void listenOtpEmailChangePasswordDelivery(SenderTemplateEmailRequest message) {
        log.info("Email confirm change password data: {}", message);
        emailService.sendEmailToBrevo(message);
    }

    @KafkaListener(topics = "email-delivery", groupId = "group-consumer-email-notification")
    public void listenEmailDelivery(SenderTemplateEmailRequest message) {
        log.info("Email sent: {}", message);
        emailService.sendEmailToBrevo(message);
    }
}
