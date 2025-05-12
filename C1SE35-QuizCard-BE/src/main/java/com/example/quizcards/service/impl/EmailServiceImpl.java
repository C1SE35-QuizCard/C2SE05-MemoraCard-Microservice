package com.example.quizcards.service.impl;

import com.example.quizcards.client.EmailClient;
import com.example.quizcards.dto.request.email.Sender;
import com.example.quizcards.dto.request.email.SenderTemplateEmailRequest;
import com.example.quizcards.dto.request.email.TemplateEmailRequest;
import com.example.quizcards.dto.response.email.EmailResponse;
import com.example.quizcards.service.IEmailService;
import feign.FeignException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class EmailServiceImpl implements IEmailService {
    JavaMailSender mailSender;

    @Value("${app.email.default-system-email}")
    @NonFinal
    String systemEmail;

    @Value("${app.email.default-system-name}")
    @NonFinal
    String systemName;

    @Value("${spring.notification.email.brevo-api-key}")
    @NonFinal
    String apiKey;

    EmailClient emailClient;

    @Override
    public void sendEmail(String to, String subject, String content) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(content);
        mailSender.send(message);
    }

    @Override
    public EmailResponse sendEmailToBrevo(SenderTemplateEmailRequest request) {
        TemplateEmailRequest templateEmailRequest = TemplateEmailRequest.builder()
                .sender(Sender.builder()
                        .name(systemName)
                        .email(systemEmail)
                        .build())
                .templateId(request.getTemplateId())
                .subject(request.getSubject())
                .params(request.getParams())
                .messageVersions(request.getMessageVersions())
                .build();
        try {
            return emailClient.sendTemplateEmail(apiKey, templateEmailRequest);
        } catch (FeignException e) {
            log.error("Error sending email: {}", e.getMessage());
            throw new RuntimeException("Cannot send email");
        }
    }
}