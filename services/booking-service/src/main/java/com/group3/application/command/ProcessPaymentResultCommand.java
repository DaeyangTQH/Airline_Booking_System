package com.group3.application.command;

import com.group3.application.integration.PaymentResultStatus;
import com.group3.domain.vo.CurrencyCode;

import java.math.BigDecimal;
import java.util.UUID;

public record ProcessPaymentResultCommand(
        UUID bookingId,
        UUID paymentId,
        PaymentResultStatus status,
        BigDecimal amount,
        CurrencyCode currency
) {
}
