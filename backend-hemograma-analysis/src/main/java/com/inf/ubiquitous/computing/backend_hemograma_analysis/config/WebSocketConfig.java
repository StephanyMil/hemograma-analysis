package com.inf.ubiquitous.computing.backend_hemograma_analysis.config;

import org.springframework.context.annotation.Configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

//@Configuration
//@EnableWebSocketMessageBroker
//public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
//
//    @Override
//    public void configureMessageBroker(MessageBrokerRegistry config) {
//        // Configura o broker de mensagens
//        config.enableSimpleBroker("/topic", "/queue");
//        config.setApplicationDestinationPrefixes("/app");
//    }
//
//    @Override
//    public void registerStompEndpoints(StompEndpointRegistry registry) {
//        // Endpoint WebSocket
//        registry.addEndpoint("/ws-notificacoes")
//                .setAllowedOriginPatterns("*")
//                .withSockJS(); // Fallback para navegadores sem WebSocket
//    }
//}
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic", "/queue");
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // WebSocket puro (sem SockJS)
        registry.addEndpoint("/ws-notificacoes")
                .setAllowedOriginPatterns("*");

        // Fallback SockJS
        registry.addEndpoint("/ws-notificacoes")
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }
}