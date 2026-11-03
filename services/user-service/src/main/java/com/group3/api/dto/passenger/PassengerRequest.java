package com.group3.api.dto.passenger;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
public record PassengerRequest(@NotBlank @Size(max = 150) String fullName,
                               @Size(max = 30) String passportNo,
                               @NotNull @PastOrPresent LocalDate dob,
                               @Size(max = 20) String phone,
                               @Email @Size(max = 255) String email) { }
