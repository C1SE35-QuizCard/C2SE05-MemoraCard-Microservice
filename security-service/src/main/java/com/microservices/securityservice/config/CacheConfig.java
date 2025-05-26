package com.microservices.securityservice.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Configuration
@EnableCaching
public class CacheConfig {
    @Bean
    public CacheManager cacheManager() {
        SimpleCacheManager mgr = new SimpleCacheManager();
//        CaffeineCache tokenMeta = new CaffeineCache(
//                "tokenMeta",
//                Caffeine.newBuilder()
//                        .expireAfterWrite(1, TimeUnit.MINUTES)
//                        .maximumSize(10_000)
//                        .build()
//        );
        CaffeineCache tokenBlacklistMeta = new CaffeineCache(
                "token_blacklist",
                Caffeine.newBuilder()
                        .expireAfterWrite(1, TimeUnit.MINUTES)
                        .maximumSize(10_000)
                        .build()
        );
        CaffeineCache tokenIatMeta = new CaffeineCache(
                "token_iat",
                Caffeine.newBuilder()
                        .expireAfterWrite(1, TimeUnit.MINUTES)
                        .maximumSize(10_000)
                        .build()
        );
        mgr.setCaches(List.of(tokenBlacklistMeta, tokenIatMeta));
        return mgr;
    }
}
