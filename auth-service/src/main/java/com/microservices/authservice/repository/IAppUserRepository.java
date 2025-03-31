package com.microservices.authservice.repository;

import com.microservices.authservice.entities.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface IAppUserRepository extends JpaRepository<AppUser, Long> {
    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    boolean existsByUserCode(String userCode);

    Optional<AppUser> findByUsername(String username);

    Optional<AppUser> findByEmail(String email);

    Optional<AppUser> findByUserCode(String userCode);

    Optional<AppUser> findByUsernameOrEmail(String username, String email);
}
