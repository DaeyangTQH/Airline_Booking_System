package com.group3.api.dto;

import java.util.UUID;

public record PaymentStatusResponse(
        UUID paymentId,
        String status,
        String paymentCode,
        String paymentUrl
) {
}
