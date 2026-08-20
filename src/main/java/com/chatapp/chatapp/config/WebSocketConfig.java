package com.chatapp.chatapp.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig
        implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(
            MessageBrokerRegistry registry) {

        /*
         * Messages sent by the server to clients.
         */
        registry.enableSimpleBroker(
                "/topic",
                "/queue"
        );

        /*
         * Messages sent by frontend to backend.
         *
         * Example:
         *
         * /app/meeting.chat
         * /app/meeting.join
         * /app/meeting.signal
         * /app/meeting.screen
         * /app/meeting.leave
         */
        registry.setApplicationDestinationPrefixes(
                "/app"
        );
    }

    @Override
    public void registerStompEndpoints(
            StompEndpointRegistry registry) {

        /*
         * SockJS endpoint.
         *
         * Frontend uses:
         *
         * const socket = new SockJS("/ws");
         */
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }
}