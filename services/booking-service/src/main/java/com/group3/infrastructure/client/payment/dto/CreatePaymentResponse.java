package com.group3.infrastructure.client.payment.dto;

import java.util.UUID;

public record CreatePaymentResponse(
        UUID paymentId,
        String status,
        String paymentUrl
) {
}
