package com.group3.api.rest;

import com.group3.api.dto.CreatePaymentRequest;
import com.group3.api.dto.CreatePaymentResponse;
import com.group3.application.command.CreatePaymentCommand;
import com.group3.application.port.in.CreatePaymentPort;
import com.group3.application.result.CreatePaymentResult;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/v1/payments")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PaymentController {

    CreatePaymentPort createPaymentPort;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CreatePaymentResponse create(
            @Valid @RequestBody CreatePaymentRequest request,
            HttpServletRequest httpRequest
    ) {
        CreatePaymentResult result = createPaymentPort.create(
                new CreatePaymentCommand(
                        request.bookingId(),
                        request.amount(),
                        request.currency(),
                        request.method(),
                        request.idempotencyKey(),
                        resolveClientIp(httpRequest)
                )
        );
        return new CreatePaymentResponse(
                result.paymentId(),
                result.status(),
                result.paymentUrl()
        );
    }

    private static String resolveClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
