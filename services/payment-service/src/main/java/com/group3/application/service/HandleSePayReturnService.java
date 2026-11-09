package com.group3.application.service;

import com.group3.application.command.HandleSePayReturnCommand;
import com.group3.application.exception.BookingCallbackException;
import com.group3.application.port.in.HandleSePayReturnPort;
import com.group3.application.port.out.BookingCallbackPort;
import com.group3.domain.aggregate.Payment;
import com.group3.domain.aggregate.PaymentStatus;
import com.group3.domain.repository.PaymentRepository;
import com.group3.domain.vo.PaymentId;
import com.group3.domain.vo.ProviderTxnId;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Hoàn tất payment khi SePay redirect success_url (không dùng IPN).
 */
@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class HandleSePayReturnService implements HandleSePayReturnPort {

    PaymentRepository paymentRepository;
    BookingCallbackPort bookingCallbackPort;

    @Override
    @Transactional
    public boolean handleSuccess(HandleSePayReturnCommand command) {
        if (command == null || command.orderInvoiceNumber() == null || command.orderInvoiceNumber().isBlank()) {
            log.warn("SePay success return missing payment/invoice id");
            return false;
        }

        UUID paymentId = parsePaymentId(command.orderInvoiceNumber());
        if (paymentId == null) {
            log.warn("SePay success return invalid invoice={}", command.orderInvoiceNumber());
            return false;
        }

        Payment payment = paymentRepository.findById(new PaymentId(paymentId)).orElse(null);
        if (payment == null) {
            log.warn("SePay success return payment not found id={}", paymentId);
            return false;
        }

        if (payment.getStatus() == PaymentStatus.COMPLETED) {
            notifyBooking(payment);
            return true;
        }
        if (payment.getStatus() != PaymentStatus.PENDING) {
            log.warn("SePay success return payment {} status={}", paymentId, payment.getStatus());
            return false;
        }

        String txn = command.transactionId();
        if (txn == null || txn.isBlank()) {
            txn = command.orderId();
        }
        if (txn == null || txn.isBlank()) {
            txn = "SEPAY-" + paymentId;
        }

        payment.complete(new ProviderTxnId(txn));
        Payment saved = paymentRepository.save(payment);
        notifyBooking(saved);
        log.info("Payment {} marked COMPLETED, booking={}", paymentId, saved.getBookingId().value());
        return true;
    }

    private void notifyBooking(Payment payment) {
        try {
            bookingCallbackPort.notifyPaymentCompleted(payment);
        } catch (BookingCallbackException ex) {
            log.error(
                    "Failed to notify booking-service for payment {} booking {}: {}",
                    payment.getId().value(),
                    payment.getBookingId().value(),
                    ex.getMessage()
            );
        }
    }

    /**
     * order_invoice_number = {@code {paymentUuid}} hoặc {@code {paymentUuid}-{suffix}}.
     * payment_id callback = {@code {paymentUuid}}.
     */
    static UUID parsePaymentId(String orderInvoiceNumber) {
        String raw = orderInvoiceNumber.trim();
        try {
            if (raw.length() > 36 && raw.charAt(36) == '-') {
                return UUID.fromString(raw.substring(0, 36));
            }
            return UUID.fromString(raw);
        } catch (Exception ex) {
            return null;
        }
    }
}
