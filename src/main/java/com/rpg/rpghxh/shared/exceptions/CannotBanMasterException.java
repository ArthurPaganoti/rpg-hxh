package com.rpg.rpghxh.shared.exceptions;

public class CannotBanMasterException extends BusinessException {

    public CannotBanMasterException() {
        super("O Mestre nao pode ser banido da sala");
    }
}
