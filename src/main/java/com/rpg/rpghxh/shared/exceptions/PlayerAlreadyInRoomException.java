package com.rpg.rpghxh.shared.exceptions;

public class PlayerAlreadyInRoomException extends BusinessException {

    public PlayerAlreadyInRoomException() {
        super("Voce ja esta nesta sala");
    }
}