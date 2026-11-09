package com.group3.flightsearch.application.port.in;

import com.group3.flightsearch.application.query.*;
import com.group3.flightsearch.domain.model.FareClass;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface FlightUseCase {
    FlightView getById(UUID id);
    List<FlightView> search(String origin, String destination, LocalDate date, FareClass fareClass, int quantity, int page, int size);
}
