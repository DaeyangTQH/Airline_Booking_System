package com.group3.api.rest;

import com.group3.api.dto.PaymentResultRequest;
import com.group3.application.command.ProcessPaymentResultCommand;
import com.group3.application.port.in.ProcessPaymentResultUseCase;
import com.group3.infrastructure.security.InternalApiKeyValidator;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/internal/v1/bookings")
@RequiredArgsConstructor
public class InternalBookingController {

    private final ProcessPaymentResultUseCase processPaymentResultUseCase;
    private final InternalApiKeyValidator internalApiKeyValidator;

    @PostMapping("/{bookingId}/payment-result")
    public ResponseEntity<Void> processPaymentResult(
            @PathVariable("bookingId") UUID bookingId,

            @RequestHeader(
                    value = "X-Internal-Api-Key",
                    required = false
            )

            String internalApiKey,

            @Valid
            @RequestBody
            PaymentResultRequest request
            ){

        internalApiKeyValidator.validate(
                internalApiKey
        );

        ProcessPaymentResultCommand command =
                new ProcessPaymentResultCommand(
                        bookingId,
                        request.paymentId(),
                        request.status(),
                        request.amount(),
                        request.currency()
                );

        processPaymentResultUseCase.process(
                command);

        return ResponseEntity.noContent().build();
    }
}
