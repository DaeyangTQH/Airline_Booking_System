package com.group3.application.command;

import java.math.BigDecimal;
import java.util.UUID;

public record CreatePaymentCommand(
        UUID bookingId,
        BigDecimal amount,
        String currency,
        String method,
        String idempotencyKey,
        String clientIp
) {}
