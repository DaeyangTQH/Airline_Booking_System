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
import java.util.LinkedHashMap;
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
        properties.setMerchantId("SP-TEST-MERCHANT");
        properties.setSecretKey("spsk_test_dummy_secret");
        properties.setCheckoutInitUrl("https://pay-sandbox.sepay.vn/v1/checkout/init");
        properties.setCallbackBaseUrl("http://localhost:8080");
        properties.setFrontendReturnUrl("http://localhost:5173/payment-return");
        properties.setPaymentMethod("BANK_TRANSFER");
        adapter = new SePayGatewayAdapter(properties);
    }

    @Test
    void payRedirectUrlPointsToBridge() {
        Payment payment = payment();
        String url = adapter.buildPayRedirectUrl(payment);
        assertEquals(
                "http://localhost:8080/api/v1/payments/" + payment.getId().value() + "/pay",
                url
        );
    }

    @Test
    void invoiceNumberIsPaymentId() {
        Payment payment = payment();
        assertEquals(payment.getId().value().toString(), adapter.invoiceNumber(payment));
    }

    @Test
    void checkoutFormUsesNodeStyleFieldOrder() {
        Payment payment = payment();
        LinkedHashMap<String, String> fields = adapter.buildCheckoutFormFields(payment);

        assertEquals(
                java.util.List.of(
                        "merchant",
                        "operation",
                        "payment_method",
                        "order_invoice_number",
                        "order_amount",
                        "currency",
                        "order_description",
                        "success_url",
                        "error_url",
                        "cancel_url",
                        "lang",
                        "signature"
                ),
                java.util.List.copyOf(fields.keySet())
        );
        assertEquals("BANK_TRANSFER", fields.get("payment_method"));
        assertEquals("150000", fields.get("order_amount"));
        assertTrue(fields.get("order_invoice_number").startsWith(payment.getId().value() + "-"));
        String paymentId = payment.getId().value().toString();
        assertEquals(
                "http://localhost:8080/api/v1/payments/sepay/return/success/" + paymentId,
                fields.get("success_url")
        );
        assertEquals(
                "http://localhost:8080/api/v1/payments/sepay/return/error/" + paymentId,
                fields.get("error_url")
        );
        assertEquals(
                "http://localhost:8080/api/v1/payments/sepay/return/cancel/" + paymentId,
                fields.get("cancel_url")
        );
        assertTrue(fields.containsKey("signature"));
        assertFalse(fields.get("signature").isBlank());
    }

    @Test
    void eachCheckoutGetsUniqueInvoiceNumber() throws Exception {
        Payment payment = payment();
        String a = adapter.buildCheckoutFormFields(payment).get("order_invoice_number");
        Thread.sleep(1);
        String b = adapter.buildCheckoutFormFields(payment).get("order_invoice_number");
        assertTrue(a.startsWith(payment.getId().value() + "-"));
        assertTrue(b.startsWith(payment.getId().value() + "-"));
        assertFalse(a.equals(b));
    }

    @Test
    void signatureMatchesNodeSdkFieldOrder() {
        Payment payment = payment();
        LinkedHashMap<String, String> fields = adapter.buildCheckoutFormFields(payment);
        assertEquals(
                adapter.signFields(fieldsWithoutSignature(fields)),
                fields.get("signature")
        );
        assertTrue(fields.get("signature").length() > 10);
    }

    private static LinkedHashMap<String, String> fieldsWithoutSignature(LinkedHashMap<String, String> fields) {
        LinkedHashMap<String, String> copy = new LinkedHashMap<>(fields);
        copy.remove("signature");
        return copy;
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
