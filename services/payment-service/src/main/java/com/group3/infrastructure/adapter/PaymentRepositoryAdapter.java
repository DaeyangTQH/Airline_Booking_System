package com.group3.infrastructure.adapter;

import com.group3.domain.aggregate.Payment;
import com.group3.domain.repository.PaymentRepository;
import com.group3.domain.vo.PaymentId;
import com.group3.infrastructure.persistence.mapper.PaymentPersistenceMapper;
import com.group3.infrastructure.persistence.repository.PaymentJpaRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PaymentRepositoryAdapter implements PaymentRepository {

    PaymentPersistenceMapper paymentPersistenceMapper;
    PaymentJpaRepository paymentJpaRepository;

    @Override
    @Transactional
    public Payment save(Payment payment) {
        return paymentPersistenceMapper.toDomain(
                paymentJpaRepository.save(paymentPersistenceMapper.toEntity(payment))
        );
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Payment> findById(PaymentId id) {
        return paymentJpaRepository.findById(id.value())
                .map(paymentPersistenceMapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Payment> findByIdempotencyKey(String idempotencyKey) {
        return paymentJpaRepository.findByIdempotencyKey(idempotencyKey)
                .map(paymentPersistenceMapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Payment> findByIdHexPrefix(String hexPrefix) {
        if (hexPrefix == null || hexPrefix.isBlank()) {
            return Optional.empty();
        }
        return paymentJpaRepository.findFirstByIdHexPrefix(hexPrefix.trim())
                .map(paymentPersistenceMapper::toDomain);
    }
}
