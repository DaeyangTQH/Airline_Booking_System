package com.group3.application.service;

import com.group3.application.exception.BookingAccessDeniedException;
import com.group3.domain.aggregate.Booking;
import com.group3.domain.exception.BookingNotFoundException;
import com.group3.domain.repository.BookingRepository;
import com.group3.domain.vo.BookingId;
import com.group3.domain.vo.PaymentId;
import com.group3.domain.vo.UserId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BookingPaymentTransactionService {

    private final BookingRepository bookingRepository;

    @Transactional
    public void attachPaymentToBooking(
            BookingId bookingId,
            UserId userId,
            PaymentId paymentId
    ){
        Booking booking = bookingRepository
                .findByIdForUpdate(bookingId)
                .orElseThrow(() ->
                        new BookingNotFoundException(
                                "Booking not found with ID: " + bookingId.value()
                        ));

        if(!booking.getUserId().equals(userId)){
            throw new BookingAccessDeniedException(
                    "You do not have permission to modify this booking"
            );
        }

        booking.attachPayment(paymentId);
        bookingRepository.save(booking);
    }
}
