package com.group3.application.command;
import java.time.LocalDate;
public record PassengerCommand(String fullName, String passportNo, LocalDate dob, String phone, String email) { }
