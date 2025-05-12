package com.example.quizcards.client;

import com.example.quizcards.dto.response.GoogleUserInfoResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "google-userinfo-client", url = "${spring.security.oauth2.client.provider.google.user-info-uri}")
public interface GoogleUserInfoClient {
    @GetMapping
    GoogleUserInfoResponse getUserInfo(@RequestParam("access_token") String accessToken);
}
