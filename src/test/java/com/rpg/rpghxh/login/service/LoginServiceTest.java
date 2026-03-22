package com.rpg.rpghxh.login.service;

import com.rpg.rpghxh.entities.user.entity.User;
import com.rpg.rpghxh.entities.user.repository.UserRepository;
import com.rpg.rpghxh.login.dto.LoginDTO;
import com.rpg.rpghxh.shared.exceptions.InvalidCredentialsException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoginServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private RedisSessionService redisSessionService;

    @InjectMocks
    private LoginService loginService;

    private LoginDTO validDTO;
    private User user;

    @BeforeEach
    void setUp() {
        validDTO = LoginDTO.builder()
                .email("gon@hunter.com")
                .senha("Senha@123")
                .build();

        user = User.builder()
                .id(1L)
                .name("Gon Freecss")
                .email("gon@hunter.com")
                .senha("$2a$10$encodedPassword")
                .build();
    }

    @Test
    void authenticate_WithValidCredentials_ShouldReturnToken() {
        when(userRepository.findByEmail("gon@hunter.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("Senha@123", "$2a$10$encodedPassword")).thenReturn(true);
        when(jwtService.generateToken("gon@hunter.com")).thenReturn("jwt-token");
        when(jwtService.getIssuedAt("jwt-token")).thenReturn(Instant.now());
        when(jwtService.getExpiration("jwt-token")).thenReturn(Instant.now().plusSeconds(28800));

        String token = loginService.authenticate(validDTO);

        assertEquals("jwt-token", token);
        verify(redisSessionService).saveSession(eq("gon@hunter.com"), eq("jwt-token"), any(Instant.class), any(Instant.class));
    }

    @Test
    void authenticate_WithNonExistentEmail_ShouldThrowInvalidCredentialsException() {
        when(userRepository.findByEmail("gon@hunter.com")).thenReturn(Optional.empty());

        assertThrows(InvalidCredentialsException.class, () -> loginService.authenticate(validDTO));
        verify(jwtService, never()).generateToken(anyString());
    }

    @Test
    void authenticate_WithWrongPassword_ShouldThrowInvalidCredentialsException() {
        when(userRepository.findByEmail("gon@hunter.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("Senha@123", "$2a$10$encodedPassword")).thenReturn(false);

        assertThrows(InvalidCredentialsException.class, () -> loginService.authenticate(validDTO));
        verify(jwtService, never()).generateToken(anyString());
    }
}
