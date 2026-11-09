package com.group3.application.service;

import com.group3.application.port.out.SePayGatewayPort;
import com.group3.application.result.PayCheckoutPageResult;
import com.group3.domain.aggregate.Payment;
import com.group3.domain.aggregate.PaymentMethod;
import com.group3.domain.aggregate.PaymentProvider;
import com.group3.domain.aggregate.PaymentStatus;
import com.group3.domain.exception.InvalidDomainValueException;
import com.group3.domain.repository.PaymentRepository;
import com.group3.domain.vo.BookingId;
import com.group3.domain.vo.CurrencyCode;
import com.group3.domain.vo.IdempotencyKey;
import com.group3.domain.vo.Money;
import com.group3.domain.vo.PaymentId;
import com.group3.domain.vo.ProviderTxnId;
import com.group3.infrastructure.config.SePayProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetPayCheckoutPageServiceTest {

    @Mock
    PaymentRepository paymentRepository;

    @Mock
    SePayGatewayPort sePayGatewayPort;

    SePayProperties properties;
    GetPayCheckoutPageService service;

    @BeforeEach
    void setUp() {
        properties = new SePayProperties();
        properties.setFrontendReturnUrl("http://localhost:5173/payment-return");
        service = new GetPayCheckoutPageService(paymentRepository, sePayGatewayPort, properties);
    }

    private Payment createPayment() {
        return Payment.create(
                new BookingId(UUID.randomUUID()),
                new Money(new BigDecimal("1500000"), CurrencyCode.VND),
                PaymentMethod.SEPAY,
                PaymentProvider.SEPAY,
                new IdempotencyKey("test-key-" + UUID.randomUUID())
        );
    }

    @Test
    void getCheckoutPage_throws_whenPaymentIdNull() {
        assertThrows(InvalidDomainValueException.class, () -> service.getCheckoutPage(null));
    }

    @Test
    void getCheckoutPage_throws_whenPaymentNotFound() {
        UUID id = UUID.randomUUID();
        when(paymentRepository.findById(new PaymentId(id))).thenReturn(Optional.empty());

        assertThrows(InvalidDomainValueException.class, () -> service.getCheckoutPage(id));
    }

    @Test
    void getCheckoutPage_returnsAlreadyPaid_whenStatusCompleted() {
        Payment payment = createPayment();
        payment.complete(new ProviderTxnId("TXN123"));
        UUID id = payment.getId().value();

        when(paymentRepository.findById(new PaymentId(id))).thenReturn(Optional.of(payment));

        PayCheckoutPageResult result = service.getCheckoutPage(id);
        assertTrue(result.alreadyPaid());
        assertTrue(result.redirectUrl().contains("status=SUCCESS"));
        assertTrue(result.redirectUrl().contains("invoice=" + id));
    }

    @Test
    void getCheckoutPage_returnsForm_whenStatusPending() {
        Payment payment = createPayment();
        UUID id = payment.getId().value();

        LinkedHashMap<String, String> fields = new LinkedHashMap<>();
        fields.put("merchant", "MERCHANT_123");
        fields.put("amount", "1500000");

        when(paymentRepository.findById(new PaymentId(id))).thenReturn(Optional.of(payment));
        when(sePayGatewayPort.buildCheckoutFormFields(payment)).thenReturn(fields);
        when(sePayGatewayPort.checkoutInitUrl()).thenReturn("https://pay-sandbox.sepay.vn/v1/checkout/init");

        PayCheckoutPageResult result = service.getCheckoutPage(id);
        assertFalse(result.alreadyPaid());
        assertEquals("https://pay-sandbox.sepay.vn/v1/checkout/init", result.formAction());
        assertEquals("MERCHANT_123", result.formFields().get("merchant"));
    }
}
