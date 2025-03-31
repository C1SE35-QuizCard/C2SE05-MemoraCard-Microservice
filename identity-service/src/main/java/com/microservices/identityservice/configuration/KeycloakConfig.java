package com.microservices.identityservice.configuration;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class KeycloakConfig {
    @Value("${keycloak.admin.server-url}")
    @NonFinal
    String serverUrl;

    @Value("${keycloak.admin.realm}")
    @NonFinal
    String realm;

    @Value("${keycloak.admin.client-id}")
    @NonFinal
    String clientId;

    @Value("${keycloak.admin.username}")
    @NonFinal
    String username;

    @Value("${keycloak.admin.password}")
    @NonFinal
    String password;

    @Bean
    public Keycloak keycloak() {
        return KeycloakBuilder.builder()
                .serverUrl(serverUrl)
                .realm("master")
                .clientId(clientId)
                .username(username)
                .password(password)
                .build();
    }
}
