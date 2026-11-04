package com.group3.api.exception;

import com.group3.response.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.MailAuthenticationException;
import org.springframework.mail.MailSendException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 1. Bắt lỗi xác thực Email (Sai App Password...)
    @ExceptionHandler(MailAuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleMailAuthException(
            MailAuthenticationException ex, HttpServletRequest request) {

        ErrorResponse error = new ErrorResponse(
                "MAIL_AUTH_FAILED",
                "Không thể đăng nhập vào Gmail. Vui lòng kiểm tra lại Mật khẩu ứng dụng (App Password).",
                Instant.now(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    // 2. Bắt lỗi gửi Email (Sai địa chỉ, mất mạng...)
    @ExceptionHandler(MailSendException.class)
    public ResponseEntity<ErrorResponse> handleMailSendException(
            MailSendException ex, HttpServletRequest request) {

        ErrorResponse error = new ErrorResponse(
                "MAIL_SEND_FAILED",
                "Hệ thống không thể gửi email đến người nhận. Vui lòng kiểm tra lại địa chỉ email.",
                Instant.now(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    // 3. Bắt lỗi dữ liệu đầu vào (Email sai định dạng, thời gian sai...)
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleBadRequest(
            IllegalArgumentException ex, HttpServletRequest request) {

        ErrorResponse error = new ErrorResponse(
                "BAD_REQUEST",
                ex.getMessage(),
                Instant.now(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    // 4. Bắt tất cả các lỗi hệ thống khác (Fallback)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGlobalException(
            Exception ex, HttpServletRequest request) {

        ErrorResponse error = new ErrorResponse(
                "INTERNAL_SERVER_ERROR",
                "Đã xảy ra lỗi hệ thống. Vui lòng thử lại sau.",
                Instant.now(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}