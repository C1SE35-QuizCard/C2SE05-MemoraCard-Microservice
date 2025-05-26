package com.microservices.trollingservice.controller;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/nig")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class TrollingController {
    @GetMapping("/troll")
    public Mono<ResponseEntity<?>> troll() {
        return Mono.just(ResponseEntity.ok("Troll successful!"));
    }

    @GetMapping("/troll2")
    @PreAuthorize("hasAnyRole('ROLE_FREE_USER', 'ROLE_PREMIUM_USER', 'ROLE_ADMIN')")
    public Mono<ResponseEntity<?>> troll2() {
        return Mono.just(ResponseEntity.ok("Troll successful!"));
    }
}
