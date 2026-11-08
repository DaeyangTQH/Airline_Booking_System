package com.group3.api.rest;

import com.group3.api.dto.PaymentStatusResponse;
import com.group3.application.port.in.GetPaymentStatusPort;
import com.group3.application.result.PaymentStatusResult;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PaymentQueryController {

    GetPaymentStatusPort getPaymentStatusPort;

    @GetMapping("/{paymentId}/status")
    public PaymentStatusResponse status(@PathVariable("paymentId") UUID paymentId) {
        PaymentStatusResult result = getPaymentStatusPort.get(paymentId);
        return new PaymentStatusResponse(
                result.paymentId(),
                result.status(),
                result.paymentCode(),
                result.paymentUrl()
        );
    }
}
