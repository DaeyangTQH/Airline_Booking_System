package com.group3.flightsearch.application.service;

import com.group3.exception.CustomException;
import com.group3.exception.ErrorType;
import com.group3.flightsearch.application.command.CreateAirportCommand;
import com.group3.flightsearch.application.command.UpdateAirportCommand;
import com.group3.flightsearch.domain.aggregate.Airport;
import com.group3.flightsearch.domain.exception.InvalidDomainValueException;
import com.group3.flightsearch.domain.repository.AirportRepository;
import com.group3.flightsearch.domain.valueobject.AirportCode;
import com.group3.flightsearch.domain.valueobject.AirportId;
import org.junit.jupiter.api.Test;
import java.util.Optional;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AirportServiceTest {
    private final AirportRepository repository = mock(AirportRepository.class);
    private final AirportService service = new AirportService(repository);

    @Test
    void createNormalizesCodeAndPersistsDomainValues() {
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        var result = service.create(new CreateAirportCommand("sgn", " Tan Son Nhat ", " HCM ", " Vietnam "));
        assertNotNull(result.id());
        assertEquals("SGN", result.code());
        assertEquals("Tan Son Nhat", result.name());
        assertEquals("HCM", result.city());
        verify(repository).existsByCode(new AirportCode("SGN"));
    }

    @Test
    void duplicateCodeIsAConflictAndDoesNotSave() {
        when(repository.existsByCode(new AirportCode("HAN"))).thenReturn(true);
        var error = assertThrows(CustomException.class,
                () -> service.create(new CreateAirportCommand("han", "Noi Bai", "Hanoi", "Vietnam")));
        assertEquals(ErrorType.CONFLICT, error.getType());
        assertEquals("AIRPORT_CODE_ALREADY_EXISTS", error.getCode());
        verify(repository, never()).save(any());
    }

    @Test
    void updatePreservesIdentityAndCode() {
        UUID id = UUID.randomUUID();
        Airport airport = Airport.register(new AirportId(id), new AirportCode("HAN"),
                "Old", "Old city", "Vietnam");
        when(repository.findById(new AirportId(id))).thenReturn(Optional.of(airport));
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        var result = service.update(new UpdateAirportCommand(id, "Noi Bai", "Hanoi", "Vietnam"));
        assertEquals(id, result.id());
        assertEquals("HAN", result.code());
        assertEquals("Noi Bai", result.name());
        assertEquals("Hanoi", result.city());
    }

    @Test
    void missingAirportIsNotFound() {
        when(repository.findById(any())).thenReturn(Optional.empty());
        var error = assertThrows(CustomException.class, () -> service.getById(UUID.randomUUID()));
        assertEquals(ErrorType.NOT_FOUND, error.getType());
    }

    @Test
    void rejectsInvalidPaginationBeforeQueryingDatabase() {
        assertThrows(InvalidDomainValueException.class, () -> service.list(-1, 20));
        assertThrows(InvalidDomainValueException.class, () -> service.list(0, 0));
        assertThrows(InvalidDomainValueException.class, () -> service.list(0, 101));
        verifyNoInteractions(repository);
    }
}
