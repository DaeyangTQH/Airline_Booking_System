package com.group3.application.integration;

import java.util.UUID;

public record PaymentSession(
        UUID paymentId,
        String status,
        String paymentUrl
) {
}
