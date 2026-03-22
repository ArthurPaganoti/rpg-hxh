package com.rpg.rpghxh.shared.validation;

import com.rpg.rpghxh.register.dto.RegisterDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PasswordMatchValidatorTest {

    private PasswordMatchValidator validator;

    @BeforeEach
    void setUp() {
        validator = new PasswordMatchValidator();
        PasswordMatch annotation = mock(PasswordMatch.class);
        when(annotation.password()).thenReturn("senha");
        when(annotation.confirmPassword()).thenReturn("confirmacaoSenha");
        validator.initialize(annotation);
    }

    @Test
    void shouldReturnTrueWhenPasswordsMatch() {
        RegisterDTO dto = RegisterDTO.builder()
                .senha("Senha@123")
                .confirmacaoSenha("Senha@123")
                .build();

        assertTrue(validator.isValid(dto, null));
    }

    @Test
    void shouldReturnFalseWhenPasswordsDoNotMatch() {
        RegisterDTO dto = RegisterDTO.builder()
                .senha("Senha@123")
                .confirmacaoSenha("Outra@456")
                .build();

        assertFalse(validator.isValid(dto, null));
    }

    @Test
    void shouldReturnTrueWhenBothPasswordsAreNull() {
        RegisterDTO dto = RegisterDTO.builder()
                .senha(null)
                .confirmacaoSenha(null)
                .build();

        assertTrue(validator.isValid(dto, null));
    }

    @Test
    void shouldReturnFalseWhenPasswordIsNullAndConfirmIsNot() {
        RegisterDTO dto = RegisterDTO.builder()
                .senha(null)
                .confirmacaoSenha("Senha@123")
                .build();

        assertFalse(validator.isValid(dto, null));
    }

    @Test
    void shouldReturnTrueWhenObjectIsNull() {
        assertTrue(validator.isValid(null, null));
    }
}
