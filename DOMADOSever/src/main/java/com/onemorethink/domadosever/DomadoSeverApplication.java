package com.onemorethink.domadosever;

import com.onemorethink.domadosever.global.security.jwt.JwtProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@ConfigurationPropertiesScan
@EnableConfigurationProperties(JwtProperties.class)
public class DomadoSeverApplication {

    public static void main(String[] args) {
        SpringApplication.run(DomadoSeverApplication.class, args);
    }

}
