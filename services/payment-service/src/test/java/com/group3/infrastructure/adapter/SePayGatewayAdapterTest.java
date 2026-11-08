package com.group3.infrastructure.adapter;

import com.group3.domain.aggregate.Payment;
import com.group3.domain.aggregate.PaymentMethod;
import com.group3.domain.aggregate.PaymentProvider;
import com.group3.domain.vo.BookingId;
import com.group3.domain.vo.CurrencyCode;
import com.group3.domain.vo.IdempotencyKey;
import com.group3.domain.vo.Money;
import com.group3.infrastructure.config.SePayProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SePayGatewayAdapterTest {

    SePayProperties properties;
    SePayGatewayAdapter adapter;

    @BeforeEach
    void setUp() {
        properties = new SePayProperties();
        properties.setAccountNumber("SBPAY7605943182");
        properties.setBankCode("ACMEBank");
        properties.setQrBaseUrl("https://qr.sepay.vn/img");
        properties.setQrTemplate("compact");
        properties.setCheckoutBaseUrl("http://localhost:8084");
        properties.setPaymentCodePrefix("AIR");
        properties.setPaymentCodeSuffixLength(12);
        adapter = new SePayGatewayAdapter(properties);
    }

    @Test
    void acmeBankIsNotVietQrSupported() {
        assertFalse(adapter.isVietQrSupported());
    }

    @Test
    void checkoutUrlPointsToLocalPage() {
        Payment payment = payment();
        String url = adapter.buildCheckoutUrl(payment);
        assertEquals(
                "http://localhost:8084/api/v1/payments/" + payment.getId().value() + "/checkout",
                url
        );
    }

    @Test
    void qrFallbackUsesQrServerForAcmeBank() {
        Payment payment = payment();
        String url = adapter.buildQrImageUrl(payment);
        assertTrue(url.startsWith("https://api.qrserver.com/v1/create-qr-code/"));
        assertTrue(url.contains("ACMEBank") || url.toLowerCase().contains("acmebank") || url.contains("data="));
    }

    @Test
    void paymentCodeIsShortPrefixedHex() {
        Payment payment = payment();
        String code = adapter.paymentCode(payment);
        assertTrue(code.startsWith("AIR"));
        assertEquals(15, code.length());
        assertEquals(
                payment.getId().value().toString().replace("-", "").substring(0, 12).toUpperCase(),
                code.substring(3)
        );
    }

    @Test
    void extractSuffixFromTransferContent() {
        Payment payment = payment();
        String content = "NGUYEN VAN A " + adapter.paymentCode(payment) + " chuyen tien";
        assertEquals(
                adapter.paymentCode(payment).substring(3),
                adapter.extractCodeSuffix(content).orElseThrow()
        );
    }

    private Payment payment() {
        return Payment.create(
                new BookingId(UUID.randomUUID()),
                new Money(new BigDecimal("150000"), CurrencyCode.VND),
                PaymentMethod.SEPAY,
                PaymentProvider.SEPAY,
                new IdempotencyKey("key-1")
        );
    }
}
