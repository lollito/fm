package com.lollito.fm.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.StompWebSocketEndpointRegistration;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class WebSocketConfigTest {

    @Mock
    private StompEndpointRegistry registry;

    @Mock
    private StompWebSocketEndpointRegistration registration;

    @Test
    public void registerStompEndpoints_ShouldConfigureAllowedOriginPatterns() {
        WebSocketConfig webSocketConfig = new WebSocketConfig();

        when(registry.addEndpoint("/ws/live-match")).thenReturn(registration);
        when(registration.setAllowedOriginPatterns(anyString(), anyString())).thenReturn(registration);

        webSocketConfig.registerStompEndpoints(registry);

        verify(registry).addEndpoint("/ws/live-match");
        verify(registration).setAllowedOriginPatterns("http://localhost:3000", "http://localhost:3001");
        verify(registration).withSockJS();
    }
}
