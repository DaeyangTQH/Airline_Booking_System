package com.group3.api.rest;

import com.group3.api.dto.BookingResponse;
import com.group3.api.mapper.BookingApiMapper;
import com.group3.application.port.in.GetBookingUseCase;
import com.group3.domain.aggregate.Booking;
import com.group3.domain.vo.BookingId;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final GetBookingUseCase getBookingPort;
    private final BookingApiMapper bookingApiMapper;

    @GetMapping("/{bookingId}")
    public ResponseEntity<BookingResponse> getById(
            @PathVariable UUID bookingId
            ){

        Booking booking = getBookingPort.getById(new BookingId(bookingId));

        return ResponseEntity.ok(bookingApiMapper.toResponse(booking));
    }
}
