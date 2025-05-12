package com.example.quizcards.service;


import com.example.quizcards.dto.request.email.SenderTemplateEmailRequest;
import com.example.quizcards.dto.response.email.EmailResponse;


public interface IEmailService {
    void sendEmail(String to, String subject, String content);

    EmailResponse sendEmailToBrevo(SenderTemplateEmailRequest request);
}