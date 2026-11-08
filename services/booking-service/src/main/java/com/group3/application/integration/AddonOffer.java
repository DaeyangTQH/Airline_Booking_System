package com.group3.application.integration;

import com.group3.domain.aggregate.AddonType;
import com.group3.domain.vo.CurrencyCode;

import java.math.BigDecimal;

public record AddonOffer(
        AddonType type,
        String description,
        BigDecimal price,
        CurrencyCode currency
) {
}
