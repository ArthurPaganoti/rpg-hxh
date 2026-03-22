package com.rpg.rpghxh.shared.validation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class PasswordValidatorTest {

    private PasswordValidator validator;

    @BeforeEach
    void setUp() {
        validator = new PasswordValidator();
        validator.initialize(null);
    }

    @Test
    void shouldReturnTrueForValidPassword() {
        assertTrue(validator.isValid("Senha@123", null));
    }

    @Test
    void shouldReturnTrueForPasswordWithDifferentSpecialChars() {
        assertTrue(validator.isValid("Abcdefg#1", null));
        assertTrue(validator.isValid("Abcdefg$1", null));
        assertTrue(validator.isValid("Abcdefg!1", null));
    }

    @ParameterizedTest
    @NullAndEmptySource
    void shouldReturnFalseForNullOrEmpty(String password) {
        assertFalse(validator.isValid(password, null));
    }

    @Test
    void shouldReturnFalseForBlankPassword() {
        assertFalse(validator.isValid("        ", null));
    }

    @Test
    void shouldReturnFalseForPasswordWithoutUppercase() {
        assertFalse(validator.isValid("senha@123", null));
    }

    @Test
    void shouldReturnFalseForPasswordWithoutLowercase() {
        assertFalse(validator.isValid("SENHA@123", null));
    }

    @Test
    void shouldReturnFalseForPasswordWithoutSpecialChar() {
        assertFalse(validator.isValid("Senha1234", null));
    }

    @Test
    void shouldReturnFalseForPasswordShorterThan8Chars() {
        assertFalse(validator.isValid("Se@1abc", null));
    }

    @Test
    void shouldReturnTrueForExactly8CharsValidPassword() {
        assertTrue(validator.isValid("Senh@12a", null));
    }
}
