package com.group3.api.dto;

import java.util.UUID;
public record CreatePaymentResponse(
        UUID paymentId,
        String status,
        String paymentUrl
) {
}

