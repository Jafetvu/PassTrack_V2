package com.passtrack.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.passtrack.api.dto.auth.LoginRequestRecord;
import com.passtrack.api.model.Rol;
import com.passtrack.api.model.Usuario;
import com.passtrack.api.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        usuarioRepository.save(Usuario.builder()
            .nombre("Dr. Roberto Gómez")
            .email("director@passtrack.edu")
            .passwordHash(passwordEncoder.encode("Director123!"))
            .rol(Rol.DIRECTOR)
            .activo(true)
            .build());

        usuarioRepository.save(Usuario.builder()
            .nombre("Usuario Inactivo")
            .email("inactivo@passtrack.edu")
            .passwordHash(passwordEncoder.encode("Secret123!"))
            .rol(Rol.DOCENTE)
            .activo(false)
            .build());
    }

    @Test
    void login_CredencialesValidas_Retorna200ConJwtSinPasswordHash() throws Exception {
        LoginRequestRecord request = new LoginRequestRecord("director@passtrack.edu", "Director123!");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.token", notNullValue()))
            .andExpect(jsonPath("$.email").value("director@passtrack.edu"))
            .andExpect(jsonPath("$.rol").value("DIRECTOR"))
            .andExpect(jsonPath("$.userId", notNullValue()))
            .andExpect(content().string(not(containsString("passwordHash"))));
    }

    @Test
    void login_CredencialesIncorrectas_Retorna401() throws Exception {
        LoginRequestRecord request = new LoginRequestRecord("director@passtrack.edu", "ClaveErronea!");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void login_UsuarioInactivo_Retorna401() throws Exception {
        LoginRequestRecord request = new LoginRequestRecord("inactivo@passtrack.edu", "Secret123!");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void login_DatosInvalidos_Retorna400() throws Exception {
        LoginRequestRecord request = new LoginRequestRecord("correo-invalido", "");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }
}
