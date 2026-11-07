package com.group3.application.port.out;

import com.group3.application.integration.PaymentSession;
import com.group3.domain.vo.BookingId;
import com.group3.domain.vo.Money;

public interface PaymentPort {

    PaymentSession initiatePayment(
            BookingId bookingId,
            Money amount,
            String idempotencyKey
    );
}
