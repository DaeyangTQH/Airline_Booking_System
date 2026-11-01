package com.group3.domain.vo;

import com.group3.domain.exception.InvalidDomainValueException;

public record ContactInfo(String phone, String email) {

    private static final String PHONE_PATTERN = "^\\+?[0-9]{9,15}$";

    private static final String EMAIL_PATTERN = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";

    public ContactInfo {
        if (phone == null || phone.isBlank()) {
            throw new InvalidDomainValueException(
                    "Phone number must not be blank"
            );
        }

        if(email == null || email.isBlank()){
            throw new InvalidDomainValueException(
                    "Email must not be blank"
            );
        }

        phone = phone.trim();
        email = email.trim().toLowerCase();

        if(!phone.matches(PHONE_PATTERN)){
            throw new InvalidDomainValueException(
                    "Invalid phone number"
            );
        }

        if (!email.matches(EMAIL_PATTERN)){
            throw new InvalidDomainValueException(
                    "Invalid email address"
            );
        }
    }
}
