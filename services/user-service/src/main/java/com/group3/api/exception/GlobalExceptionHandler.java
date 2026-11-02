package com.group3.api.exception;

import com.group3.domain.exception.DomainValidationException;
import com.group3.exception.CustomException;
import com.group3.exception.ErrorType;
import com.group3.response.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import java.time.Instant;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ErrorResponse> custom(CustomException ex, HttpServletRequest request) {
        HttpStatus status = switch (ex.getType()) {
            case VALIDATION -> HttpStatus.BAD_REQUEST;
            case NOT_FOUND -> HttpStatus.NOT_FOUND;
            case CONFLICT -> HttpStatus.CONFLICT;
            case UNAUTHORIZED -> HttpStatus.UNAUTHORIZED;
            case FORBIDDEN -> HttpStatus.FORBIDDEN;
            case INTERNAL -> HttpStatus.INTERNAL_SERVER_ERROR;
        };
        return error(status, ex.getCode(), ex.getMessage(), request);
    }

    @ExceptionHandler(DomainValidationException.class)
    public ResponseEntity<ErrorResponse> domain(DomainValidationException ex, HttpServletRequest request) {
        return error(HttpStatus.BAD_REQUEST, "DOMAIN_VALIDATION", ex.getMessage(), request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> validation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        String message = ex.getBindingResult().getFieldErrors().stream().findFirst()
                .map(FieldError::getDefaultMessage).orElse("Invalid request data");
        return error(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", message, request);
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class})
    public ResponseEntity<ErrorResponse> malformed(Exception ex, HttpServletRequest request) {
        return error(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", "Invalid request data", request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> unexpected(Exception ex, HttpServletRequest request) {
        log.error("Unhandled User Service exception", ex);
        return error(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", "Unexpected server error", request);
    }

    private ResponseEntity<ErrorResponse> error(HttpStatus status, String code, String message,
                                                HttpServletRequest request) {
        return ResponseEntity.status(status).body(
                new ErrorResponse(code, message, Instant.now(), request.getRequestURI()));
    }
}
