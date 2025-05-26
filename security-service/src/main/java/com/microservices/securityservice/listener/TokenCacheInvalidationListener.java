package com.microservices.securityservice.listener;

import com.github.benmanes.caffeine.cache.Cache;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TokenCacheInvalidationListener implements MessageListener {
    CacheManager cacheManager;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        System.out.println("Message received: " + new String(message.getChannel()) + " - " + new String(message.getBody()));
        System.out.println("For cache invalidation: " + new String(pattern));
        String channel = new String(message.getChannel());  // e.g. "cache-invalidate:token_blacklist"
        String key     = new String(message.getBody());     // e.g. "token_blacklist_123_abcd"
        String cache   = channel.split(":", 2)[1];          // "token_blacklist" hoặc "token_iat"
        @SuppressWarnings("unchecked")
        Cache<Object, Object> c = (Cache<Object, Object>) cacheManager.getCache(cache).getNativeCache();
        System.out.println("Evicting cache: " + cache + " for key: " + key);
        c.invalidate(key);
    }
}
