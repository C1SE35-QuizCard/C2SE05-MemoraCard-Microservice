package com.microservices.apigateway.exception;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Collections;

public class ErrorHandler {
    public static final ObjectMapper objectMapper = new ObjectMapper();

    public static Mono<Void> handleAuthenticationError(ServerHttpResponse response, String message, HttpStatus status) {
        response.setStatusCode(status);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        response.getHeaders().set(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, "X-Validate-Again");
        response.getHeaders().set("X-Validate-Again", "true");
        try {
            byte[] body = objectMapper.writeValueAsBytes(Collections.singletonMap("error", message));
            DataBuffer buffer = response.bufferFactory().wrap(body);
            return response.writeWith(Mono.just(buffer));
        } catch (JsonProcessingException e) {
            DataBuffer buffer = response.bufferFactory().wrap("{\"error\": \"Internal Server Error formatting auth error response\"}".getBytes(StandardCharsets.UTF_8));
            response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR); // Đảm bảo status code đúng
            return response.writeWith(Mono.just(buffer));
        }
    }

    public static Mono<Void> handleGlobalError(ServerHttpResponse response, String message, HttpStatus status) {
        response.setStatusCode(status);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        try {
            byte[] body = objectMapper.writeValueAsBytes(Collections.singletonMap("error", message));
            DataBuffer buffer = response.bufferFactory().wrap(body);
            return response.writeWith(Mono.just(buffer));
        } catch (JsonProcessingException e) {
            DataBuffer buffer = response.bufferFactory().wrap("{\"error\": \"Internal Server Error formatting response\"}".getBytes(StandardCharsets.UTF_8));
            response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
            return response.writeWith(Mono.just(buffer));
        }
    }
}
