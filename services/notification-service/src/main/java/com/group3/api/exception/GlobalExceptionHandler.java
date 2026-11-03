package com.group3.api.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.MailAuthenticationException;
import org.springframework.mail.MailSendException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 1. Bắt lỗi xác thực email (Sai mật khẩu ứng dụng, tài khoản bị khóa...)
    @ExceptionHandler(MailAuthenticationException.class)
    public ResponseEntity<?> handleMailAuthenticationException(MailAuthenticationException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        body.put("error", "Lỗi xác thực Email Server");
        body.put("message", "Không thể đăng nhập vào Gmail. Vui lòng kiểm tra lại Mật khẩu ứng dụng (App Password) trong file application.properties.");
        body.put("details", ex.getMessage());

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }

    // 2. Bắt lỗi gửi email chung chung (Mất mạng, sai địa chỉ người nhận...)
    @ExceptionHandler(MailSendException.class)
    public ResponseEntity<?> handleMailSendException(MailSendException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        body.put("error", "Lỗi gửi Email");
        body.put("message", "Hệ thống không thể gửi email đến người nhận. Vui lòng kiểm tra lại địa chỉ email.");
        body.put("details", ex.getMessage());

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }

    // 3. Bắt lỗi dữ liệu đầu vào không hợp lệ (Email sai định dạng, giờ bay sai...)
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> handleBadRequestException(IllegalArgumentException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("error", "Dữ liệu không hợp lệ");
        body.put("message", ex.getMessage());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }
}