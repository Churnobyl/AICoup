package com.aicoup.app.config;

import com.aicoup.app.interceptor.websocket.WebsocketHandShakeInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;


/**
 * 웹소켓 관련 Config
 */
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
@Configuration
public class WebSocketConfiguration implements WebSocketMessageBrokerConfigurer {

    /**
     * 서버 송수신 uri 세팅
     * @param registry
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.setApplicationDestinationPrefixes("/pub");
        registry.enableSimpleBroker("/sub");
    }

    /**
     * 웹소켓 엔드포인트, 인터셉터 세팅
     * @param registry
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/game")
                .addInterceptors(new WebsocketHandShakeInterceptor())
                .setAllowedOrigins("*");
    }


}
