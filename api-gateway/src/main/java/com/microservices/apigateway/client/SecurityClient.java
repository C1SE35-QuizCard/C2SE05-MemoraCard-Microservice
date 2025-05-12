package com.microservices.apigateway.client;

import com.microservices.dto.request.ValidateRequest;
import com.microservices.security.model.UserInfo;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.service.annotation.PostExchange;
import reactor.core.publisher.Mono;

public interface SecurityClient {
//    @PostExchange(url = "/internal/validate-token", contentType = MediaType.APPLICATION_JSON_VALUE)
//    NO CODE: Mono<ResponseEntity<UserInfo>> validateToken(ValidateRequest token);

    @PostExchange(
            url         = "/internal/validate-token",
            contentType = MediaType.APPLICATION_JSON_VALUE,
            accept      = MediaType.APPLICATION_JSON_VALUE
    )
    Mono<ResponseEntity<UserInfo>> validateToken(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization
    );
}
