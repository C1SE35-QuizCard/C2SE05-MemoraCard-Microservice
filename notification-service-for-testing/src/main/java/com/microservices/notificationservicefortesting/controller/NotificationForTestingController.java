package com.microservices.notificationservicefortesting.controller;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;

@RestController
@RequestMapping("/sse-testing")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class NotificationForTestingController {
    @GetMapping(value = "/test", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> test() {
        return ReactiveSecurityContextHolder.getContext()          // Mono<SecurityContext>
                .map(SecurityContext::getAuthentication)                 // Mono<Authentication>
                .filter(Authentication::isAuthenticated)           // lọc login
                .flatMapMany(auth ->                                // đã xác thực → gửi stream
                        Flux.interval(Duration.ZERO, Duration.ofMillis(2500))
                                .map(i -> ServerSentEvent.<String>builder()
                                        .event("test")
                                        .data("hello world ❤️")
                                        .build()))
                .switchIfEmpty(Flux.error(                          // chưa login → 401
                        new ResponseStatusException(HttpStatus.UNAUTHORIZED)));
    }

    @GetMapping(value = "/ping", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Mono<ResponseEntity<String>> ping() {
        return ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .filter(Authentication::isAuthenticated)
                .flatMap(auth -> Mono.just(ResponseEntity.ok("pong")))
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED)));
    }
}
