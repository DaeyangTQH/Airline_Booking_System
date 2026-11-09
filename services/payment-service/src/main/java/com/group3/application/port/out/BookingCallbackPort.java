package com.group3.application.port.out;

import com.group3.domain.aggregate.Payment;

public interface BookingCallbackPort {

    void notifyPaymentCompleted(Payment payment);
}
