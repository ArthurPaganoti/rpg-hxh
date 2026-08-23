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

    @ExceptionHandler({RoomAccessDeniedException.class, RoomMembershipRequiredException.class, UserBannedException.class})
    public ResponseEntity<ResponseDTO<Object>> handleRoomAccessDeniedException(BusinessException ex) {
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

    @ExceptionHandler({InvalidFileTypeException.class, InvalidImageTypeException.class})
    public ResponseEntity<ResponseDTO<Object>> handleInvalidFileTypeException(BusinessException ex) {
        return ResponseEntity
                .status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
                .body(ResponseDTO.error("BUSINESS_ERROR", ex.getMessage()));
    }

    @ExceptionHandler(org.springframework.web.multipart.MaxUploadSizeExceededException.class)
    public ResponseEntity<ResponseDTO<Object>> handleMaxUploadSize(org.springframework.web.multipart.MaxUploadSizeExceededException ex) {
        return ResponseEntity
                .status(HttpStatus.PAYLOAD_TOO_LARGE)
                .body(ResponseDTO.error("BUSINESS_ERROR", "Arquivo muito grande. Tamanho maximo: 50MB"));
    }

    @ExceptionHandler(FileStorageException.class)
    public ResponseEntity<ResponseDTO<Object>> handleFileStorage(FileStorageException ex) {
        log.error("Erro de armazenamento de arquivo", ex);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ResponseDTO.error("INTERNAL_ERROR", "Erro ao processar o arquivo"));
    }

    @ExceptionHandler({InvalidInviteException.class, PlayerNotInRoomException.class, BanNotFoundException.class, SheetNotFoundException.class, CoverNotFoundException.class})
    public ResponseEntity<ResponseDTO<Object>> handleInvalidInviteException(BusinessException ex) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ResponseDTO.error("BUSINESS_ERROR", ex.getMessage()));
    }

    @ExceptionHandler({RoomFullException.class, PlayerAlreadyInRoomException.class, MaxPlayersBelowCurrentException.class, MasterCannotLeaveRoomException.class, CannotRemoveMasterException.class, CannotBanMasterException.class})
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
