package com.scene.mesh.mcp.server.config;

import com.scene.mesh.foundation.impl.cache.RedisCache;
import com.scene.mesh.foundation.spec.api.ApiClient;
import com.scene.mesh.foundation.spec.cache.ICache;
import com.scene.mesh.service.impl.action.DefaultMetaActionService;
import com.scene.mesh.service.spec.action.IMetaActionService;
import com.scene.mesh.service.spec.cache.MutableCacheService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ServiceConfig {

    @Value("${sm.redis.host}")
    private String redisHost;

    @Value("${sm.redis.port}")
    private int redisPort;

    @Bean
    public RedisCache iCache() {
        return new RedisCache(redisHost, redisPort);
    }

    @Bean
    public MutableCacheService mutableCache(ICache iCache) {
        return new MutableCacheService(iCache,null);
    }

    @Bean
    public IMetaActionService metaActionService(MutableCacheService mutableCacheService) {
        return new DefaultMetaActionService(mutableCacheService);
    }

}
