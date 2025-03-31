package com.microservices.authservice.service;

import com.microservices.authservice.entities.AppRole;
import com.microservices.authservice.entities.AppUser;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.StringJoiner;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class JwtTokenService {
    @Value("${jwt.authSignerKey}")
    @NonFinal
    String AUTH_KEY_SECRET;

    @NonFinal
    @Value("${jwt.valid-duration}")
    protected long VALID_DURATION;

    @NonFinal
    @Value("${jwt.refreshable-duration}")
    protected long REFRESHABLE_DURATION;

    public String generateToken(AppUser user, boolean isRefresh) {
        JWSHeader header = new JWSHeader(JWSAlgorithm.HS512);

        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                .subject(user.getUserId().toString())
                .issuer("memoracard.com")
                .issueTime(new Date())
                .expirationTime(new Date(
                        (isRefresh)
                                ? Instant.now().plus(REFRESHABLE_DURATION, ChronoUnit.SECONDS).toEpochMilli()
                                :
                                Instant.now().plus(VALID_DURATION, ChronoUnit.SECONDS).toEpochMilli()
                ))
                .jwtID(UUID.randomUUID().toString())
                .claim("type", isRefresh ? "refresh" : "access")
                .claim("scope", buildScope(user))
                .claim("uid", user.getUserId())
                .build();

        Payload payload = new Payload(jwtClaimsSet.toJSONObject());

        JWSObject jwsObject = new JWSObject(header, payload);

        try {
            jwsObject.sign(new MACSigner(AUTH_KEY_SECRET.getBytes()));
            return jwsObject.serialize();
        } catch (JOSEException e) {
            log.error("Cannot create token", e);
            throw new RuntimeException(e);
        }
    }

    public SignedJWT verifyToken(String token) throws JOSEException, ParseException {
        JWSVerifier verifier = new MACVerifier(AUTH_KEY_SECRET.getBytes());

        SignedJWT signedJWT = SignedJWT.parse(token);

        Date expiryTime = signedJWT.getJWTClaimsSet().getExpirationTime();

        var verified = signedJWT.verify(verifier);

        if (!(verified && expiryTime.after(new Date())))
            throw new JOSEException("Error: token expired");

//        if (invalidatedTokenRepository.existsById(signedJWT.getJWTClaimsSet().getJWTID()))
//            throw new AppException(ErrorCode.UNAUTHENTICATED);

        return signedJWT;
    }

    public String buildScope(AppUser user) {
        StringJoiner stringJoiner = new StringJoiner(" ");

        List<AppRole> roles = List.of(user.getRole());

        roles.forEach(role -> {
            stringJoiner.add(role.getRoleName());
        });

        // if (!CollectionUtils.isEmpty(role.getPermissions()))
        //     role.getPermissions().forEach(permission -> stringJoiner.add(permission.getName()));

        return stringJoiner.toString();
    }
}
