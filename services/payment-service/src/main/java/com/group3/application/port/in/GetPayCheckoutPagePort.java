package com.group3.application.port.in;

import com.group3.application.result.PayCheckoutPageResult;

import java.util.UUID;

public interface GetPayCheckoutPagePort {

    PayCheckoutPageResult getCheckoutPage(UUID paymentId);
}
