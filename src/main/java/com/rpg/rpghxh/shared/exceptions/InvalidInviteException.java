package com.rpg.rpghxh.shared.exceptions;

public class InvalidInviteException extends BusinessException {

    public InvalidInviteException() {
        super("Convite invalido ou expirado");
    }
}