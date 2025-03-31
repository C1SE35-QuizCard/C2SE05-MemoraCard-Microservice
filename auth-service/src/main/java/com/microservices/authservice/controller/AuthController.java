package com.microservices.authservice.controller;

import com.microservices.authservice.dto.request.AuthenticateRequest;
import com.microservices.authservice.dto.request.IntrospectRequest;
import com.microservices.authservice.dto.response.IntrospectResponse;
import com.microservices.authservice.dto.response.JwtAuthenticationResponse;
import com.microservices.authservice.service.AuthService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class AuthController {
    AuthService authService;

    @PostMapping("/login")
    JwtAuthenticationResponse authenticate(@RequestBody AuthenticateRequest request) {
        return authService.login(request);
    }

    @PostMapping("/introspect")
    IntrospectResponse authenticate(@RequestBody IntrospectRequest request) {
        return authService.introspect(request);
    }
}
