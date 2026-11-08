package com.group3.application.port.in;

import com.group3.application.result.PaymentStatusResult;

import java.util.UUID;

public interface GetPaymentStatusPort {

    PaymentStatusResult get(UUID paymentId);
}
