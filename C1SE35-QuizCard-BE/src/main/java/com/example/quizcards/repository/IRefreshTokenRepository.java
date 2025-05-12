package com.example.quizcards.repository;

import com.example.quizcards.entities.AppUser;
import com.example.quizcards.entities.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface IRefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByToken(String token);

    int deleteByUser(AppUser user);

    int deleteByToken(String token);
}
