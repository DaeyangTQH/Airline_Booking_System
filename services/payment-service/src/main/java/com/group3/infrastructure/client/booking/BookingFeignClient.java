package com.group3.infrastructure.client.booking;

import com.group3.infrastructure.client.booking.dto.PaymentResultRequest;
import com.group3.infrastructure.config.BookingFeignConfiguration;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.UUID;

@FeignClient(
        name = "booking-service",
        path = "/internal/v1/bookings",
        configuration = BookingFeignConfiguration.class
)
public interface BookingFeignClient {

    @PostMapping("/{bookingId}/payment-result")
    void notifyPaymentResult(
            @PathVariable("bookingId") UUID bookingId,
            @RequestBody PaymentResultRequest request
    );
}
