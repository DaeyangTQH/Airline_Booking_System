package com.group3.flightsearch.api.dto;

import com.group3.flightsearch.domain.model.FareClass;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.Instant;

public record CreateFareRequest(
        @NotNull FareClass fareClass,
        @NotNull @DecimalMin("0.00") @Digits(integer = 13, fraction = 2) BigDecimal basePrice,
        @NotBlank @Pattern(regexp = "[A-Z]{3}") String currency,
        @NotNull Instant validFrom, @NotNull Instant validTo) {}
