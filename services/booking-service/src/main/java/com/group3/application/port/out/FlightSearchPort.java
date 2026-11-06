package com.group3.application.port.out;

import com.group3.application.integration.FlightFareQuote;
import com.group3.domain.vo.FareClass;

import java.util.UUID;

public interface FlightSearchPort {

    FlightFareQuote getFareQuote(
            UUID flightId,
            FareClass fareClass,
            int ticketQuantity
    );
}
