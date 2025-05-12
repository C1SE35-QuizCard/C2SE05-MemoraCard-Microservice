package com.microservices.securityservice.service;

import org.springframework.security.core.userdetails.UserDetails;
import reactor.core.publisher.Mono;

public interface ICustomUserDetailsService {
    Mono<UserDetails> findByUsernameOnly(String username);
}
