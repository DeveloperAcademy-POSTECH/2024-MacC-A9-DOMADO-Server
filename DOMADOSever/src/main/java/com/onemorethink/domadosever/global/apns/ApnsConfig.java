package com.onemorethink.domadosever.global.apns;


import com.eatthepath.pushy.apns.ApnsClient;
import com.eatthepath.pushy.apns.ApnsClientBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;
import java.io.IOException;

@Configuration
@Slf4j
public class ApnsConfig {
    @Value("${apns.certificate.path}")
    private String certificatePath;

    @Value("${apns.certificate.password}")
    private String certificatePassword;

    @Value("${apns.production}")
    private boolean production;

    @Bean
    public ApnsClient apnsClient() throws IOException {
        log.info("Initializing APNs client in {} mode", production ? "production" : "development");
        return new ApnsClientBuilder()
                .setApnsServer(production ?
                        ApnsClientBuilder.PRODUCTION_APNS_HOST :
                        ApnsClientBuilder.DEVELOPMENT_APNS_HOST)
                .setClientCredentials(new File(certificatePath), certificatePassword)
                .build();
    }
}