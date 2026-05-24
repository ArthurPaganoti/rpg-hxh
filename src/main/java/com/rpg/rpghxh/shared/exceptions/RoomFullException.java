package com.rpg.rpghxh.shared.exceptions;

public class RoomFullException extends BusinessException {

    public RoomFullException() {
        super("A sala esta cheia");
    }
}