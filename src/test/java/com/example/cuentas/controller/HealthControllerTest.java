package com.example.cuentas.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests unitarios para el endpoint /health de ms-cuentas.
 *
 * @WebMvcTest levanta solo la capa web (sin JPA ni RabbitMQ),
 * lo que hace los tests rápidos y completamente aislados.
 */
@WebMvcTest(HealthController.class)
class HealthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("GET /health - debe retornar 200 OK con status UP")
    void health_shouldReturnOk() throws Exception {
        mockMvc.perform(get("/health")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    @DisplayName("GET /health - debe identificarse como ms-cuentas")
    void health_shouldReturnCorrectServiceName() throws Exception {
        mockMvc.perform(get("/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.service").value("ms-cuentas"))
                .andExpect(jsonPath("$.timestamp").exists());
    }
}
