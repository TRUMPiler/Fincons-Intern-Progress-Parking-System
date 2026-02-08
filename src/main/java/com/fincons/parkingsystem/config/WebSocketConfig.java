package com.fincons.parkingsystem.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * Configuration class for setting up WebSocket and STOMP messaging.
 * This class enables the message broker and registers the necessary endpoints.
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    /**
     * Configures the message broker, which is responsible for routing messages
     * from one client to another.
     *
     * @param config The registry for configuring the message broker.
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Enables a simple in-memory message broker to carry messages back to the client
        // on destinations prefixed with "/topic".
        config.enableSimpleBroker("/topic");
        // Designates the "/app" prefix for messages that are bound for @MessageMapping-annotated
        // methods in controller classes.
        config.setApplicationDestinationPrefixes("/app");
    }

    /**
     * Registers the STOMP endpoints, mapping each endpoint to a specific URL and
     * enabling CORS for allowed origins.
     *
     * @param registry The registry for STOMP endpoints.
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Registers the "/ws" endpoint, enabling WebSocket connections.
        // setAllowedOrigins is configured to allow connections from the specified origin.
        registry.addEndpoint("/ws")
                .setAllowedOrigins("http://localhost:4200");
    }
}
