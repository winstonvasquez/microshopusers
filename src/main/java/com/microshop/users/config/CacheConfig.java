package com.microshop.users.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public CacheManager cacheManager() {
        // "ubigeo" cachea los 3 niveles del ubigeo INEI: 1 entrada de departamentos + 25 de
        // provincias + 196 de distritos = 222 como mucho, holgado dentro del maximumSize. Es data
        // inmutable (la siembra la V42 y nada la escribe), así que el TTL solo sirve para no
        // retener memoria si nadie vuelve a pedirla.
        CaffeineCacheManager manager = new CaffeineCacheManager("erp-params", "themes", "ubigeo");
        manager.setCaffeine(Caffeine.newBuilder()
                .expireAfterWrite(5, TimeUnit.MINUTES)
                .maximumSize(500));
        return manager;
    }
}
