package com.group3.flightsearch.application.port.in;

import com.group3.flightsearch.application.command.CreateAirportCommand;
import com.group3.flightsearch.application.command.UpdateAirportCommand;
import com.group3.flightsearch.application.query.AirportView;
import java.util.List;
import java.util.UUID;

public interface AirportUseCase {
    AirportView create(CreateAirportCommand command);
    AirportView update(UpdateAirportCommand command);
    AirportView getById(UUID id);
    List<AirportView> list(int page, int size);
}
