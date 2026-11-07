package com.group3.application.result;

import java.util.UUID;

public record InitiatePaymentResult(
        UUID paymentId,
        String status,
        String paymentUrl
) {
}
