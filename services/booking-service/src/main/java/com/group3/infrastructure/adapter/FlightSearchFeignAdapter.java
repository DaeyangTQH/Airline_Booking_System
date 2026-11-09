package com.group3.infrastructure.adapter;

import com.group3.application.exception.FlightNotFoundException;
import com.group3.application.exception.FlightNotBookableException;
import com.group3.application.exception.FlightSearchCommunicationException;
import com.group3.application.integration.FlightFareQuote;
import com.group3.application.port.out.FlightSearchPort;
import com.group3.domain.vo.CurrencyCode;
import com.group3.domain.vo.FareClass;
import com.group3.infrastructure.client.flight.FlightSearchFeignClient;
import com.group3.infrastructure.client.flight.dto.FlightFareResponse;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class FlightSearchFeignAdapter implements FlightSearchPort {

    private final FlightSearchFeignClient flightSearchFeignClient;

    @Override
    public FlightFareQuote getFareQuote(UUID flightId, FareClass fareClass, int ticketQuantity) {
        try{
            FlightFareResponse response =
                    flightSearchFeignClient.getFareQuote(
                            flightId,
                            fareClass.name(),
                            ticketQuantity
                    );
            return toQuote(response);
        }catch (FeignException exception){
            if(exception.status() == 409){
                throw new FlightNotBookableException(
                        "Flight fare is not supported for booking"
                );
            }
            if(exception.status() == 404){
                throw new FlightNotFoundException(
                        "Flight or fare was not found"
                );
            }

            throw new FlightSearchCommunicationException(
                    "Could not get fare quote from Flight Search Service"
            );
        }
    }

    private FlightFareQuote toQuote(
            FlightFareResponse response
    ){
        if(response == null){
            throw new FlightSearchCommunicationException(
                    "Flight Search Service returned an empty response"
            );
        }

        try{
            return new FlightFareQuote(
                    response.flightId(),
                    FareClass.valueOf(response.fareClass()),

                    response.unitPrice(),

                    CurrencyCode.valueOf(response.currency()),

                    response.availableSeats(),
                    response.bookable()
            );
        }catch(IllegalArgumentException exception){
            throw new FlightSearchCommunicationException(
                    "Flight Search Service returned an invalid fare class or currency"
            );
        }
    }
}
