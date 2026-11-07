package com.group3.application.service;

import com.group3.application.command.InitiatePaymentCommand;
import com.group3.application.exception.BookingAccessDeniedException;
import com.group3.application.integration.PaymentSession;
import com.group3.application.port.in.InitiatePaymentUseCase;
import com.group3.application.port.out.PaymentPort;
import com.group3.application.result.InitiatePaymentResult;
import com.group3.domain.aggregate.Booking;
import com.group3.domain.exception.BookingNotFoundException;
import com.group3.domain.exception.InvalidDomainValueException;
import com.group3.domain.repository.BookingRepository;
import com.group3.domain.vo.BookingId;
import com.group3.domain.vo.PaymentId;
import com.group3.domain.vo.UserId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class InitiatePaymentService implements InitiatePaymentUseCase {

    private static final String IDEMPOTENCY_KEY_PREFIX = "booking-payment:";
    private final BookingRepository bookingRepository;
    private final PaymentPort paymentPort;
    private final BookingPaymentTransactionService bookingPaymentTransactionService;

    @Override
    public InitiatePaymentResult initiate(InitiatePaymentCommand command) {

        if(command == null) {
            throw new InvalidDomainValueException(
                    "Initiate payment command must not be null"
            );
        }

        BookingId bookingId
                = new BookingId(command.bookingId());

        UserId userId =
                new UserId(command.userId());

        Booking booking = bookingRepository
                .findById(bookingId)
                .orElseThrow(() ->
                        new BookingNotFoundException(
                                "Booking not found with ID: " + bookingId.value()
                        ));

        verifyOwnership(booking, userId);

        booking.ensureCanInitiatePayment(
                Instant.now()
        );

        String idempotencyKey = IDEMPOTENCY_KEY_PREFIX + bookingId.value();

        PaymentSession paymentSession =
                paymentPort.initiatePayment(
                        booking.getId(),
                        booking.getTotalAmount(),
                        idempotencyKey
                );
        bookingPaymentTransactionService.attachPaymentToBooking(
                bookingId,
                userId,
                new PaymentId(
                        paymentSession.paymentId()
                )
        );

        return new InitiatePaymentResult(
                paymentSession.paymentId(),
                paymentSession.status(),
                paymentSession.paymentUrl()
        );
    }

    private void verifyOwnership(
            Booking booking,
            UserId userId
    ){
        if(!booking.getUserId().equals(userId)){
            throw new BookingAccessDeniedException(
                    "You do not have permission to pay for this booking"
            );
        }
    }
}
