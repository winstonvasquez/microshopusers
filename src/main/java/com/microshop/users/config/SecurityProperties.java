package com.microshop.users.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "application.security")
public class SecurityProperties {

    private final Jwt jwt = new Jwt();
    private final OAuth2 oauth2 = new OAuth2();

    public Jwt getJwt() { return jwt; }
    public OAuth2 getOauth2() { return oauth2; }

    public static class Jwt {
        private String publicKey;
        public String getPublicKey() { return publicKey; }
        public void setPublicKey(String publicKey) { this.publicKey = publicKey; }
    }

    public static class OAuth2 {
        private final Google google = new Google();
        public Google getGoogle() { return google; }

        public static class Google {
            private String clientId;
            public String getClientId() { return clientId; }
            public void setClientId(String clientId) { this.clientId = clientId; }
        }
    }
}
