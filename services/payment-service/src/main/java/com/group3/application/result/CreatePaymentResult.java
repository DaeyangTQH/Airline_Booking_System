package com.group3.application.result;
import java.util.UUID;

public record CreatePaymentResult(
        UUID paymentId,
        String status,
        String paymentUrl
) {
}