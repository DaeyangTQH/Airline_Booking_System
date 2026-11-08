package com.group3.infrastructure.adapter;

import com.group3.application.exception.BookingCallbackException;
import com.group3.application.port.out.BookingCallbackPort;
import com.group3.domain.aggregate.Payment;
import com.group3.domain.aggregate.PaymentStatus;
import com.group3.infrastructure.client.booking.BookingFeignClient;
import com.group3.infrastructure.client.booking.dto.PaymentResultRequest;
import feign.FeignException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class BookingCallbackAdapter implements BookingCallbackPort {

    BookingFeignClient bookingFeignClient;

    @Override
    public void notifyPaymentCompleted(Payment payment) {
        PaymentResultRequest request = new PaymentResultRequest(
                payment.getId().value(),
                PaymentStatus.COMPLETED.name(),
                payment.getAmount().amount(),
                payment.getAmount().currency().name()
        );

        try {
            bookingFeignClient.notifyPaymentResult(
                    payment.getBookingId().value(),
                    request
            );
        } catch (FeignException ex) {
            throw new BookingCallbackException(
                    "Could not notify booking-service of payment result",
                    ex
            );
        }
    }
}
