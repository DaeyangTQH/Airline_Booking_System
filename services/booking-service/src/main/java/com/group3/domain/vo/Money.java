package com.group3.domain.vo;

import com.group3.domain.exception.InvalidDomainValueException;

import java.math.BigDecimal;

public record Money(BigDecimal amount, CurrencyCode currency) {
    public Money {
        if(amount == null){
            throw new InvalidDomainValueException(
                    "Money amount must not be null"
            );
        }

        if(currency == null){
            throw new InvalidDomainValueException(
                    "Money currency must not be null"
            );
        }

        if(amount.signum() < 0){
            throw new InvalidDomainValueException(
                    "Money amount must not be negative"
            );
        }

        amount = amount.stripTrailingZeros();
    }

    public Money multiply(TicketQuantity quantity){
        if(quantity == null){
            throw new InvalidDomainValueException(
                    "Ticket quantity must not be null"
            );
        }

        return new Money(
                amount.multiply(BigDecimal.valueOf(quantity.value())),
                currency
        );
    }

    public Money add(Money other){
        if(other == null){
            throw new InvalidDomainValueException(
                    "Money must not be null"
            );
        }

        if(!currency.equals(other.currency())){
            throw new InvalidDomainValueException(
                    "Cannot add Money with different currencies"
            );
        }

        return new Money(amount.add(other.amount()), currency);
    }
}
