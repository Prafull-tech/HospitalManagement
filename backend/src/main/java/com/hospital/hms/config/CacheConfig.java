package com.hospital.hms.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    @Profile("prod")
    @ConditionalOnProperty(name = "spring.data.redis.host")
    public CacheManager redisCacheManager(RedisConnectionFactory connectionFactory) {
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(5))
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(
                                new GenericJackson2JsonRedisSerializer()));

        Map<String, RedisCacheConfiguration> cacheConfigs = new HashMap<>();
        cacheConfigs.put("patients", defaultConfig.entryTtl(Duration.ofMinutes(5)));
        cacheConfigs.put("permissions", defaultConfig.entryTtl(Duration.ofMinutes(10)));
        cacheConfigs.put("bedAvailability", defaultConfig.entryTtl(Duration.ofSeconds(30)));
        cacheConfigs.put("systemConfig", defaultConfig.entryTtl(Duration.ofHours(1)));
        cacheConfigs.put("medicineMaster", defaultConfig.entryTtl(Duration.ofMinutes(15)));

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(cacheConfigs)
                .build();
    }

    @Bean
    @Profile("!prod")
    public CacheManager simpleCacheManager() {
        return new ConcurrentMapCacheManager(
                "patients", "permissions", "bedAvailability", "systemConfig", "medicineMaster");
    }
}
