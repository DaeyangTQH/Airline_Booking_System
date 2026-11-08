package com.group3.flightsearch.application.command;

import java.util.UUID;

public record UpdateAircraftCommand(UUID id, String model, int totalSeats) {}
