package com.group3.infrastructure.persistence.entity;

import com.group3.persistence.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(
        name = "booking_passengers",
        indexes = {
                @Index(
                        name = "idx_booking_passengers_booking_id",
                        columnList = "booking_id"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
public class BookingPassengerEntity extends BaseEntity {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "booking_id", nullable = false)
    private BookingEntity booking;

    @Column(name = "passenger_profile_id")
    private UUID passengerProfileId;

    @Column(name = "full_name", nullable = false, length = 150)
    private String fullName;

    @Column(name = "passport_number", nullable = false)
    private  String passportNumber;

    @Column(name = "date_of_birth", nullable = false)
    private LocalDate dateOfBirth;

    @Column(name ="phone")
    private String phone;

    @Column(name = "email")
    private String email;

}
