package com.microservices.demo.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
    private static final String[] POST_PUBLIC_ENDPOINTS = {

    };

    private static final String[] GET_PUBLIC_ENDPOINTS = {

    };

    private static final String[] PUT_PUBLIC_ENDPOINTS = {

    };

    private static final String[] DELETE_PUBLIC_ENDPOINTS = {

    };

    private final CustomJwtDecoder customJwtDecoder;

    private final CustomHeaderBearerTokenResolver customHeaderBearerTokenResolver;

    public SecurityConfig(CustomJwtDecoder customJwtDecoder,
                          CustomHeaderBearerTokenResolver customHeaderBearerTokenResolver) {
        this.customJwtDecoder = customJwtDecoder;
        this.customHeaderBearerTokenResolver = customHeaderBearerTokenResolver;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(request -> request
                .requestMatchers(HttpMethod.POST, POST_PUBLIC_ENDPOINTS).permitAll()
                .requestMatchers(HttpMethod.GET, GET_PUBLIC_ENDPOINTS).permitAll()
                .requestMatchers(HttpMethod.PUT, PUT_PUBLIC_ENDPOINTS).permitAll()
                .requestMatchers(HttpMethod.DELETE, DELETE_PUBLIC_ENDPOINTS).permitAll()
                .anyRequest()
                .authenticated());

        httpSecurity.oauth2ResourceServer(oauth2 -> oauth2
                .bearerTokenResolver(customHeaderBearerTokenResolver)
                .jwt(jwtConfigurer -> jwtConfigurer
                        .decoder(customJwtDecoder)
                        .jwtAuthenticationConverter(jwtAuthenticationConverter()))
                .authenticationEntryPoint(new JwtAuthenticationEntryPoint()));
        httpSecurity.csrf(AbstractHttpConfigurer::disable);
        httpSecurity.httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable);

        return httpSecurity.build();
    }

    @Bean
    JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
        jwtGrantedAuthoritiesConverter.setAuthorityPrefix("");

        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(jwtGrantedAuthoritiesConverter);

        return jwtAuthenticationConverter;
    }
}
