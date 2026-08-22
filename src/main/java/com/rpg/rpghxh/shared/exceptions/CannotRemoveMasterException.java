package com.rpg.rpghxh.shared.exceptions;

public class CannotRemoveMasterException extends BusinessException {

    public CannotRemoveMasterException() {
        super("O Mestre nao pode ser removido da sala");
    }
}