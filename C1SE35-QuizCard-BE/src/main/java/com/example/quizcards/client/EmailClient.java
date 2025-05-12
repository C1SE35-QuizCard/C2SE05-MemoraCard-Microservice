package com.example.quizcards.client;

import com.example.quizcards.dto.request.email.TemplateEmailRequest;
import com.example.quizcards.dto.response.email.EmailResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "email-client", url = "${spring.notification.email.brevo-url}")
public interface EmailClient {
    @PostMapping(value = "/v3/smtp/email", produces = MediaType.APPLICATION_JSON_VALUE)
    EmailResponse sendTemplateEmail(@RequestHeader("api-key") String apiKey, @RequestBody TemplateEmailRequest body);
}
