package com.onemorethink.domadosever.global.security.jwt;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "jwt")
@Getter
@Setter
@Slf4j
public class JwtProperties {
    private String secret;

    @PostConstruct
    public void init() {
        log.info("JwtProperties initialized with secret: {}", secret != null ? "present" : "null");
        log.info("Access token validity: {}", accessToken.getValidity());
        log.info("Refresh token validity: {}", refreshToken.getValidity());
    }

    @NestedConfigurationProperty
    private TokenConfig accessToken = new TokenConfig();

    @NestedConfigurationProperty
    private TokenConfig refreshToken = new TokenConfig();

    @Getter @Setter
    public static class TokenConfig {
        private long validity;
    }
}
