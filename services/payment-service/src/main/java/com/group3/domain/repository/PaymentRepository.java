package com.group3.domain.repository;

import com.group3.domain.aggregate.Payment;
import com.group3.domain.vo.PaymentId;

import java.util.Optional;

public interface PaymentRepository {

    Payment save(Payment payment);

    Optional<Payment> findById(PaymentId id);

    Optional<Payment> findByIdempotencyKey(String idempotencyKey);

    /**
     * Tìm payment theo tiền tố hex của UUID (không dấu '-'), dùng mã CK ngắn.
     */
    Optional<Payment> findByIdHexPrefix(String hexPrefix);
}
