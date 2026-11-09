package com.group3.application.command;

public record HandleSePayReturnCommand(
        String orderInvoiceNumber,
        String orderId,
        String transactionId
) {
}
