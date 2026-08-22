package com.rpg.rpghxh.shared.exceptions;

public class MasterCannotLeaveRoomException extends BusinessException {

    public MasterCannotLeaveRoomException() {
        super("O Mestre nao pode sair da sala. Delete a sala em vez disso");
    }
}