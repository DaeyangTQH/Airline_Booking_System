
package com.group3.dto.passenger;

import com.group3.entity.Passenger;

import java.time.LocalDate;
import java.util.UUID;

public class PassengerResponse {

    private UUID id;
    private UUID userId;

    private String fullName;
    private String passportNo;
    private LocalDate dob;
    private String phone;
    private String email;

    public PassengerResponse() {
    }

    public PassengerResponse(Passenger passenger) {
        this.id = passenger.getId();
        this.userId = passenger.getUser().getId();
        this.fullName = passenger.getFullName();
        this.passportNo = passenger.getPassportNo();
        this.dob = passenger.getDob();
        this.phone = passenger.getPhone();
        this.email = passenger.getEmail();
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPassportNo() {
        return passportNo;
    }

    public void setPassportNo(String passportNo) {
        this.passportNo = passportNo;
    }

    public LocalDate getDob() {
        return dob;
    }

    public void setDob(LocalDate dob) {
        this.dob = dob;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
