package com.group3.domain.aggregate;

import com.group3.domain.exception.InvalidDomainValueException;
import com.group3.domain.vo.IdempotencyKey;
import com.group3.domain.vo.Money;
import com.group3.domain.vo.ProviderRefundId;
import com.group3.domain.vo.RefundId;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class Refund {

    RefundId id;
    Money amount;
    IdempotencyKey idempotencyKey;
    RefundStatus status;
    ProviderRefundId providerRefundId;

    static Refund create(
            RefundId id,
            Money amount,
            IdempotencyKey idempotencyKey,
            ProviderRefundId providerRefundId
    ) {
        if (id == null) {
            throw new InvalidDomainValueException(
                    "Refund ID must not be null"
            );
        }

        if (amount == null) {
            throw new InvalidDomainValueException(
                    "Refund amount must not be null"
            );
        }

        if (idempotencyKey == null) {
            throw new InvalidDomainValueException(
                    "Refund idempotency key must not be null"
            );
        }

        if (providerRefundId == null) {
            throw new InvalidDomainValueException(
                    "Provider refund ID must not be null"
            );
        }

        return new Refund(
                id,
                amount,
                idempotencyKey,
                RefundStatus.COMPLETED,
                providerRefundId
        );
    }
}
