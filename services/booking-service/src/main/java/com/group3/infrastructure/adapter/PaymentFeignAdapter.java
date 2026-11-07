package com.group3.infrastructure.adapter;

import com.group3.application.exception.PaymentServiceCommunicationException;
import com.group3.application.integration.PaymentSession;
import com.group3.application.port.out.PaymentPort;
import com.group3.domain.vo.BookingId;
import com.group3.domain.vo.Money;
import com.group3.infrastructure.client.payment.PaymentFeignClient;
import com.group3.infrastructure.client.payment.dto.CreatePaymentRequest;
import com.group3.infrastructure.client.payment.dto.CreatePaymentResponse;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PaymentFeignAdapter implements PaymentPort {

    private final PaymentFeignClient paymentFeignClient;

    @Override
    public PaymentSession initiatePayment(BookingId bookingId, Money amount, String idempotencyKey) {

        CreatePaymentRequest request =
                new CreatePaymentRequest(
                        bookingId.value(),
                        amount.amount(),
                        amount.currency().name(),
                        "VNPAY",
                        idempotencyKey
                );

        try{

            CreatePaymentResponse response =
                    paymentFeignClient.createPayment(request);

            return toPaymentSession(response);
        }catch(FeignException exception){
            throw new PaymentServiceCommunicationException(
                    "Could not initiate payment with Payment Service"
            );
        }
    }

    private PaymentSession toPaymentSession(
            CreatePaymentResponse response
    ) {
        if(response == null
                || response.paymentId() == null
                || response.status() == null
                || response.status().isBlank()
                || response.paymentUrl() == null
                || response.paymentUrl().isBlank()) {

            throw new PaymentServiceCommunicationException(
                    "Payment Service returned an invalid response"
            );
        }

        return new PaymentSession(
                response.paymentId(),
                response.status(),
                response.paymentUrl()
        );
    }
}
