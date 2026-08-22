package com.rpg.rpghxh.shared.exceptions;

public class SheetNotFoundException extends BusinessException {

    public SheetNotFoundException() {
        super("Ficha nao encontrada");
    }
}