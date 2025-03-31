package com.microservices.gatewayservice.service;


import com.microservices.gatewayservice.client.AuthClient;
import com.microservices.gatewayservice.dto.request.IntrospectRequest;
import com.microservices.gatewayservice.dto.response.IntrospectResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthService {
    AuthClient identityClient;

    public Mono<IntrospectResponse> introspect(String token){
        return identityClient.introspect(IntrospectRequest.builder()
                        .token(token)
                .build());
    }
}
