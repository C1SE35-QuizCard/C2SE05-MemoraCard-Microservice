package com.microservices.gatewayservice.client;

import com.microservices.gatewayservice.dto.request.IntrospectRequest;
import com.microservices.gatewayservice.dto.response.IntrospectResponse;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.PostExchange;
import reactor.core.publisher.Mono;

public interface AuthClient {
    @PostExchange(value = "/introspect", contentType = MediaType.APPLICATION_JSON_VALUE)
    Mono<IntrospectResponse> introspect(@RequestBody IntrospectRequest request);
}
