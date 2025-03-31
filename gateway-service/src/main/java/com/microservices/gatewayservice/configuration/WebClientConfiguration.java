package com.microservices.gatewayservice.configuration;

import com.microservices.gatewayservice.client.AuthClient;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.loadbalancer.reactive.ReactorLoadBalancerExchangeFilterFunction;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.support.WebClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;


import java.text.MessageFormat;
import java.util.List;

@Configuration
@FieldDefaults(level = AccessLevel.PRIVATE)
public class WebClientConfiguration {
    @Value("${app.auth.protocol}")
    String authProtocol;

    @Value("${app.auth.context-path}")
    String authContextPath;

    @Bean
    public CorsWebFilter corsWebFilter() {
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        corsConfiguration.setAllowedOrigins(List.of("*"));
        corsConfiguration.setAllowedHeaders(List.of("*"));
        corsConfiguration.setAllowedMethods(List.of("*"));

        UrlBasedCorsConfigurationSource urlBasedCorsConfigurationSource = new UrlBasedCorsConfigurationSource();
        urlBasedCorsConfigurationSource.registerCorsConfiguration("/**", corsConfiguration);

        return new CorsWebFilter(urlBasedCorsConfigurationSource);
    }

    @Bean
    public WebClient webClient(ReactorLoadBalancerExchangeFilterFunction lbFunction) {
        return WebClient.builder()
                .filter(lbFunction)
                .build();
    }

    @Bean
    public AuthClient authClient(WebClient webClient) {
        String baseUrl = MessageFormat.format("{0}://auth-service/{1}", authProtocol, authContextPath);
        WebClient adjustedWebClient = webClient.mutate()
                .baseUrl(baseUrl)
                .build();
        HttpServiceProxyFactory httpServiceProxyFactory = HttpServiceProxyFactory
                .builderFor(WebClientAdapter.create(adjustedWebClient))
                .build();
        return httpServiceProxyFactory.createClient(AuthClient.class);
    }
}
