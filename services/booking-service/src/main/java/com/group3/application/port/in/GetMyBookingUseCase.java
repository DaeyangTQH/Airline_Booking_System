package com.group3.application.port.in;

import com.group3.application.query.BookingSumaryView;

import java.util.List;
import java.util.UUID;

public interface GetMyBookingUseCase {

    List<BookingSumaryView> getByUserId(UUID userId);
}
