package com.lollito.fm.config.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
public class WebSecurityConfigTest {

    @Autowired
    private CorsConfigurationSource corsConfigurationSource;

    @Test
    public void testCorsConfiguration() {
        assertNotNull(corsConfigurationSource, "CorsConfigurationSource bean should be present");

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/test");
        CorsConfiguration config = corsConfigurationSource.getCorsConfiguration(request);

        assertNotNull(config, "CorsConfiguration should not be null");

        List<String> allowedOrigins = config.getAllowedOrigins();
        assertNotNull(allowedOrigins, "Allowed origins should not be null");

        // Verify default values from application.properties
        assertTrue(allowedOrigins.contains("http://localhost:3000"), "Should contain http://localhost:3000");
        assertTrue(allowedOrigins.contains("http://localhost:3001"), "Should contain http://localhost:3001");
    }
}
