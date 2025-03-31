package com.microservices.authservice.initializer;

import com.microservices.authservice.constant.PredefinedRole;
import com.microservices.authservice.entities.AppRole;
import com.microservices.authservice.entities.AppUser;
import com.microservices.authservice.repository.IAppRoleRepository;
import com.microservices.authservice.repository.IAppUserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
//@Import(AuthService.class)
public class AuthInitializer {
    @NonFinal
    static final String ADMIN_USER_NAME = "admin123";

    @NonFinal
    static final String ADMIN_PASSWORD = "StrongPassword123!";

    @NonFinal
    static final String ADMIN_EMAIL = "chichiquota@gmail.com";

    PasswordEncoder passwordEncoder;

    @Bean
    CommandLineRunner init(IAppRoleRepository roleRepository,
                           IAppUserRepository userRepository) {
        return args -> {
            if (!userRepository.existsByUsername(ADMIN_USER_NAME)) {
                roleRepository.save(AppRole.builder().roleName(PredefinedRole.FREE_USER_ROLE).build());
                roleRepository.save(AppRole.builder().roleName(PredefinedRole.PREMIUM_USER_ROLE).build());

                AppRole adminRole = roleRepository.save(AppRole.builder().roleName(PredefinedRole.ADMIN_ROLE).build());

                userRepository.save(AppUser.builder()
                        .username(ADMIN_USER_NAME)
                        .enabled(true)
                        .email(ADMIN_EMAIL)
                        .hashPassword(passwordEncoder.encode(ADMIN_PASSWORD))
                        .role(adminRole)
                        .build());
            }
        };
    }
}
