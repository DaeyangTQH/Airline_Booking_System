package com.group3.application.port.in;

import com.group3.application.query.BookingSummaryView;

import java.util.List;
import java.util.UUID;

public interface GetMyBookingsUseCase {

    List<BookingSummaryView> getByUserId(UUID userId);
}
