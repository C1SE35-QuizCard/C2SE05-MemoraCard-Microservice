package com.example.quizcards.config;

import lombok.experimental.NonFinal;
import nl.martijndwars.webpush.PushService;
import nl.martijndwars.webpush.Utils;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.security.Security;

@Configuration
public class PushServiceConfig {
    @Value("${spring.web-push-notifications.public-key}")
    @NonFinal
    String push_publicKey;

    @Value("${spring.web-push-notifications.private-key}")
    @NonFinal
    String push_privateKey;

    @Bean
    public PushService pushService() {
        try {
            Security.addProvider(new BouncyCastleProvider());
            return new PushService()
                    .setPublicKey(Utils.loadPublicKey(push_publicKey))
                    .setPrivateKey(Utils.loadPrivateKey(push_privateKey))
                    //.setSubject("mailto:your-email@example.com");
                    .setSubject("sender from server");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
