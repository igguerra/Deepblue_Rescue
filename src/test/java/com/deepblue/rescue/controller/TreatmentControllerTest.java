package com.deepblue.rescue.controller;

import com.deepblue.rescue.domain.TreatmentType;
import com.deepblue.rescue.dto.response.TreatmentResponse;
import com.deepblue.rescue.exception.BusinessRuleException;
import com.deepblue.rescue.exception.GlobalExceptionHandler;
import com.deepblue.rescue.exception.ResourceNotFoundException;
import com.deepblue.rescue.service.TreatmentService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TreatmentController.class)
@Import(GlobalExceptionHandler.class)
class TreatmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TreatmentService service;

    private String validRequestBody() {
        LocalDateTime performedAt = LocalDateTime.now().minusDays(1);
        return """
                {
                    "animalCode": "AN-001",
                    "specialistCode": "SPEC-001",
                    "performedAt": "%s",
                    "type": "WOUND_CARE",
                    "description": "Cleaning and treatment of flipper injury."
                }
                """.formatted(performedAt);
    }

    @Test
    void shouldCreateTreatment() throws Exception {
        TreatmentResponse response = new TreatmentResponse(
                1L, "AN-001", "SPEC-001", LocalDateTime.now().minusDays(1),
                TreatmentType.WOUND_CARE, "Cleaning and treatment of flipper injury.");

        when(service.register(any())).thenReturn(response);

        mockMvc.perform(post("/api/treatments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validRequestBody()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.animalCode").value("AN-001"));

        verify(service).register(any());
    }

    @Test
    void shouldReturn400WhenRequestIsInvalid() throws Exception {
        String invalidBody = """
                {
                    "animalCode": "",
                    "specialistCode": "",
                    "type": null,
                    "description": ""
                }
                """;

        mockMvc.perform(post("/api/treatments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.details.animalCode").exists())
                .andExpect(jsonPath("$.details.specialistCode").exists());

        verify(service, never()).register(any());
    }

    @Test
    void shouldReturn404WhenAnimalDoesNotExist() throws Exception {
        when(service.register(any())).thenThrow(
                new ResourceNotFoundException("Animal not found: AN-999"));

        String body = """
                {
                    "animalCode": "AN-999",
                    "specialistCode": "SPEC-001",
                    "performedAt": "%s",
                    "type": "WOUND_CARE",
                    "description": "Cleaning and treatment of flipper injury."
                }
                """.formatted(LocalDateTime.now().minusDays(1));

        mockMvc.perform(post("/api/treatments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Animal not found: AN-999"));
    }

    @Test
    void shouldReturn409WhenBusinessRuleIsViolated() throws Exception {
        when(service.register(any())).thenThrow(
                new BusinessRuleException("Released animals cannot receive treatments"));

        mockMvc.perform(post("/api/treatments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validRequestBody()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Released animals cannot receive treatments"));
    }
}