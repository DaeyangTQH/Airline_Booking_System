package com.group3.flightsearch.domain.aggregate;

import com.group3.flightsearch.domain.exception.InvalidDomainValueException;
import com.group3.flightsearch.domain.valueobject.AircraftId;
import com.group3.flightsearch.domain.valueobject.AircraftModel;
import com.group3.flightsearch.domain.valueobject.SeatCapacity;

public final class Aircraft {
    private final AircraftId id;
    private AircraftModel model;
    private SeatCapacity totalSeats;

    private Aircraft(AircraftId id, AircraftModel model, SeatCapacity totalSeats) {
        if (id == null || model == null || totalSeats == null) {
            throw new InvalidDomainValueException("Aircraft id, model and capacity are required");
        }
        this.id = id;
        this.model = model;
        this.totalSeats = totalSeats;
    }

    public static Aircraft register(AircraftId id, AircraftModel model, SeatCapacity totalSeats) {
        return new Aircraft(id, model, totalSeats);
    }

    public void changeModel(AircraftModel model) {
        if (model == null) {
            throw new InvalidDomainValueException("Aircraft model is required");
        }
        this.model = model;
    }

    // The caller supplies the current assigned seat count from the seat repository.
    public void changeCapacity(SeatCapacity capacity, int assignedSeats) {
        if (capacity == null || assignedSeats < 0) {
            throw new InvalidDomainValueException("Capacity and nonnegative assigned seat count are required");
        }
        if (capacity.value() < assignedSeats) {
            throw new InvalidDomainValueException("Capacity cannot be below assigned seat count");
        }
        this.totalSeats = capacity;
    }

    public AircraftId getId() { return id; }
    public AircraftModel getModel() { return model; }
    public SeatCapacity getTotalSeats() { return totalSeats; }
}
