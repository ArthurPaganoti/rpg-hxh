package com.rpg.rpghxh.shared.exceptions;

public class RoomAccessDeniedException extends BusinessException {

    public RoomAccessDeniedException() {
        super("Apenas o Mestre da sala pode gerar o link de convite");
    }
}
