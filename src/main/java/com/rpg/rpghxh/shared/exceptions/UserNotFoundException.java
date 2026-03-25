package com.rpg.rpghxh.shared.exceptions;

public class UserNotFoundException extends BusinessException {

    public UserNotFoundException() {
        super("Usuário não encontrado");
    }
}
