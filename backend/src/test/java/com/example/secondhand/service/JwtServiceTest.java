package com.example.secondhand.service;

import com.example.secondhand.model.Role;
import com.example.secondhand.model.User;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private static final String SECRET = "this-is-a-test-secret-key-with-enough-length-1234567890";
    private static final long EXPIRATION_MS = 1000 * 60 * 60; // 1 hour

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secretKey", SECRET);
        ReflectionTestUtils.setField(jwtService, "expiration", EXPIRATION_MS);
    }

    private User buildUser() {
        return User.builder()
                .id(1L)
                .username("ali123")
                .role(Role.USER)
                .build();
    }

    // ==================== generateToken ====================

    @Test
    void generateToken_shouldReturnNonNullNonEmptyToken() {
        String token = jwtService.generateToken(buildUser());

        assertNotNull(token);
        assertFalse(token.isBlank());
    }

    // ==================== extractUsername ====================

    @Test
    void extractUsername_shouldReturnCorrectUsername() {
        String token = jwtService.generateToken(buildUser());

        assertEquals("ali123", jwtService.extractUsername(token));
    }

    // ==================== extractUserId ====================

    @Test
    void extractUserId_shouldReturnCorrectUserId() {
        String token = jwtService.generateToken(buildUser());

        assertEquals(1L, jwtService.extractUserId(token));
    }

    // ==================== extractRole ====================

    @Test
    void extractRole_shouldReturnCorrectRole() {
        User admin = User.builder().id(2L).username("admin1").role(Role.ADMIN).build();
        String token = jwtService.generateToken(admin);

        assertEquals(Role.ADMIN, jwtService.extractRole(token));
    }

    // ==================== isTokenExpired ====================

    @Test
    void isTokenExpired_shouldReturnFalse_whenTokenIsFresh() {
        String token = jwtService.generateToken(buildUser());

        assertFalse(jwtService.isTokenExpired(token));
    }

    @Test
    void isTokenExpired_shouldThrow_whenTokenIsAlreadyExpired() {
        SecretKey key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
        Date past = new Date(System.currentTimeMillis() - 10_000);
        Date evenMorePast = new Date(System.currentTimeMillis() - 20_000);

        String expiredToken = Jwts.builder()
                .subject("ali123")
                .claim("userId", 1L)
                .claim("role", Role.USER.name())
                .issuedAt(evenMorePast)
                .expiration(past)
                .signWith(key)
                .compact();

        assertThrows(ExpiredJwtException.class,
                () -> jwtService.isTokenExpired(expiredToken));
    }

    // ==================== validateToken ====================

    @Test
    void validateToken_shouldReturnTrue_whenTokenIsValidAndUsernameMatches() {
        String token = jwtService.generateToken(buildUser());

        assertTrue(jwtService.validateToken(token, "ali123"));
    }

    @Test
    void validateToken_shouldReturnFalse_whenUsernameDoesNotMatch() {
        String token = jwtService.generateToken(buildUser());

        assertFalse(jwtService.validateToken(token, "someone-else"));
    }

    @Test
    void validateToken_shouldReturnFalse_whenTokenIsMalformed() {
        assertFalse(jwtService.validateToken("not-a-valid-token", "ali123"));
    }

    @Test
    void validateToken_shouldReturnFalse_whenTokenIsSignedWithDifferentKey() {
        SecretKey otherKey = Keys.hmacShaKeyFor(
                "a-completely-different-secret-key-1234567890abcdef".getBytes(StandardCharsets.UTF_8));
        Date now = new Date();
        Date expiry = new Date(now.getTime() + EXPIRATION_MS);

        String tokenFromOtherKey = Jwts.builder()
                .subject("ali123")
                .claim("userId", 1L)
                .claim("role", Role.USER.name())
                .issuedAt(now)
                .expiration(expiry)
                .signWith(otherKey)
                .compact();

        assertFalse(jwtService.validateToken(tokenFromOtherKey, "ali123"));
    }
}
