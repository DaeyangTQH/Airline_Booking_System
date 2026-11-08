package com.group3.application.service;

import com.group3.application.command.HandleSePayWebhookCommand;
import com.group3.application.exception.BookingCallbackException;
import com.group3.application.port.in.HandleSePayWebhookPort;
import com.group3.application.port.out.BookingCallbackPort;
import com.group3.application.port.out.SePayGatewayPort;
import com.group3.domain.aggregate.Payment;
import com.group3.domain.aggregate.PaymentStatus;
import com.group3.domain.repository.PaymentRepository;
import com.group3.domain.vo.PaymentId;
import com.group3.domain.vo.ProviderTxnId;
import com.group3.infrastructure.config.SePayProperties;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class HandleSePayWebhookService implements HandleSePayWebhookPort {

    SePayProperties sePayProperties;
    SePayGatewayPort sePayGatewayPort;
    PaymentRepository paymentRepository;
    BookingCallbackPort bookingCallbackPort;

    @Override
    @Transactional
    public boolean handle(HandleSePayWebhookCommand command) {
        if (command == null) {
            return false;
        }
        if (!isAuthorized(command.authorizationHeader())) {
            return false;
        }
        if (!"in".equalsIgnoreCase(nullToEmpty(command.transferType()))) {
            // Bỏ qua tiền ra — vẫn trả success để SePay không retry vô ích
            return true;
        }
        if (command.transferAmount() == null
                || command.transferAmount().compareTo(BigDecimal.ZERO) <= 0) {
            return true;
        }

        Payment payment = resolvePayment(command).orElse(null);
        if (payment == null) {
            return true;
        }

        if (payment.getStatus() == PaymentStatus.COMPLETED) {
            try {
                bookingCallbackPort.notifyPaymentCompleted(payment);
            } catch (BookingCallbackException ignored) {
                // idempotent ack
            }
            return true;
        }

        if (payment.getStatus() != PaymentStatus.PENDING) {
            return true;
        }

        // Cho phép thanh toán dư; không chấp nhận thiếu tiền
        if (command.transferAmount().compareTo(payment.getAmount().amount()) < 0) {
            return true;
        }

        String txn = command.sepayTxnId() != null
                ? String.valueOf(command.sepayTxnId())
                : nullToEmpty(command.referenceCode());
        if (txn.isBlank()) {
            txn = "SEPAY-" + payment.getId().value();
        }

        payment.complete(new ProviderTxnId(txn));
        Payment saved = paymentRepository.save(payment);
        try {
            bookingCallbackPort.notifyPaymentCompleted(saved);
        } catch (BookingCallbackException ex) {
            // Payment đã COMPLETED — ack SePay; booking có thể retry bằng webhook replay
        }
        return true;
    }

    private Optional<Payment> resolvePayment(HandleSePayWebhookCommand command) {
        Optional<UUID> fullId = sePayGatewayPort.parsePaymentId(command.code());
        if (fullId.isEmpty()) {
            fullId = sePayGatewayPort.parsePaymentId(command.content());
        }
        if (fullId.isPresent()) {
            return paymentRepository.findById(new PaymentId(fullId.get()));
        }

        Optional<String> suffix = sePayGatewayPort.extractCodeSuffix(command.code());
        if (suffix.isEmpty()) {
            suffix = sePayGatewayPort.extractCodeSuffix(command.content());
        }
        return suffix.flatMap(paymentRepository::findByIdHexPrefix);
    }

    private boolean isAuthorized(String authorizationHeader) {
        String expectedKey = sePayProperties.getWebhookApiKey();
        if (expectedKey == null || expectedKey.isBlank()) {
            // Dev: chưa cấu hình key thì chấp nhận (local). Prod phải set SEPAY_WEBHOOK_API_KEY.
            return true;
        }
        if (authorizationHeader == null || authorizationHeader.isBlank()) {
            return false;
        }
        String expected = "Apikey " + expectedKey;
        return constantTimeEquals(expected, authorizationHeader.trim());
    }

    private static boolean constantTimeEquals(String a, String b) {
        byte[] left = a.getBytes(StandardCharsets.UTF_8);
        byte[] right = b.getBytes(StandardCharsets.UTF_8);
        return MessageDigest.isEqual(left, right);
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}
