package com.group3.infrastructure.adapter;

import com.group3.application.port.out.TicketNumberGenerator;
import com.group3.domain.vo.TicketNumber;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.time.Year;
import java.time.ZoneId;

@Component
public class RandomTicketNumberGenerator implements TicketNumberGenerator {

    private static final int NUMBER_BOUND = 100_000;

    private final SecureRandom random = new SecureRandom();

    @Override
    public TicketNumber generate() {

        int currentYear = Year
                .now(ZoneId.of("Asia/Ho_Chi_Minh"))
                .getValue();

        int randomNumber = random.nextInt(NUMBER_BOUND);

        String numberPart = "%05d".formatted(randomNumber);

        return new TicketNumber(
                "TKT" + currentYear + numberPart
        );
    }
}
