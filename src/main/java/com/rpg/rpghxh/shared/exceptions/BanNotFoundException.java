package com.rpg.rpghxh.shared.exceptions;

public class BanNotFoundException extends BusinessException {

    public BanNotFoundException() {
        super("Este jogador nao esta banido da sala");
    }
}
