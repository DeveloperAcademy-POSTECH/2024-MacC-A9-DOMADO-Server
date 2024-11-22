package com.onemorethink.domadosever.global.websocket;

import com.onemorethink.domadosever.global.security.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketConfigurer {
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(websocketHandler(), "/ws/notifications")
                .addInterceptors(new WebSocketAuthInterceptor(jwtTokenProvider))
                .setAllowedOrigins("*");  // 실제 환경에서는 구체적인 도메인 지정
    }

    @Bean
    public WebSocketHandler websocketHandler() {
        return new RentalWebSocketHandler();
    }
}