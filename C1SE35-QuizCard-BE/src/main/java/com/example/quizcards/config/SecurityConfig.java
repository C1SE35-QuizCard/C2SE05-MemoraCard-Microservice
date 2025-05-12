package com.example.quizcards.config;

import com.example.quizcards.security.JwtAuthenticationFilter;
import com.example.quizcards.service.impl.CustomUserDetailsServiceImpl;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.context.DelegatingSecurityContextRepository;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.RequestAttributeSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    private final CustomUserDetailsServiceImpl userDetailsService;

    private final SecurityContextRepository securityContextRepository;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter,
                          CustomUserDetailsServiceImpl userDetailsService,
                          @Lazy SecurityContextRepository securityContextRepository) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.userDetailsService = userDetailsService;
        this.securityContextRepository = securityContextRepository;
    }

//    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter,
//                          CustomUserDetailsServiceImpl userDetailsService) {
//        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
//        this.userDetailsService = userDetailsService;
//    }


    // Mảng chứa các endpoint cần xác thực
    String[] authEndpoints = {
            "/v1/auth/user-info",
            "/v1/auth/logout",
            "/v1/auth/update-password",
            "/v1/home/data",
            "/api/v1/home/data",
            "/v1/home/admin",
            "/v1/set/count-set",
            "/v1/set/count-set-in-current-date",
            "/v1/set/create-new-set",
            "/v1/set/update-set",
            "/v1/set/delete-set/",
            "/v1/flashcards/create-new-flashcard",
            "/v1/flashcards/update-flashcard",
            "/v1/flashcards/delete-flashcard",
            "/v1/folder/create-new-folder",
            "/v1/folder/update-folder",
            "/v1/folder/delete-folder",
            "/v1/folder/user",
            "/v1/collection/create-new-collection",
            "/v1/collection/delete-collection",
            "/v1/deadline/create-deadline",
            "/v1/deadline/update-deadline",
            "/v1/deadline/delete-deadline/",
            "/v1/category-subscription/current-benefit",
            "/v1/category-subscription/current-subscription",
            "/v1/flashcard-settings/update",
            "/v1/flashcard-settings/",
            "/v1/flashcard-settings/sort",
            "/v1/progress/user/set/",
            "/v1/progress/user/analysis/set/",
            "/v1/progress/user/assign-progress",
            "/v1/progress/user/reset-progress/",
            "/v1/users/**",
            "/v1/category/create",
            "/v1/category/update",
            "/v1/category/delete",
            "/v1/notification/**",
            "/v1/streak-learning/**",
            "/v1/srs-progress/**",
            "/v1/setting-progress/**"
    };

    // Mảng chứa các endpoint cho phép truy cập công khai
    String[] permitAllEndpoints = {
            "/auth/forgot-password",
            "/v1/ka",
            "/v1/ka/**",
//            "/ws/**"
    };

    String[] permitWsEndpoints = {
            "/ws",
            "/ws/**"
    };

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)  // Sử dụng phương pháp mới để vô hiệu hóa CSRF
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(authEndpoints).authenticated()
                        .requestMatchers(permitAllEndpoints).permitAll()
                        .requestMatchers(permitWsEndpoints).permitAll()
                        .anyRequest().permitAll()
                )
                .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .securityContext(request -> request.securityContextRepository(securityContextRepository))
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .exceptionHandling(ex -> ex.authenticationEntryPoint((request, response, authException) -> {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType("application/json");
                    response.getWriter().write("{\"error\": \"Authentication required!\"}");
                    response.getWriter().flush();
                }));

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:3000", "https://localhost:3000"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }


    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration)
            throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public SecurityContextRepository securityContextRepository() {
        return new DelegatingSecurityContextRepository(
                new RequestAttributeSecurityContextRepository(),
                new HttpSessionSecurityContextRepository()
        );
    }
}