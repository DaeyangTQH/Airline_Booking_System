package com.group3.application.service;

import com.group3.application.command.CancelBookingCommand;
import com.group3.application.exception.BookingAccessDeniedException;
import com.group3.application.port.in.CancelBookingUseCase;
import com.group3.domain.aggregate.Booking;
import com.group3.domain.exception.BookingNotFoundException;
import com.group3.domain.exception.InvalidDomainValueException;
import com.group3.domain.repository.BookingRepository;
import com.group3.domain.vo.BookingId;
import com.group3.domain.vo.UserId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CancelBookingService implements CancelBookingUseCase {

    private final BookingRepository bookingRepository;

    @Override
    @Transactional
    public void cancel(CancelBookingCommand command) {

        if(command == null){
            throw new InvalidDomainValueException(
                    "Cancel booking command must not be null"
            );
        }

        BookingId bookingId = new BookingId(command.bookingId());
        UserId userId = new UserId(command.userId());

        Booking booking = bookingRepository
                .findByIdForUpdate(bookingId)
                .orElseThrow(() ->
                        new BookingNotFoundException(
                                "Booking not found with ID: " + bookingId.value()
                        ));

        if(!booking.getUserId().equals(userId)){
            throw new BookingAccessDeniedException(
                    "You do not have permission to cancel this booking"
            );
        }

        booking.cancel();
        bookingRepository.save(booking);
    }
}
