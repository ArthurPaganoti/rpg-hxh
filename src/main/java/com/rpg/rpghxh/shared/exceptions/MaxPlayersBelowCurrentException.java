package com.rpg.rpghxh.shared.exceptions;

public class MaxPlayersBelowCurrentException extends BusinessException {

    public MaxPlayersBelowCurrentException() {
        super("O maximo de jogadores nao pode ser menor que o numero atual de jogadores da sala");
    }
}