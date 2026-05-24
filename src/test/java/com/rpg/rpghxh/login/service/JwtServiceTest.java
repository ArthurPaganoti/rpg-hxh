package com.rpg.rpghxh.login.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;
    private static final String SECRET = "test-secret-key-for-jwt-must-be-at-least-32-characters-long";
    private static final long EXPIRATION = 28800000;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(SECRET, EXPIRATION);
    }

    @Test
    void generateToken_ShouldReturnNonNullToken() {
        String token = jwtService.generateToken("gon@hunter.com");

        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    void extractEmail_ShouldReturnCorrectEmail() {
        String token = jwtService.generateToken("gon@hunter.com");

        String email = jwtService.extractEmail(token);

        assertEquals("gon@hunter.com", email);
    }

    @Test
    void isTokenValid_WithValidToken_ShouldReturnTrue() {
        String token = jwtService.generateToken("gon@hunter.com");

        assertTrue(jwtService.isTokenValid(token));
    }

    @Test
    void isTokenValid_WithInvalidToken_ShouldReturnFalse() {
        assertFalse(jwtService.isTokenValid("invalid-token"));
    }

    @Test
    void isTokenValid_WithTamperedToken_ShouldReturnFalse() {
        String token = jwtService.generateToken("gon@hunter.com");
        String tamperedToken = token + "tampered";

        assertFalse(jwtService.isTokenValid(tamperedToken));
    }

    @Test
    void isTokenValid_WithExpiredToken_ShouldReturnFalse() {
        JwtService shortLivedService = new JwtService(SECRET, 0);
        String token = shortLivedService.generateToken("gon@hunter.com");

        assertFalse(shortLivedService.isTokenValid(token));
    }

    @Test
    void isTokenValid_WithDifferentSecret_ShouldReturnFalse() {
        String token = jwtService.generateToken("gon@hunter.com");
        JwtService otherService = new JwtService(
                "another-secret-key-must-be-at-least-32-characters-long-too", EXPIRATION);

        assertFalse(otherService.isTokenValid(token));
    }

    @Test
    void getIssuedAt_ShouldReturnPastInstant() {
        Instant before = Instant.now().minusSeconds(1);
        String token = jwtService.generateToken("gon@hunter.com");

        Instant issuedAt = jwtService.getIssuedAt(token);

        assertTrue(issuedAt.isAfter(before));
        assertTrue(issuedAt.isBefore(Instant.now().plusSeconds(1)));
    }

    @Test
    void getExpiration_ShouldReturnFutureInstant() {
        String token = jwtService.generateToken("gon@hunter.com");

        Instant expiration = jwtService.getExpiration(token);

        assertTrue(expiration.isAfter(Instant.now()));
    }

    @Test
    void generateToken_ShouldProduceDifferentTokensForDifferentEmails() {
        String token1 = jwtService.generateToken("gon@hunter.com");
        String token2 = jwtService.generateToken("killua@hunter.com");

        assertNotEquals(token1, token2);
    }
}
