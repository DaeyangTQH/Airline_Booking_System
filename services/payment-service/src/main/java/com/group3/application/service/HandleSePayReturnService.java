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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Hoàn tất payment khi SePay redirect success_url (không dùng IPN).
 */
@Service
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class HandleSePayReturnService implements HandleSePayReturnPort {

    PaymentRepository paymentRepository;
    BookingCallbackPort bookingCallbackPort;

    @Override
    @Transactional
    public void handleSuccess(HandleSePayReturnCommand command) {
        if (command == null || command.orderInvoiceNumber() == null || command.orderInvoiceNumber().isBlank()) {
            return;
        }

        UUID paymentId = parsePaymentId(command.orderInvoiceNumber());
        if (paymentId == null) {
            return;
        }

        Payment payment = paymentRepository.findById(new PaymentId(paymentId)).orElse(null);
        if (payment == null) {
            return;
        }

        if (payment.getStatus() == PaymentStatus.COMPLETED) {
            try {
                bookingCallbackPort.notifyPaymentCompleted(payment);
            } catch (BookingCallbackException ignored) {
            }
            return;
        }
        if (payment.getStatus() != PaymentStatus.PENDING) {
            return;
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
        try {
            bookingCallbackPort.notifyPaymentCompleted(saved);
        } catch (BookingCallbackException ignored) {
        }
    }

    /**
     * order_invoice_number = {@code {paymentUuid}} hoặc {@code {paymentUuid}-{epochMillis}}.
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
