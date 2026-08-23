package com.rpg.rpghxh.shared.exceptions;

public class InvalidImageTypeException extends BusinessException {

    public InvalidImageTypeException() {
        super("Tipo de imagem nao permitido. Envie PNG, JPG ou WEBP");
    }
}
