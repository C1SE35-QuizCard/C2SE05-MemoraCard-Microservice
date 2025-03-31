package com.microservices.gatewayservice.configuration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microservices.gatewayservice.service.AuthService;
import com.microservices.gatewayservice.service.JwtTokenService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PACKAGE, makeFinal = true)
public class AuthenticationFilter implements GlobalFilter, Ordered {
    ObjectMapper objectMapper;
    AuthService authService;
    JwtTokenService tokenService;

    @NonFinal
    private String[] publicEndpoints = {
            "/auth/.*",
    };

    @Value("${app.api-prefix}")
    @NonFinal
    private String apiPrefix;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        log.info("Enter authentication filter....");

        if (isPublicEndpoint(exchange.getRequest()))
            return chain.filter(exchange);

        // Get token from authorization header
        List<String> authHeader = exchange.getRequest().getHeaders().get(HttpHeaders.AUTHORIZATION);
        if (CollectionUtils.isEmpty(authHeader))
            return unauthenticated(exchange.getResponse());

        String token = authHeader.getFirst().replace("Bearer ", "");
        log.info("Token: {}", token);

        return authService.introspect(token)
                .flatMap(introspectResponse -> {
                    if (introspectResponse.isValid()) {
                        String newToken = tokenService.generateToken(introspectResponse);

                        // Thêm header mới vào request
                        ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                                .header(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, "X-Security-Token")
                                .header("X-Security-Token", newToken)
                                .build();

                        return chain.filter(exchange.mutate().request(mutatedRequest).build());
                    } else
                        return unauthenticated(exchange.getResponse());
                }).onErrorResume(throwable -> {
                    String errorMessage = throwable.getMessage();
                    log.error("Error thrown: {}", errorMessage);
                    int statusCode = 500; // Giá trị mặc định nếu không parse được hoặc message null

                    if (errorMessage != null && !errorMessage.isEmpty()) {
                        String[] parts = errorMessage.split(" ", 2);

                        if (parts.length > 0) {
                            String potentialStatusCode = parts[0];
                            try {
                                // Parse phần đầu tiên thành số nguyên (int thường đủ cho mã HTTP)
                                statusCode = Integer.parseInt(potentialStatusCode);
                                log.info("Parsed status code from error message: {}", statusCode);
                            } catch (NumberFormatException e) {
                                // Log cảnh báo nếu phần đầu không phải là số
                                log.warn("Could not parse status code from beginning of message: '{}'. Full message: {}", potentialStatusCode, errorMessage);
                                // statusCode vẫn là -1
                            }
                        } else {
                            // Trường hợp message không chứa khoảng trắng (ít xảy ra với format bạn mô tả)
                            log.warn("Error message does not contain a space to split status code: {}", errorMessage);
                        }
                    } else {
                        log.warn("Throwable message was null or empty.");
                    }

                    if (statusCode == HttpStatus.UNAUTHORIZED.value()) {
                        return unauthenticated(exchange.getResponse());
                    }

                    if (statusCode == HttpStatus.SERVICE_UNAVAILABLE.value()) {
                        return serverHttpError(exchange.getResponse(), HttpStatus.SERVICE_UNAVAILABLE, "Service unavailable");
                    }

                    return serverHttpError(exchange.getResponse(), HttpStatus.valueOf(statusCode), "Error from server");
                });

//        return identityService.introspect(token).flatMap(introspectResponse -> {
//            if (introspectResponse.getResult().isValid())
//                return chain.filter(exchange);
//            else
//                return unauthenticated(exchange.getResponse());
//        }).onErrorResume(throwable -> unauthenticated(exchange.getResponse()));
    }

    @Override
    public int getOrder() {
        return -1;
    }

    private boolean isPublicEndpoint(ServerHttpRequest request) {
        return Arrays.stream(publicEndpoints)
                .anyMatch(s -> request.getURI().getPath().matches(apiPrefix + s));
    }

    Mono<Void> serverHttpError(ServerHttpResponse response, HttpStatus status, String message) {
        Map<String, Object> mapResponse = new HashMap<>();
        mapResponse.put("message", message);

        String body = null;
        try {
            body = objectMapper.writeValueAsString(mapResponse);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        response.setStatusCode(status);
        response.getHeaders().add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);

        return response.writeWith(
                Mono.just(response.bufferFactory().wrap(body.getBytes())));
    }

    Mono<Void> unauthenticated(ServerHttpResponse response) {
        Map<String, Object> mapResponse = new HashMap<>();
        mapResponse.put("message", "Unauthenticated");

        String body = null;
        try {
            body = objectMapper.writeValueAsString(mapResponse);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);

        return response.writeWith(
                Mono.just(response.bufferFactory().wrap(body.getBytes())));
    }
}
