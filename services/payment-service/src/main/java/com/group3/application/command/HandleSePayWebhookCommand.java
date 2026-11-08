package com.group3.application.command;

import java.math.BigDecimal;

public record HandleSePayWebhookCommand(
        Long sepayTxnId,
        String code,
        String content,
        String transferType,
        BigDecimal transferAmount,
        String referenceCode,
        String authorizationHeader
) {
}
