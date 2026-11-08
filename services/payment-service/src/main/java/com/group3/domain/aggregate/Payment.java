package com.group3.domain.aggregate;

import com.group3.domain.exception.InvalidDomainValueException;
import com.group3.domain.exception.InvalidPaymentStateException;
import com.group3.domain.exception.RefundNotAllowedException;
import com.group3.domain.vo.BookingId;
import com.group3.domain.vo.IdempotencyKey;
import com.group3.domain.vo.Money;
import com.group3.domain.vo.PaymentId;
import com.group3.domain.vo.ProviderRefundId;
import com.group3.domain.vo.ProviderTxnId;
import com.group3.domain.vo.RefundId;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.util.ArrayList;
import java.util.List;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Payment {

    final PaymentId id;
    final BookingId bookingId;
    final Money amount;
    final PaymentMethod method;
    final PaymentProvider provider;
    final IdempotencyKey idempotencyKey;
    final List<Refund> refunds = new ArrayList<>();

    @Setter(AccessLevel.PRIVATE)
    PaymentStatus status = PaymentStatus.PENDING;

    @Setter(AccessLevel.PRIVATE)
    ProviderTxnId providerTxnId;

    public static Payment restore(
            PaymentId id,
            BookingId bookingId,
            Money amount,
            PaymentMethod method,
            PaymentProvider provider,
            IdempotencyKey idempotencyKey,
            PaymentStatus status,
            ProviderTxnId providerTxnId
    ) {
        if (id == null) {
            throw new InvalidDomainValueException("Payment ID must not be null");
        }
        if (status == null) {
            throw new InvalidDomainValueException("Payment status must not be null");
        }
        Payment payment = new Payment(
                id, bookingId, amount, method, provider, idempotencyKey
        );
        payment.setStatus(status);
        payment.setProviderTxnId(providerTxnId);
        return payment;
    }

    public static Payment create(
            BookingId bookingId,
            Money amount,
            PaymentMethod method,
            PaymentProvider provider,
            IdempotencyKey idempotencyKey
    ) {
        if (bookingId == null) {
            throw new InvalidDomainValueException(
                    "Booking ID must not be null"
            );
        }

        if (amount == null) {
            throw new InvalidDomainValueException(
                    "Payment amount must not be null"
            );
        }

        if (method == null) {
            throw new InvalidDomainValueException(
                    "Payment method must not be null"
            );
        }

        if (provider == null) {
            throw new InvalidDomainValueException(
                    "Payment provider must not be null"
            );
        }

        if (idempotencyKey == null) {
            throw new InvalidDomainValueException(
                    "Idempotency key must not be null"
            );
        }

        return new Payment(
                PaymentId.generate(),
                bookingId,
                amount,
                method,
                provider,
                idempotencyKey
        );
    }

    public void complete(ProviderTxnId providerTxnId) {
        if (status == PaymentStatus.COMPLETED) {
            return;
        }

        if (status != PaymentStatus.PENDING) {
            throw new InvalidPaymentStateException(
                    "Cannot complete a payment in status " + status
            );
        }

        if (providerTxnId == null) {
            throw new InvalidDomainValueException(
                    "Provider transaction ID must not be null"
            );
        }

        setProviderTxnId(providerTxnId);
        setStatus(PaymentStatus.COMPLETED);
    }

    public void fail() {
        if (status == PaymentStatus.FAILED) {
            return;
        }

        if (status != PaymentStatus.PENDING) {
            throw new InvalidPaymentStateException(
                    "Cannot fail a payment in status " + status
            );
        }

        setStatus(PaymentStatus.FAILED);
    }

    public void cancel() {
        if (status == PaymentStatus.CANCELLED) {
            return;
        }

        if (status != PaymentStatus.PENDING) {
            throw new InvalidPaymentStateException(
                    "Cannot cancel a payment in status " + status
            );
        }

        setStatus(PaymentStatus.CANCELLED);
    }

    public Refund refund(
            Money refundAmount,
            IdempotencyKey refundKey,
            ProviderRefundId providerRefundId
    ) {
        if (status != PaymentStatus.COMPLETED) {
            throw new RefundNotAllowedException(
                    "Refund is only allowed for a completed payment"
            );
        }

        if (refundAmount == null) {
            throw new InvalidDomainValueException(
                    "Refund amount must not be null"
            );
        }

        if (refundKey == null) {
            throw new InvalidDomainValueException(
                    "Refund idempotency key must not be null"
            );
        }

        Refund existing = findRefundByKey(refundKey);
        if (existing != null) {
            return existing;
        }

        if (!amount.currency().equals(refundAmount.currency())) {
            throw new RefundNotAllowedException(
                    "Refund currency must match payment currency"
            );
        }

        Money alreadyRefunded = refundedTotal();
        Money nextTotal = alreadyRefunded == null
                ? refundAmount
                : alreadyRefunded.add(refundAmount);
        if (nextTotal.isGreaterThan(amount)) {
            throw new RefundNotAllowedException(
                    "Total refund cannot exceed payment amount"
            );
        }

        Refund refund = Refund.create(
                RefundId.generate(),
                refundAmount,
                refundKey,
                providerRefundId
        );
        refunds.add(refund);
        return refund;
    }

    public List<Refund> getRefunds() {
        return List.copyOf(refunds);
    }

    private Refund findRefundByKey(IdempotencyKey refundKey) {
        return refunds.stream()
                .filter(refund -> refund.getIdempotencyKey().equals(refundKey))
                .findFirst()
                .orElse(null);
    }

    private Money refundedTotal() {
        return refunds.stream()
                .filter(refund -> refund.getStatus() == RefundStatus.COMPLETED)
                .map(Refund::getAmount)
                .reduce(Money::add)
                .orElse(null);
    }
}
