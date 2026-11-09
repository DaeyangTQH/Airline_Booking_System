package com.group3.application.service;

import com.group3.application.command.CreatePaymentCommand;
import com.group3.application.port.in.CreatePaymentPort;
import com.group3.application.port.out.SePayGatewayPort;
import com.group3.application.result.CreatePaymentResult;
import com.group3.domain.aggregate.Payment;
import com.group3.domain.aggregate.PaymentMethod;
import com.group3.domain.aggregate.PaymentProvider;
import com.group3.domain.exception.InvalidDomainValueException;
import com.group3.domain.repository.PaymentRepository;
import com.group3.domain.vo.BookingId;
import com.group3.domain.vo.CurrencyCode;
import com.group3.domain.vo.IdempotencyKey;
import com.group3.domain.vo.Money;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class CreatePaymentService implements CreatePaymentPort {

    PaymentRepository paymentRepository;
    SePayGatewayPort sePayGatewayPort;

    @Override
    @Transactional
    public CreatePaymentResult create(CreatePaymentCommand command) {
        if (command == null) {
            throw new InvalidDomainValueException("Create payment command must not be null");
        }
        IdempotencyKey key = new IdempotencyKey(command.idempotencyKey());
        return paymentRepository.findByIdempotencyKey(key.value())
                .map(this::toResult)
                .orElseGet(() -> createNew(command, key));
    }

    private CreatePaymentResult createNew(
            CreatePaymentCommand command,
            IdempotencyKey key
    ) {
        PaymentMethod method = resolveMethod(command.method());
        CurrencyCode currency = CurrencyCode.valueOf(command.currency());
        Payment payment = Payment.create(
                new BookingId(command.bookingId()),
                new Money(command.amount(), currency),
                method,
                PaymentProvider.SEPAY,
                key
        );
        Payment saved = paymentRepository.save(payment);
        return toResult(saved);
    }

    private CreatePaymentResult toResult(Payment payment) {
        return new CreatePaymentResult(
                payment.getId().value(),
                payment.getStatus().name(),
                sePayGatewayPort.buildPayRedirectUrl(payment)
        );
    }

    private static PaymentMethod resolveMethod(String method) {
        if (method == null || method.isBlank()) {
            return PaymentMethod.SEPAY;
        }
        String normalized = method.trim().toUpperCase();
        if ("VNPAY".equals(normalized) || "SEPAY".equals(normalized)) {
            return PaymentMethod.SEPAY;
        }
        return PaymentMethod.valueOf(normalized);
    }
}
