package com.microservices.setprogresssettingservice.service;

import com.microservices.dto.security.UserPrincipal;
import com.microservices.setprogresssettingservice.model.SetFlashcard;
import com.microservices.setprogresssettingservice.repository.ISetRepository;
import com.microservices.utils.ReactiveRedisUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SetServiceImpl {
    ISetRepository setRepo;
    ReactiveRedisUtils reactiveRedisUtils;
    PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public Mono<Void> checkAccess(
            UserPrincipal up,
            Long setId,
            String rawPwd,
            String rawValidAt
    ) {
        String userId = up.getId().toString();
        String key = String.format("set:%d:pass_checked:user:%s", setId, userId);

        // 1) Nếu đã có trong Redis set → bypass
        return reactiveRedisUtils
                .hasKey(key)                                                       // :contentReference[oaicite:0]{index=0}
                .flatMap(isMember -> {
                    if (Boolean.TRUE.equals(isMember)) {
                        return Mono.empty();
                    }
                    // 2) Fetch từ DB
                    return setRepo.findById(setId)
                            .switchIfEmpty(Mono.error(new ResponseStatusException(
                                    HttpStatus.NOT_FOUND, "Set not found: " + setId
                            )))
                            .flatMap(set -> {
                                // 3) nếu unprotected hoặc owner/admin → bypass
                                if (isUnprotected(set) || isOwner(up, set)) {
                                    return Mono.empty();
                                }
                                // 4) xác thực password
                                if (rawPwd == null || rawPwd.isBlank()
                                        || !passwordEncoder.matches(rawPwd, set.getHashPassword())) {
                                    return Mono.error(new ResponseStatusException(
                                            HttpStatus.FORBIDDEN, "{\"type\":\"Password\"}"
                                    ));
                                }
                                // 5) parse TTL và lưu vào Redis set
                                long ttl = parseValidAt(rawValidAt);
                                return reactiveRedisUtils
                                        .saveToSet(key, userId, ttl, TimeUnit.SECONDS)           // :contentReference[oaicite:1]{index=1}
                                        .then();
                            });
                });
    }

    private boolean isUnprotected(SetFlashcard set) {
        String hash = set.getHashPassword();
        return hash == null || hash.isBlank();
    }

    private boolean isOwner(UserPrincipal up, SetFlashcard set) {
        boolean isAdmin = up.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        return isAdmin || up.getId().equals(set.getAppUserId());
    }

    private long parseValidAt(String raw) {
        long max = 7 * 24 * 3600L;
        if (raw == null) return max;
        try {
            long v = Long.parseLong(raw);
            if (v < 0 || v > max) throw new NumberFormatException();
            return v;
        } catch (NumberFormatException ex) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "{\"type\":\"Valid at\",\"reason\":\"Invalid range\"}"
            );
        }
    }
}
