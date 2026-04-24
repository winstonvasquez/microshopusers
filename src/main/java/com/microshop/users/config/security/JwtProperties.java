package com.microshop.users.config.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "application.security.jwt")
public record JwtProperties(
        long expiration,
        String privateKey,
        String publicKey) {
}
