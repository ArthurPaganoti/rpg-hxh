package com.rpg.rpghxh.shared.exceptions;

public class PlayerNotInRoomException extends BusinessException {

    public PlayerNotInRoomException() {
        super("Jogador nao esta na sala");
    }
}
