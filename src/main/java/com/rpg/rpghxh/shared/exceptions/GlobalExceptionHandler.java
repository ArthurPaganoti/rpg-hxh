package com.rpg.rpghxh.shared.exceptions;

import com.rpg.rpghxh.shared.dto.ResponseDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ResponseDTO<Object>> handleInvalidCredentialsException(InvalidCredentialsException ex) {
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(ResponseDTO.error("BUSINESS_ERROR", ex.getMessage()));
    }

    @ExceptionHandler(RoomAccessDeniedException.class)
    public ResponseEntity<ResponseDTO<Object>> handleRoomAccessDeniedException(RoomAccessDeniedException ex) {
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(ResponseDTO.error("BUSINESS_ERROR", ex.getMessage()));
    }

    @ExceptionHandler(RoomNotFoundException.class)
    public ResponseEntity<ResponseDTO<Object>> handleRoomNotFoundException(RoomNotFoundException ex) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ResponseDTO.error("BUSINESS_ERROR", ex.getMessage()));
    }

    @ExceptionHandler({NoHandlerFoundException.class, NoResourceFoundException.class})
    public ResponseEntity<ResponseDTO<Object>> handleNoHandlerFound(Exception ex) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ResponseDTO.error("BUSINESS_ERROR", "Endpoint nao encontrado"));
    }

    @ExceptionHandler(InvalidInviteException.class)
    public ResponseEntity<ResponseDTO<Object>> handleInvalidInviteException(InvalidInviteException ex) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ResponseDTO.error("BUSINESS_ERROR", ex.getMessage()));
    }

    @ExceptionHandler({RoomFullException.class, PlayerAlreadyInRoomException.class, MaxPlayersBelowCurrentException.class})
    public ResponseEntity<ResponseDTO<Object>> handleConflictExceptions(BusinessException ex) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(ResponseDTO.error("BUSINESS_ERROR", ex.getMessage()));
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ResponseDTO<Object>> handleBusinessException(BusinessException ex) {
        return ResponseEntity
                .badRequest()
                .body(ResponseDTO.error("BUSINESS_ERROR", ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ResponseDTO<Object>> handleValidationException(MethodArgumentNotValidException ex) {

        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String key = (error instanceof FieldError fieldError)
                    ? fieldError.getField()
                    : error.getObjectName();
            String errorMessage = error.getDefaultMessage();
            errors.put(key, errorMessage);
        });

        return ResponseEntity
                .badRequest()
                .body(ResponseDTO.error("VALIDATION_ERROR", errors, "Erro de validação dos campos"));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ResponseDTO<Object>> handleGenericException(Exception ex) {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ResponseDTO.error("INTERNAL_ERROR", "Erro interno do servidor"));
    }
}
