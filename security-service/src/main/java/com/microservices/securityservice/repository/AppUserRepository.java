package com.microservices.securityservice.repository;

import com.microservices.securityservice.model.AppUser;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface AppUserRepository extends R2dbcRepository<AppUser, Long> {
    Mono<AppUser> findByUsername(String username);

    Mono<AppUser> findByEmail(String email);

    Mono<AppUser> findByUsernameOrEmail(String username, String email);
}
