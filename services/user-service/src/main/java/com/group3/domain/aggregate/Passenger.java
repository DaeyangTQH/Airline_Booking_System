package com.group3.domain.aggregate;

import com.group3.domain.exception.DomainValidationException;
import com.group3.domain.vo.PassengerId;
import com.group3.domain.vo.UserId;
import com.group3.domain.vo.Email;
import java.time.LocalDate;

public class Passenger {
    private final PassengerId id;
    private final UserId userId;
    private String fullName;
    private String passportNo;
    private LocalDate dob;
    private String phone;
    private String email;

    private Passenger(PassengerId id, UserId userId, String name, String passport,
                      LocalDate dob, String phone, String email) {
        if (id == null || userId == null) throw new DomainValidationException("Passenger ID and owner are required");
        this.id = id;
        this.userId = userId;
        update(name, passport, dob, phone, email);
    }

    public static Passenger create(UserId ownerId, String name, String passport,
                                   LocalDate dob, String phone, String email) {
        return new Passenger(PassengerId.newId(), ownerId, name, passport, dob, phone, email);
    }

    public static Passenger restore(PassengerId id, UserId ownerId, String name, String passport,
                                    LocalDate dob, String phone, String email) {
        return new Passenger(id, ownerId, name, passport, dob, phone, email);
    }

    public void update(String name, String passport, LocalDate dob, String phone, String email) {
        if (name == null || name.isBlank() || name.trim().length() > 150) {
            throw new DomainValidationException("Passenger full name must be 1-150 characters");
        }
        if (dob == null || dob.isAfter(LocalDate.now())) {
            throw new DomainValidationException("Invalid passenger date of birth");
        }
        this.fullName = name.trim();
        this.passportNo = optional(passport, 30, "Passport number");
        this.dob = dob;
        this.phone = optional(phone, 20, "Phone");
        String optionalEmail = optional(email, 255, "Email");
        this.email = optionalEmail == null ? null : new Email(optionalEmail).value();
    }

    private static String optional(String value, int max, String label) {
        if (value == null || value.isBlank()) return null;
        String clean = value.trim();
        if (clean.length() > max) throw new DomainValidationException(label + " is too long");
        return clean;
    }

    public PassengerId getId() { return id; }
    public UserId getUserId() { return userId; }
    public String getFullName() { return fullName; }
    public String getPassportNo() { return passportNo; }
    public LocalDate getDob() { return dob; }
    public String getPhone() { return phone; }
    public String getEmail() { return email; }
}
