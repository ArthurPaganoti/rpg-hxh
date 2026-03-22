package com.rpg.rpghxh.register.service;

import com.rpg.rpghxh.shared.dto.ResponseDTO;
import com.rpg.rpghxh.entities.user.entity.User;
import com.rpg.rpghxh.shared.exceptions.EmailAlreadyExistsException;
import com.rpg.rpghxh.shared.exceptions.NameAlreadyExistsException;
import com.rpg.rpghxh.register.dto.RegisterDTO;
import com.rpg.rpghxh.register.mapper.RegisterMapper;
import com.rpg.rpghxh.entities.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegisterServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private RegisterMapper registerMapper;

    @InjectMocks
    private RegisterService registerService;

    private RegisterDTO validDTO;

    @BeforeEach
    void setUp() {
        validDTO = RegisterDTO.builder()
                .name("Gon Freecss")
                .email("gon@hunter.com")
                .senha("Senha@123")
                .confirmacaoSenha("Senha@123")
                .build();
    }

    @Test
    void register_WithValidData_ShouldReturnSuccess() {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.existsByName(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(registerMapper.toEntity(any(RegisterDTO.class), anyString())).thenReturn(new User());
        when(userRepository.save(any(User.class))).thenReturn(new User());

        ResponseDTO<String> response = registerService.register(validDTO);

        assertTrue(response.isSuccess());
        assertEquals("Usuário registrado com sucesso", response.getMessage());
        verify(registerMapper).toEntity(validDTO, "encodedPassword");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_WithExistingEmail_ShouldThrowEmailAlreadyExistsException() {
        when(userRepository.existsByEmail("gon@hunter.com")).thenReturn(true);

        assertThrows(EmailAlreadyExistsException.class, () -> registerService.register(validDTO));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void register_WithExistingName_ShouldThrowNameAlreadyExistsException() {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.existsByName("Gon Freecss")).thenReturn(true);

        assertThrows(NameAlreadyExistsException.class, () -> registerService.register(validDTO));
        verify(userRepository, never()).save(any(User.class));
    }
}
