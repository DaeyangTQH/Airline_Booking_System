package com.group3.flightsearch.application.command;

import java.util.UUID;

public record UpdateAirportCommand(UUID id, String name, String city, String country) {}
