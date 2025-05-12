package com.example.quizcards.service;

import com.example.quizcards.entities.RefreshToken;
import com.example.quizcards.security.UserPrincipal;

import java.util.Optional;

public interface IRefreshTokenService {
    Optional<RefreshToken> findByToken(String token);

    RefreshToken createRefreshToken(Long userId);

    RefreshToken verifyExpiration(RefreshToken refreshToken);

    RefreshToken updateRefreshTokenWithCurrentExpiredDate(RefreshToken refreshToken);

    void deleteByToken(String refreshToken);
}
