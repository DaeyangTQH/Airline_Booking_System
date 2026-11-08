package com.group3.infrastructure.persistence.mapper;

import com.group3.domain.aggregate.Payment;
import com.group3.domain.vo.*;
import com.group3.infrastructure.persistence.entity.PaymentEntity;
import org.mapstruct.Mapper;
import org.springframework.stereotype.Component;

@Component
public class PaymentPersistenceMapper {
    public PaymentEntity toEntity(Payment payment) {
        PaymentEntity entity = new PaymentEntity();
        entity.setId(payment.getId().value());
        entity.setBookingId(payment.getBookingId().value());
        entity.setAmount(payment.getAmount().amount());
        entity.setCurrency(payment.getAmount().currency());
        entity.setMethod(payment.getMethod());
        entity.setStatus(payment.getStatus());
        entity.setProvider(payment.getProvider());
        entity.setIdempotencyKey(payment.getIdempotencyKey().value());
        if (payment.getProviderTxnId() != null) {
            entity.setProviderTxnId(payment.getProviderTxnId().value());
        }
        return entity;
    }
    public Payment toDomain(PaymentEntity entity) {
        ProviderTxnId txnId = entity.getProviderTxnId() == null
                || entity.getProviderTxnId().isBlank()
                ? null
                : new ProviderTxnId(entity.getProviderTxnId());
        return Payment.restore(
                new PaymentId(entity.getId()),
                new BookingId(entity.getBookingId()),
                new Money(entity.getAmount(), entity.getCurrency()),
                entity.getMethod(),
                entity.getProvider(),
                new IdempotencyKey(entity.getIdempotencyKey()),
                entity.getStatus(),
                txnId
        );
    }
}
