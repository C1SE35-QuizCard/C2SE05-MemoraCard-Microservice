package com.example.quizcards.client;

//import com.example.quizcards.config.FeignFormConfig;
import com.example.quizcards.dto.response.GoogleAccessTokenResponse;
import feign.Headers;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "google-oauth2-client", url = "${spring.security.oauth2.client.provider.google.token-uri}")
public interface GoogleOAuth2Client {
    @PostMapping(consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    @Headers("Content-Type: " + MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    GoogleAccessTokenResponse getAccessToken(@RequestParam("code") String code,
                                               @RequestParam("client_id") String clientId,
                                               @RequestParam("client_secret") String clientSecret,
                                               @RequestParam("redirect_uri") String redirectUri,
                                               @RequestParam("grant_type") String grantType);
}
