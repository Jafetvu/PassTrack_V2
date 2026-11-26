package com.passtrack.api.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityFilterChainTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void peticionSinTokenARutaProtegida_Retorna401() throws Exception {
        mockMvc.perform(get("/api/usuarios"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void peticionConTokenInvalidoARutaProtegida_Retorna401() throws Exception {
        mockMvc.perform(get("/api/usuarios")
                .header("Authorization", "Bearer token.invalido.firmado"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void actuatorHealth_EsPublicoYRetorna200() throws Exception {
        mockMvc.perform(get("/actuator/health"))
            .andExpect(status().isOk());
    }
}
