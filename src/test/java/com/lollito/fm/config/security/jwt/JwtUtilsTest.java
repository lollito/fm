package com.lollito.fm.config.security.jwt;

import com.lollito.fm.model.User;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilsTest {

    private JwtUtils jwtUtils;
    private String secret = "testSecretKeyForIntegrationTestingOnly";

    @BeforeEach
    void setUp() {
        jwtUtils = new JwtUtils();
        ReflectionTestUtils.setField(jwtUtils, "jwtSecret", secret);
        ReflectionTestUtils.setField(jwtUtils, "jwtExpirationMs", 3600000);
    }

    @Test
    void generateJwtToken_shouldThrowException_whenIdIsNull() {
        User user = new User();
        user.setUsername("testuser");
        user.setId(null);

        assertThrows(IllegalArgumentException.class, () -> {
            jwtUtils.generateJwtToken(user);
        });
    }

    @Test
    void generateJwtToken_shouldThrowException_whenUsernameIsNull() {
        User user = new User();
        user.setId(1L);
        user.setUsername(null);

        assertThrows(IllegalArgumentException.class, () -> {
            jwtUtils.generateJwtToken(user);
        });
    }

    @Test
    void generateJwtToken_shouldGenerateTokenWithUserIdClaim_whenUserIsValid() {
        User user = new User();
        user.setId(123L);
        user.setUsername("testuser");

        String token = jwtUtils.generateJwtToken(user);

        assertNotNull(token);
        assertTrue(jwtUtils.validateJwtToken(token));
        assertEquals("testuser", jwtUtils.getUserNameFromJwtToken(token));

        // Verify claim manually
        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        Long userId = Jwts.parser().verifyWith(key).build()
                .parseSignedClaims(token)
                .getPayload()
                .get("userId", Long.class);

        assertEquals(123L, userId);
    }
}
