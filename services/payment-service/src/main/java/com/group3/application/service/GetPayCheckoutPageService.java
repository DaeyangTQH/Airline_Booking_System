package com.group3.application.service;

import com.group3.application.port.in.GetPayCheckoutPagePort;
import com.group3.application.port.out.SePayGatewayPort;
import com.group3.application.result.PayCheckoutPageResult;
import com.group3.domain.aggregate.Payment;
import com.group3.domain.aggregate.PaymentStatus;
import com.group3.domain.exception.InvalidDomainValueException;
import com.group3.domain.repository.PaymentRepository;
import com.group3.domain.vo.PaymentId;
import com.group3.infrastructure.config.SePayProperties;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class GetPayCheckoutPageService implements GetPayCheckoutPagePort {

    PaymentRepository paymentRepository;
    SePayGatewayPort sePayGatewayPort;
    SePayProperties sePayProperties;

    @Override
    @Transactional(readOnly = true)
    public PayCheckoutPageResult getCheckoutPage(UUID paymentId) {
        if (paymentId == null) {
            throw new InvalidDomainValueException("Payment ID must not be null");
        }

        Payment payment = paymentRepository.findById(new PaymentId(paymentId))
                .orElseThrow(() -> new InvalidDomainValueException("Payment not found"));

        if (payment.getStatus() == PaymentStatus.COMPLETED) {
            String frontend = sePayProperties.getFrontendReturnUrl();
            if (frontend == null || frontend.isBlank()) {
                frontend = "http://localhost:5173/payment-return";
            }
            String redirectUrl = frontend + "?status=SUCCESS&invoice=" + paymentId;
            return PayCheckoutPageResult.alreadyPaid(redirectUrl);
        }

        if (payment.getStatus() != PaymentStatus.PENDING) {
            throw new InvalidDomainValueException(
                    "Payment is not payable in status " + payment.getStatus()
            );
        }

        LinkedHashMap<String, String> fields = sePayGatewayPort.buildCheckoutFormFields(payment);
        String formAction = sePayGatewayPort.checkoutInitUrl();

        return PayCheckoutPageResult.checkout(formAction, fields);
    }
}
