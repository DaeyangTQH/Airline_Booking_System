package com.group3.api.dto;

import com.group3.application.integration.PaymentResultStatus;
import com.group3.domain.vo.CurrencyCode;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public record PaymentResultRequest(

        @NotNull(message = "Payment ID must not be null")
        UUID paymentId,

        @NotNull(message = "Payment status must not be null")
        PaymentResultStatus status,

        @NotNull(message = "Payment amount must not be null")
        @DecimalMin(
                value = "0.0",
                inclusive = false,
                message = "Payment amount must be greater than zero"
        )
        BigDecimal amount,

        @NotNull(message = "Currency must not be null")
        CurrencyCode currency
) {
}
