package com.microservices.apigateway.configuration;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class LoadBalancerConfig {

    @Bean
    @LoadBalanced                            // <-- this is the magic
    public WebClient.Builder lbWebClientBuilder() {
        return WebClient.builder();
    }
}