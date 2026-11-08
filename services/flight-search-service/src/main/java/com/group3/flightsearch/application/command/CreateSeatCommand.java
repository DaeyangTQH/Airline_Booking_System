package com.group3.flightsearch.application.command;

import com.group3.flightsearch.domain.model.FareClass;
import java.util.UUID;

public record CreateSeatCommand(UUID flightId, String seatNumber, FareClass seatClass) {}
