package com.group3.flightsearch.application.port.in;

import com.group3.flightsearch.application.query.FareQuoteView;
import com.group3.flightsearch.domain.model.FareClass;
import java.util.UUID;

public interface GetFareQuoteUseCase {
    FareQuoteView getQuote(UUID flightId, FareClass fareClass, int quantity);
}
