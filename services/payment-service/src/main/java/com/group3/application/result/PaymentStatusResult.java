package com.group3.application.result;

import java.util.UUID;

public record PaymentStatusResult(
        UUID paymentId,
        String status,
        String paymentCode,
        String paymentUrl
) {
}
