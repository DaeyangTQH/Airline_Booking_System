package com.group3.flightsearch.application.port.in;

import com.group3.flightsearch.application.command.CreateAircraftCommand;
import com.group3.flightsearch.application.command.UpdateAircraftCommand;
import com.group3.flightsearch.application.query.AircraftView;
import java.util.List;
import java.util.UUID;

public interface AircraftUseCase {
    AircraftView create(CreateAircraftCommand command);
    AircraftView update(UpdateAircraftCommand command);
    AircraftView getById(UUID id);
    List<AircraftView> list(int page, int size);
}
