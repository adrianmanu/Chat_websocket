package com.chat.socket.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfiguration implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic");
        registry.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/chat-socket")
                .setAllowedOriginPatterns("http://10.1.144.*", "http://10.1.145.*", "http://10.1.146.*", "http://10.1.147.*",
                        "http://10.1.148.*", "http://10.1.149.*", "http://10.1.150.*", "http://10.1.151.*",
                        "http://10.1.152.*", "http://10.1.153.*", "http://10.1.154.*", "http://10.1.155.*",
                        "http://10.1.156.*", "http://10.1.157.*", "http://10.1.158.*", "http://10.1.159.*", "http://localhost:4200", "http://localhost:3001")
                .withSockJS();
    }
}
