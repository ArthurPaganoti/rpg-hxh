package com.rpg.rpghxh.shared.exceptions;

public class InvalidCredentialsException extends BusinessException {

    public InvalidCredentialsException() {
        super("Email ou senha inválidos");
    }
}
