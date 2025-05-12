package com.example.quizcards.service.impl;

import com.example.quizcards.entities.RefreshToken;
import com.example.quizcards.repository.IAppUserRepository;
import com.example.quizcards.repository.IRefreshTokenRepository;
import com.example.quizcards.security.JwtTokenProvider;
import com.example.quizcards.security.UserPrincipal;
import com.example.quizcards.service.IRefreshTokenService;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RefreshTokenServiceImpl implements IRefreshTokenService {
    @Value("${jwt.refreshTokenExpirationInSec}")
    @NonFinal
    Long refreshTokenDurationSec;

    IRefreshTokenRepository IRefreshTokenRepository;

    IAppUserRepository userRepository;

    @Override
    public Optional<RefreshToken> findByToken(String token) {
        return IRefreshTokenRepository.findByToken(token);
    }

    @Override
    @Transactional
    public RefreshToken createRefreshToken(Long userID) {
        RefreshToken newRefreshToken = new RefreshToken();

        newRefreshToken.setUser(userRepository.findById(userID).get());
        newRefreshToken.setExpiryDate(Instant.now().plusSeconds(refreshTokenDurationSec).atZone(ZoneId.systemDefault()).toLocalDateTime());
        newRefreshToken.setToken(UUID.randomUUID().toString());

        return IRefreshTokenRepository.save(newRefreshToken);
    }

    @Override
    public RefreshToken verifyExpiration(RefreshToken refreshToken) {
        if (refreshToken.getExpiryDate().compareTo(LocalDateTime.now()) < 0) {
            IRefreshTokenRepository.delete(refreshToken);
            return null;
        }
        return refreshToken;
    }

    @Override
    @Transactional
    public RefreshToken updateRefreshTokenWithCurrentExpiredDate(RefreshToken refreshToken) {
        refreshToken.setExpiryDate(
                Instant.now().plusSeconds(refreshTokenDurationSec).atZone(ZoneId.systemDefault()).toLocalDateTime()
        );
        return IRefreshTokenRepository.save(refreshToken);
    }

    @Override
    public void deleteByToken(String refreshToken) {
        IRefreshTokenRepository.deleteByToken(refreshToken);
    }
}
