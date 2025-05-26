package com.microservices.progressservice.config;

import com.microservices.progressservice.client.SettingProgressReactiveClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.support.WebClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;


@Configuration
public class SetProgressSettingClientConfig {

    // This class is used to configure the ReactiveFeign client for setting progress.
    // It uses WebClient to make HTTP requests to the remote service.

    // The base URL for the remote service is injected from application properties.

    @Value("${app.set-progress-setting-base-url}")
    private String baseUrl;

    private final WebClient.Builder lbBuilder;

    public SetProgressSettingClientConfig(WebClient.Builder lbBuilder) {
        this.lbBuilder = lbBuilder;
    }

    @Bean
    public SettingProgressReactiveClient settingClient() {
        WebClient wc = lbBuilder
                .baseUrl(baseUrl)
                .build();

        return HttpServiceProxyFactory
                .builderFor(WebClientAdapter.create(wc))
                .build()
                .createClient(SettingProgressReactiveClient.class);
    }
}