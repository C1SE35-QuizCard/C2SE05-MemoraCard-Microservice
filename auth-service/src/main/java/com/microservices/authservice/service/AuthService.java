package com.microservices.authservice.service;

import com.microservices.authservice.dto.request.AuthenticateRequest;
import com.microservices.authservice.dto.request.IntrospectRequest;
import com.microservices.authservice.dto.response.IntrospectResponse;
import com.microservices.authservice.dto.response.JwtAuthenticationResponse;
import com.microservices.authservice.entities.AppUser;
import com.microservices.authservice.repository.IAppRoleRepository;
import com.microservices.authservice.repository.IAppUserRepository;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jwt.SignedJWT;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j // ghi log
public class AuthService {
    IAppRoleRepository roleRepository;

    IAppUserRepository userRepository;

    PasswordEncoder passwordEncoder;

    JwtTokenService jwtTokenService;

    public IntrospectResponse introspect(IntrospectRequest request) {
        var token = request.getToken();
        boolean isValid = false;
        IntrospectResponse response = new IntrospectResponse();

        try {
            SignedJWT signedJWT = jwtTokenService.verifyToken(token);
            var claims = signedJWT.getJWTClaimsSet().getClaims();
            Long uid = Long.parseLong(claims.get("uid").toString());
            AppUser appUser = userRepository.findById(uid).
                    orElseThrow(() -> new RuntimeException("User not found"));
            response.setEmail(appUser.getEmail());
            response.setEnabled(appUser.getEnabled());
            response.setRoles(List.of(appUser.getRole().getRoleName()));
            response.setUserId(uid);
            response.setUsername(appUser.getUsername());
            isValid = true;
        } catch (JOSEException | ParseException | NumberFormatException e) {
            log.error(e.getMessage());
        }
        response.setValid(isValid);
        return response;
    }

    public JwtAuthenticationResponse login(AuthenticateRequest request) {
        AppUser au = userRepository.findByUsernameOrEmail(request.getUsernameOrEmail(), request.getUsernameOrEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(request.getPassword(), au.getHashPassword())) {
            throw new RuntimeException("Wrong password");
        }

        String token = jwtTokenService.generateToken(au, false);

        return JwtAuthenticationResponse.builder().
                accessToken(token)
                .build();
    }
}
