package com.group3.application.service;

import com.group3.application.command.ProcessPaymentResultCommand;
import com.group3.application.integration.NotificationEvent;
import com.group3.application.integration.NotificationEventType;
import com.group3.application.integration.PaymentResultStatus;
import com.group3.application.port.in.ProcessPaymentResultUseCase;
import com.group3.application.port.out.NotificationEventPublisher;
import com.group3.domain.aggregate.Booking;
import com.group3.domain.aggregate.BookingStatus;
import com.group3.domain.exception.BookingNotFoundException;
import com.group3.domain.exception.InvalidDomainValueException;
import com.group3.domain.repository.BookingRepository;
import com.group3.domain.vo.BookingId;
import com.group3.domain.vo.Money;
import com.group3.domain.vo.PaymentId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProcessPaymentResultService implements ProcessPaymentResultUseCase {

    private final BookingRepository bookingRepository;
    private final NotificationEventPublisher notificationEventPublisher;

    @Override
    @Transactional
    public void process(ProcessPaymentResultCommand command) {

        if (command == null) {
            throw new InvalidDomainValueException(
                    "Payment result command must not be null"
            );
        }

        if(command.status() == null) {
            throw new InvalidDomainValueException(
                    "Payment result status must not be null"
            );
        }

        BookingId bookingId =
                new BookingId(command.bookingId());

        PaymentId paymentId =
                new PaymentId(command.paymentId());

        Money paidAmount = new Money(
                command.amount(),
                command.currency()
        );

        Booking booking = bookingRepository
                .findByIdForUpdate(bookingId)
                .orElseThrow(() ->
                        new BookingNotFoundException(
                                "Booking not found with ID: " + bookingId.value()
                        ));

        if (command.status()
                == PaymentResultStatus.COMPLETED) {

            boolean newlyConfirmed =
                    booking.getStatus()
                            == BookingStatus.PENDING;

            booking.confirmPayment(
                    paymentId,
                    paidAmount,
                    Instant.now()
            );

            bookingRepository.save(booking);

            if (newlyConfirmed) {
                notificationEventPublisher.publish(
                        createBookingConfirmedEvent(booking)
                );
            }
        }
    }

    private NotificationEvent createBookingConfirmedEvent(Booking booking) {
        return new NotificationEvent(
                UUID.randomUUID(),
                booking.getUserId().value(),
                booking.getContactInfo().email(),
                NotificationEventType.BOOKING_CONFIRMED,
                "Booking confirmed",
                "Payment was successful. Booking "
                        + booking.getReference().value()
                        + " has been confirmed.",
                booking.getId().value(),
                null,
                Instant.now()
        );
    }
}
