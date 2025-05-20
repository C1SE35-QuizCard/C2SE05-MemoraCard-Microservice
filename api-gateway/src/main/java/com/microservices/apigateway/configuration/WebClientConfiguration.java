package com.microservices.apigateway.configuration;

import com.microservices.apigateway.client.SecurityClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.support.WebClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

import java.util.List;

@Configuration
public class WebClientConfiguration {
    @Value("${services.security_service_url}")
    String securityServiceUrl;

    private final WebClient.Builder lbBuilder;

    public WebClientConfiguration(WebClient.Builder lbBuilder) {
        this.lbBuilder = lbBuilder;
    }

    @Bean
    public SecurityClient securityClient() {
        WebClient client = lbBuilder
                .baseUrl(securityServiceUrl)   // SERVICE-ID trong Eureka
                .build();

        return HttpServiceProxyFactory
                .builderFor(WebClientAdapter.create(client))
                .build()
                .createClient(SecurityClient.class);
    }

//    @Bean
//    @Order(Integer.MAX_VALUE)
//    CorsWebFilter corsWebFilter() {
//        CorsConfiguration corsConfiguration = new CorsConfiguration();
//        corsConfiguration.setAllowedOrigins(List.of("*"));
//        corsConfiguration.setAllowedHeaders(List.of("*"));
//        corsConfiguration.setAllowedMethods(List.of("*"));
//        corsConfiguration.setAllowCredentials(true);
//
//        UrlBasedCorsConfigurationSource urlBasedCorsConfigurationSource = new UrlBasedCorsConfigurationSource();
//        urlBasedCorsConfigurationSource.registerCorsConfiguration("/**", corsConfiguration);
//
//        return new CorsWebFilter(urlBasedCorsConfigurationSource);
//    }
}
