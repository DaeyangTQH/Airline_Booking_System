package com.group3.flightsearch.api.exception;

import com.group3.exception.CustomException;
import com.group3.flightsearch.domain.exception.InvalidDomainStateException;
import com.group3.flightsearch.domain.exception.InvalidDomainValueException;
import com.group3.response.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ErrorResponse> handleCustom(CustomException exception, HttpServletRequest request) {
        HttpStatus status;
        switch (exception.getType()) {
            case VALIDATION:
                status = HttpStatus.BAD_REQUEST;
                break;
            case NOT_FOUND:
                status = HttpStatus.NOT_FOUND;
                break;
            case CONFLICT:
                status = HttpStatus.CONFLICT;
                break;
            case UNAUTHORIZED:
                status = HttpStatus.UNAUTHORIZED;
                break;
            case FORBIDDEN:
                status = HttpStatus.FORBIDDEN;
                break;
            case INTERNAL:
                status = HttpStatus.INTERNAL_SERVER_ERROR;
                break;
            default:
                throw new IllegalStateException("Unsupported error type: " + exception.getType());
        }
        return error(status, exception.getCode(), exception.getMessage(), request);
    }

    @ExceptionHandler(InvalidDomainValueException.class)
    public ResponseEntity<ErrorResponse> handleDomainValue(
            InvalidDomainValueException exception, HttpServletRequest request) {
        return error(HttpStatus.BAD_REQUEST, "INVALID_DOMAIN_VALUE", exception.getMessage(), request);
    }

    @ExceptionHandler(InvalidDomainStateException.class)
    public ResponseEntity<ErrorResponse> handleDomainState(
            InvalidDomainStateException exception, HttpServletRequest request) {
        return error(HttpStatus.CONFLICT, "INVALID_DOMAIN_STATE", exception.getMessage(), request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(
            MethodArgumentNotValidException exception, HttpServletRequest request) {
        Set<String> messages = new LinkedHashSet<>();
        for (var field : exception.getBindingResult().getFieldErrors()) {
            messages.add(field.getField() + ": " + field.getDefaultMessage());
        }
        String message = String.join("; ", messages);
        return error(HttpStatus.BAD_REQUEST, "REQUEST_VALIDATION_FAILED", message, request);
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class,
            MissingServletRequestParameterException.class})
    public ResponseEntity<ErrorResponse> handleMalformedRequest(Exception exception, HttpServletRequest request) {
        return error(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", "Invalid JSON or request parameter", request);
    }

    private ResponseEntity<ErrorResponse> error(
            HttpStatus status, String code, String message, HttpServletRequest request) {
        return ResponseEntity.status(status)
                .body(new ErrorResponse(code, message, Instant.now(), request.getRequestURI()));
    }
}
