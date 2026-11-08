package com.group3.application.service;

import com.group3.application.port.in.GetPaymentStatusPort;
import com.group3.application.port.out.SePayGatewayPort;
import com.group3.application.result.PaymentStatusResult;
import com.group3.domain.aggregate.Payment;
import com.group3.domain.exception.InvalidDomainValueException;
import com.group3.domain.repository.PaymentRepository;
import com.group3.domain.vo.PaymentId;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class GetPaymentStatusService implements GetPaymentStatusPort {

    PaymentRepository paymentRepository;
    SePayGatewayPort sePayGatewayPort;

    @Override
    @Transactional(readOnly = true)
    public PaymentStatusResult get(UUID paymentId) {
        if (paymentId == null) {
            throw new InvalidDomainValueException("Payment id must not be null");
        }
        Payment payment = paymentRepository.findById(new PaymentId(paymentId))
                .orElseThrow(() -> new InvalidDomainValueException("Payment not found"));
        return new PaymentStatusResult(
                payment.getId().value(),
                payment.getStatus().name(),
                sePayGatewayPort.paymentCode(payment),
                sePayGatewayPort.buildCheckoutUrl(payment)
        );
    }
}
