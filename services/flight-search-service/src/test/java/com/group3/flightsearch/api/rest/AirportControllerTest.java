package com.group3.flightsearch.api.rest;

import com.group3.exception.CustomException;
import com.group3.exception.ErrorType;
import com.group3.flightsearch.api.exception.GlobalExceptionHandler;
import com.group3.flightsearch.application.port.in.AirportUseCase;
import com.group3.flightsearch.application.query.AirportView;
import com.group3.flightsearch.domain.exception.InvalidDomainValueException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import java.util.UUID;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AirportControllerTest {
    private final AirportUseCase useCase = mock(AirportUseCase.class);
    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        var validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        mvc = MockMvcBuilders.standaloneSetup(new AirportController(useCase))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator).build();
    }

    @Test
    void createReturnsLocationAndSharedView() throws Exception {
        UUID id = UUID.randomUUID();
        when(useCase.create(any())).thenReturn(new AirportView(id, "HAN", "Noi Bai", "Hanoi", "Vietnam"));
        mvc.perform(post("/api/v1/airports").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"code":"HAN","name":"Noi Bai","city":"Hanoi","country":"Vietnam"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/v1/airports/" + id))
                .andExpect(jsonPath("$.code").value("HAN"));
    }

    @Test
    void invalidBodyUsesSharedErrorResponse() throws Exception {
        mvc.perform(post("/api/v1/airports").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"code":"HAN","name":"","city":"Hanoi","country":"Vietnam"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("REQUEST_VALIDATION_FAILED"))
                .andExpect(jsonPath("$.path").value("/api/v1/airports"))
                .andExpect(jsonPath("$.timestamp").exists());
        verifyNoInteractions(useCase);
    }

    @Test
    void malformedUuidAndJsonAreBadRequests() throws Exception {
        mvc.perform(get("/api/v1/airports/not-a-uuid")).andExpect(status().isBadRequest());
        mvc.perform(post("/api/v1/airports").contentType(MediaType.APPLICATION_JSON).content("{"))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(useCase);
    }

    @Test
    void missingAndDuplicateAirportsHaveCorrectStatus() throws Exception {
        when(useCase.getById(any())).thenThrow(
                new CustomException("AIRPORT_NOT_FOUND", ErrorType.NOT_FOUND, "Not found"));
        mvc.perform(get("/api/v1/airports/" + UUID.randomUUID()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("AIRPORT_NOT_FOUND"));
        when(useCase.create(any())).thenThrow(
                new CustomException("AIRPORT_CODE_ALREADY_EXISTS", ErrorType.CONFLICT, "Duplicate"));
        mvc.perform(post("/api/v1/airports").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"code":"HAN","name":"Noi Bai","city":"Hanoi","country":"Vietnam"}
                                """))
                .andExpect(status().isConflict());
    }

    @Test
    void invalidPageIsBadRequest() throws Exception {
        when(useCase.list(-1, 20)).thenThrow(new InvalidDomainValueException("Invalid page"));
        mvc.perform(get("/api/v1/airports").param("page", "-1"))
                .andExpect(status().isBadRequest());
    }
}
