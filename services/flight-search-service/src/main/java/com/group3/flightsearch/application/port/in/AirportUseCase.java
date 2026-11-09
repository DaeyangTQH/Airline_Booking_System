package com.group3.flightsearch.application.port.in;

import com.group3.flightsearch.application.query.AirportView;
import java.util.List;

public interface AirportUseCase {
    List<AirportView> list(int page, int size);
}
