package com.rpg.rpghxh.business.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;

import java.time.Instant;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
public class ResponseDTO<T> {

    private final boolean success;
    private final String code;
    private final String message;
    private final T content;
    private final Instant timestamp;

    private ResponseDTO(boolean success, String code, String message, T content) {
        this.success = success;
        this.code = code;
        this.message = message;
        this.content = content;
        this.timestamp = Instant.now();
    }

    public static <T> ResponseDTO<T> success() {
        return new ResponseDTO<>(true, null, null, null);
    }

    public static <T> ResponseDTO<T> success(String message) {
        return new ResponseDTO<>(true, null, message, null);
    }

    public static <T> ResponseDTO<T> success(T content, String message) {
        return new ResponseDTO<>(true, null, message, content);
    }

    public static <T> ResponseDTO<T> error(String code, String message) {
        return new ResponseDTO<>(false, code, message, null);
    }
}

