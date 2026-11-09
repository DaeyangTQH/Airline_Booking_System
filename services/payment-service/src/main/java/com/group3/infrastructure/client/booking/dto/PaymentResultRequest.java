package com.group3.infrastructure.client.booking.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record PaymentResultRequest(
        UUID paymentId,
        String status,
        BigDecimal amount,
        String currency
) {
}
