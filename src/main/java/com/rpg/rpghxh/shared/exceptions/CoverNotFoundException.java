package com.rpg.rpghxh.shared.exceptions;

public class CoverNotFoundException extends BusinessException {

    public CoverNotFoundException() {
        super("A sala nao tem imagem de capa");
    }
}
