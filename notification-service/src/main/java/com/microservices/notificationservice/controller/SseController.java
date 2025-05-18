package com.microservices.notificationservice.controller;

import com.microservices.dto.response.StreakNotificationResponse;
import com.microservices.dto.security.UserPrincipal;
import com.microservices.notificationservice.dto.AckRequest;
import com.microservices.notificationservice.service.SseService;
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
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;

@RestController
@RequestMapping("/sse")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SseController {
    SseService sse;

    /* Stream SSE */
    @GetMapping(value = "/streak-stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<StreakNotificationResponse>> streakNotificationStream(
            @RequestHeader("X-Token-ID") String tokenId,
            @RequestHeader(value = "Last-Event-ID", required = false) String lastId) {
        System.out.println("tokenId = " + tokenId);
        return ReactiveSecurityContextHolder.getContext()          // Mono<SecurityContext>
                .map(SecurityContext::getAuthentication)                 // Mono<Authentication>
                .filter(Authentication::isAuthenticated)           // chỉ giữ authenticated
                .map(auth -> (UserPrincipal) auth.getPrincipal())  // Mono<UserPrincipal>
                .flatMapMany(principal ->                          // Mono → Flux
                        sse.subscribe(principal, lastId, tokenId))
                .switchIfEmpty(Flux.error(                         // chưa đăng nhập → 401
                        new ResponseStatusException(HttpStatus.UNAUTHORIZED)));
    }

    @GetMapping(value = "/test", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> test() {
        return ReactiveSecurityContextHolder.getContext()          // Mono<SecurityContext>
                .map(SecurityContext::getAuthentication)                 // Mono<Authentication>
                .filter(Authentication::isAuthenticated)           // lọc login
                .flatMapMany(auth ->                                // đã xác thực → gửi stream
                        Flux.interval(Duration.ZERO, Duration.ofSeconds(5))
                                .map(i -> ServerSentEvent.<String>builder()
                                        .event("test")
                                        .data("hello world ❤️")
                                        .build()))
                .switchIfEmpty(Flux.error(                          // chưa login → 401
                        new ResponseStatusException(HttpStatus.UNAUTHORIZED)));
    }

    /* Client ACK */
    @PostMapping("/ack")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> ack(@RequestBody AckRequest req) {
        return sse.ack(req.sagaId());      // gói trong service cho sạch controller
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
