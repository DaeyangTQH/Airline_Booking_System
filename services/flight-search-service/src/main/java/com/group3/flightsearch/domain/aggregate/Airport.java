package com.group3.flightsearch.domain.aggregate;

import com.group3.flightsearch.domain.exception.InvalidDomainValueException;
import com.group3.flightsearch.domain.valueobject.AirportCode;
import com.group3.flightsearch.domain.valueobject.AirportId;

public final class Airport {
    private final AirportId id;
    private final AirportCode code;
    private String name;
    private String city;
    private String country;

    private Airport(AirportId id, AirportCode code, String name, String city, String country) {
        if (id == null || code == null) {
            throw new InvalidDomainValueException("Airport id and code are required");
        }
        this.id = id;
        this.code = code;
        this.name = required(name, "Airport name");
        this.city = required(city, "City");
        this.country = required(country, "Country");
    }

    public static Airport register(AirportId id, AirportCode code, String name, String city, String country) {
        return new Airport(id, code, name, city, country);
    }

    public void rename(String name) {
        this.name = required(name, "Airport name");
    }

    public void changeLocation(String city, String country) {
        String validCity = required(city, "City");
        String validCountry = required(country, "Country");
        this.city = validCity;
        this.country = validCountry;
    }

    private static String required(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new InvalidDomainValueException(field + " must not be blank");
        }
        return value.trim();
    }

    public AirportId getId() { return id; }
    public AirportCode getCode() { return code; }
    public String getName() { return name; }
    public String getCity() { return city; }
    public String getCountry() { return country; }
}
