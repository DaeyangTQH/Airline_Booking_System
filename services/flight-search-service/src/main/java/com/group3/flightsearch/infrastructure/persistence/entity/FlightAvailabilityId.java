package com.group3.flightsearch.infrastructure.persistence.entity;

import com.group3.flightsearch.domain.model.FareClass;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class FlightAvailabilityId implements Serializable {
    private static final long serialVersionUID = 1L;
    public UUID flightId;
    public FareClass fareClass;
}
