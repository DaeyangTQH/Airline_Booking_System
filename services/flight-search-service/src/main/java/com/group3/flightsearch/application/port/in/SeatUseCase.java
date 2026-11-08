package com.group3.flightsearch.application.port.in;

import com.group3.flightsearch.application.command.CreateSeatCommand;
import com.group3.flightsearch.application.query.SeatView;
import com.group3.flightsearch.domain.model.FareClass;
import java.util.List;
import java.util.UUID;

public interface SeatUseCase {
    SeatView create(CreateSeatCommand command);
    SeatView getById(UUID id);
    List<SeatView> list(UUID flightId, FareClass seatClass, int page, int size);
}
