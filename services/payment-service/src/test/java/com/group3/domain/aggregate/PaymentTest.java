package com.group3.domain.aggregate;

import com.group3.domain.exception.InvalidDomainValueException;
import com.group3.domain.exception.InvalidPaymentStateException;
import com.group3.domain.exception.RefundNotAllowedException;
import com.group3.domain.vo.BookingId;
import com.group3.domain.vo.CurrencyCode;
import com.group3.domain.vo.IdempotencyKey;
import com.group3.domain.vo.Money;
import com.group3.domain.vo.ProviderRefundId;
import com.group3.domain.vo.ProviderTxnId;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PaymentTest {

    @Test
    void createPaymentStartsAsPending() {
        Payment payment = newPayment();

        assertEquals(PaymentStatus.PENDING, payment.getStatus());
        assertEquals(PaymentProvider.SEPAY, payment.getProvider());
        assertEquals(PaymentMethod.SEPAY, payment.getMethod());
        assertNull(payment.getProviderTxnId());
    }

    @Test
    void createRejectsNonPositiveAmount() {
        assertThrows(
                InvalidDomainValueException.class,
                () -> new Money(BigDecimal.ZERO, CurrencyCode.VND)
        );
    }

    @Test
    void completePendingPayment() {
        Payment payment = newPayment();
        ProviderTxnId txnId = new ProviderTxnId("13925464");

        payment.complete(txnId);

        assertEquals(PaymentStatus.COMPLETED, payment.getStatus());
        assertEquals(txnId, payment.getProviderTxnId());
    }

    @Test
    void completeTwiceIsIdempotent() {
        Payment payment = completedPayment();
        ProviderTxnId firstTxn = payment.getProviderTxnId();

        payment.complete(new ProviderTxnId("another-txn"));

        assertEquals(PaymentStatus.COMPLETED, payment.getStatus());
        assertEquals(firstTxn, payment.getProviderTxnId());
    }

    @Test
    void failPendingPayment() {
        Payment payment = newPayment();

        payment.fail();

        assertEquals(PaymentStatus.FAILED, payment.getStatus());
    }

    @Test
    void cannotCompleteFailedPayment() {
        Payment payment = newPayment();
        payment.fail();

        assertThrows(
                InvalidPaymentStateException.class,
                () -> payment.complete(new ProviderTxnId("13925464"))
        );
    }

    @Test
    void cannotCompleteCancelledPayment() {
        Payment payment = newPayment();
        payment.cancel();

        assertThrows(
                InvalidPaymentStateException.class,
                () -> payment.complete(new ProviderTxnId("13925464"))
        );
    }

    @Test
    void cannotRefundPendingPayment() {
        Payment payment = newPayment();

        assertThrows(
                RefundNotAllowedException.class,
                () -> payment.refund(
                        vnd("100000"),
                        new IdempotencyKey("refund-1"),
                        new ProviderRefundId("REF-1")
                )
        );
    }

    @Test
    void refundCompletedPayment() {
        Payment payment = completedPayment();

        Refund refund = payment.refund(
                vnd("500000"),
                new IdempotencyKey("refund-1"),
                new ProviderRefundId("REF-1")
        );

        assertEquals(RefundStatus.COMPLETED, refund.getStatus());
        assertEquals(1, payment.getRefunds().size());
        assertEquals(PaymentStatus.COMPLETED, payment.getStatus());
    }

    @Test
    void refundWithSameIdempotencyKeyReturnsExisting() {
        Payment payment = completedPayment();
        IdempotencyKey key = new IdempotencyKey("refund-1");

        Refund first = payment.refund(vnd("500000"), key, new ProviderRefundId("REF-1"));
        Refund second = payment.refund(vnd("500000"), key, new ProviderRefundId("REF-1"));

        assertSame(first, second);
        assertEquals(1, payment.getRefunds().size());
    }

    @Test
    void refundCannotExceedPaymentAmount() {
        Payment payment = completedPayment();
        payment.refund(
                vnd("2000000"),
                new IdempotencyKey("refund-1"),
                new ProviderRefundId("REF-1")
        );

        assertThrows(
                RefundNotAllowedException.class,
                () -> payment.refund(
                        vnd("1500000"),
                        new IdempotencyKey("refund-2"),
                        new ProviderRefundId("REF-2")
                )
        );
    }

    @Test
    void refundRejectsDifferentCurrency() {
        Payment payment = completedPayment();

        assertThrows(
                RefundNotAllowedException.class,
                () -> payment.refund(
                        new Money(new BigDecimal("10"), CurrencyCode.USD),
                        new IdempotencyKey("refund-1"),
                        new ProviderRefundId("REF-1")
                )
        );
    }

    private Payment completedPayment() {
        Payment payment = newPayment();
        payment.complete(new ProviderTxnId("13925464"));
        return payment;
    }

    private Payment newPayment() {
        return Payment.create(
                new BookingId(UUID.randomUUID()),
                vnd("3000000"),
                PaymentMethod.SEPAY,
                PaymentProvider.SEPAY,
                new IdempotencyKey("booking-pay-1")
        );
    }

    private Money vnd(String amount) {
        return new Money(new BigDecimal(amount), CurrencyCode.VND);
    }
}
