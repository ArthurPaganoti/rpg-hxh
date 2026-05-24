package com.rpg.rpghxh.login.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.io.IOException;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RateLimitFilterTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Mock
    private FilterChain filterChain;

    private RateLimitFilter rateLimitFilter;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @BeforeEach
    void setUp() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        rateLimitFilter = new RateLimitFilter(redisTemplate, objectMapper);
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        request.setRemoteAddr("127.0.0.1");
    }

    @Test
    void doFilterInternal_WithinLimit_ShouldContinueChain() throws ServletException, IOException {
        request.setMethod("POST");
        request.setServletPath("/login");
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.increment("rate_limit:login:127.0.0.1")).thenReturn(1L);

        rateLimitFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertEquals(200, response.getStatus());
    }

    @Test
    void doFilterInternal_FirstRequest_ShouldSetExpire() throws ServletException, IOException {
        request.setMethod("POST");
        request.setServletPath("/login");
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.increment("rate_limit:login:127.0.0.1")).thenReturn(1L);

        rateLimitFilter.doFilterInternal(request, response, filterChain);

        verify(redisTemplate).expire(eq("rate_limit:login:127.0.0.1"), eq(Duration.ofMinutes(1)));
    }

    @Test
    void doFilterInternal_SubsequentRequest_ShouldNotResetExpire() throws ServletException, IOException {
        request.setMethod("POST");
        request.setServletPath("/login");
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.increment("rate_limit:login:127.0.0.1")).thenReturn(3L);

        rateLimitFilter.doFilterInternal(request, response, filterChain);

        verify(redisTemplate, never()).expire(any(), any(Duration.class));
    }

    @Test
    void doFilterInternal_ExceedsLimit_ShouldReturn429() throws ServletException, IOException {
        request.setMethod("POST");
        request.setServletPath("/login");
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.increment("rate_limit:login:127.0.0.1")).thenReturn(6L);

        rateLimitFilter.doFilterInternal(request, response, filterChain);

        assertEquals(429, response.getStatus());
        assertTrue(response.getContentType().startsWith("application/json"));
        verify(filterChain, never()).doFilter(request, response);
    }

    @Test
    void doFilterInternal_ExceedsLimit_ShouldReturnErrorBody() throws ServletException, IOException {
        request.setMethod("POST");
        request.setServletPath("/register");
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.increment("rate_limit:register:127.0.0.1")).thenReturn(6L);

        rateLimitFilter.doFilterInternal(request, response, filterChain);

        String body = response.getContentAsString();
        assertTrue(body.contains("Muitas tentativas"));
        assertTrue(body.contains("VALIDATION_ERROR"));
    }

    @Test
    void shouldNotFilter_GetRequest_ShouldSkipFilter() {
        request.setMethod("GET");
        request.setServletPath("/login");

        assertTrue(rateLimitFilter.shouldNotFilter(request));
    }

    @Test
    void shouldNotFilter_PostToOtherRoute_ShouldSkipFilter() {
        request.setMethod("POST");
        request.setServletPath("/rooms");

        assertTrue(rateLimitFilter.shouldNotFilter(request));
    }

    @Test
    void shouldNotFilter_PostToJoinRoom_ShouldNotSkip() {
        request.setMethod("POST");
        request.setServletPath("/rooms/join/some-hash-uuid");

        assertFalse(rateLimitFilter.shouldNotFilter(request));
    }

    @Test
    void doFilterInternal_JoinRoom_ShouldUseNormalizedKey() throws ServletException, IOException {
        request.setMethod("POST");
        request.setServletPath("/rooms/join/810fe538-edc4-4687-9304-81e0317da443");
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.increment("rate_limit:rooms/join:127.0.0.1")).thenReturn(1L);

        rateLimitFilter.doFilterInternal(request, response, filterChain);

        verify(valueOperations).increment("rate_limit:rooms/join:127.0.0.1");
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_JoinRoom_ExceedsLimit_ShouldReturn429() throws ServletException, IOException {
        request.setMethod("POST");
        request.setServletPath("/rooms/join/810fe538-edc4-4687-9304-81e0317da443");
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.increment("rate_limit:rooms/join:127.0.0.1")).thenReturn(6L);

        rateLimitFilter.doFilterInternal(request, response, filterChain);

        assertEquals(429, response.getStatus());
        verify(filterChain, never()).doFilter(request, response);
    }

    @Test
    void shouldNotFilter_PostToLogin_ShouldNotSkip() {
        request.setMethod("POST");
        request.setServletPath("/login");

        assertFalse(rateLimitFilter.shouldNotFilter(request));
    }

    @Test
    void shouldNotFilter_PostToRegister_ShouldNotSkip() {
        request.setMethod("POST");
        request.setServletPath("/register");

        assertFalse(rateLimitFilter.shouldNotFilter(request));
    }

    @Test
    void doFilterInternal_WithXForwardedFor_ShouldUseFirstIp() throws ServletException, IOException {
        request.setMethod("POST");
        request.setServletPath("/login");
        request.addHeader("X-Forwarded-For", "192.168.1.1, 10.0.0.1");
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.increment("rate_limit:login:192.168.1.1")).thenReturn(1L);

        rateLimitFilter.doFilterInternal(request, response, filterChain);

        verify(valueOperations).increment("rate_limit:login:192.168.1.1");
        verify(filterChain).doFilter(request, response);
    }
}
