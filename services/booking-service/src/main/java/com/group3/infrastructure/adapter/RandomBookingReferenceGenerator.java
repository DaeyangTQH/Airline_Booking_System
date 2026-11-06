package com.group3.infrastructure.adapter;

import com.group3.application.port.out.BookingReferenceGenerator;
import com.group3.domain.vo.BookingReference;
import org.springframework.stereotype.Component;
import java.security.SecureRandom;

@Component
public class RandomBookingReferenceGenerator implements BookingReferenceGenerator {

    private static final int REFERENCE_LENGTH = 6;
    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private final SecureRandom random = new SecureRandom();
    @Override
    public BookingReference generate() {
        StringBuilder reference =
                new StringBuilder(REFERENCE_LENGTH);

        for(int i = 0; i < REFERENCE_LENGTH; i++){
            int randomIndex =
                    random.nextInt(CHARACTERS.length());

            reference.append(
                    CHARACTERS.charAt(randomIndex)
            );
        }

        return new BookingReference(
                reference.toString()
        );
    }
}
