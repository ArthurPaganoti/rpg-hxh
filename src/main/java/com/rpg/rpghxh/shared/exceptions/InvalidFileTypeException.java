package com.rpg.rpghxh.shared.exceptions;

public class InvalidFileTypeException extends BusinessException {

    public InvalidFileTypeException() {
        super("Tipo de arquivo nao permitido. Envie PDF, DOC, DOCX ou ODT");
    }
}