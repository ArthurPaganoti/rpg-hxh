package com.rpg.rpghxh.shared.exceptions;

public class RoomMembershipRequiredException extends BusinessException {

    public RoomMembershipRequiredException() {
        super("Apenas membros da sala podem acessar");
    }
}
