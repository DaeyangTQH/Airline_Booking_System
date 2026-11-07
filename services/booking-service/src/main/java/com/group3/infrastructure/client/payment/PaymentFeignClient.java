package com.group3.infrastructure.client.payment;

import com.group3.infrastructure.client.payment.dto.CreatePaymentRequest;
import com.group3.infrastructure.client.payment.dto.CreatePaymentResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
        name = "payment-service",
        path = "/internal/v1/payments"
)
public interface PaymentFeignClient {

    @PostMapping
    CreatePaymentResponse createPayment(
            @RequestBody CreatePaymentRequest request
    );
}
