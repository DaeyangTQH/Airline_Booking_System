package com.group3.infrastructure.client.payment.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record CreatePaymentRequest(
        UUID bookingId,
        BigDecimal amount,
        String currency,
        String method,
        String idempotencyKey
) {
}
