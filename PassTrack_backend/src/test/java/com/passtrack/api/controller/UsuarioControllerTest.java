package com.passtrack.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.passtrack.api.dto.usuario.UsuarioCreateRecord;
import com.passtrack.api.model.Rol;
import com.passtrack.api.model.Usuario;
import com.passtrack.api.repository.UsuarioRepository;
import com.passtrack.api.security.CustomUserDetails;
import com.passtrack.api.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class UsuarioControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper json;

    @Autowired
    private UsuarioRepository repo;

    @Autowired
    private PasswordEncoder encoder;

    @Autowired
    private JwtService jwt;

    private String tokenDirector;
    private String tokenDocente;
    private String tokenAlumno;
    private Usuario alumnoExistente;

    @BeforeEach
    void prepararUsuarios() {
        Usuario dir = repo.save(Usuario.builder()
            .nombre("Dirección General")
            .email("dir.test@passtrack.edu")
            .passwordHash(encoder.encode("DirPass123!"))
            .rol(Rol.DIRECTOR)
            .activo(true)
            .build());

        Usuario doc = repo.save(Usuario.builder()
            .nombre("Profa. Elena Ruiz")
            .email("doc.test@passtrack.edu")
            .passwordHash(encoder.encode("DocPass123!"))
            .rol(Rol.DOCENTE)
            .activo(true)
            .build());

        alumnoExistente = repo.save(Usuario.builder()
            .nombre("Carlos Méndez")
            .email("alu.test@passtrack.edu")
            .passwordHash(encoder.encode("AluPass123!"))
            .rol(Rol.ALUMNO)
            .activo(true)
            .build());

        tokenDirector = "Bearer " + jwt.generateToken(new CustomUserDetails(dir));
        tokenDocente = "Bearer " + jwt.generateToken(new CustomUserDetails(doc));
        tokenAlumno = "Bearer " + jwt.generateToken(new CustomUserDetails(alumnoExistente));
    }

    @Test
    void crearUsuario_ComoDirector_Retorna201() throws Exception {
        UsuarioCreateRecord dto = new UsuarioCreateRecord(
            "Laura Sánchez",
            "laura.sanchez@passtrack.edu",
            "ClaveSegura1!",
            Rol.DOCENTE
        );

        mvc.perform(post("/api/usuarios")
                .header(HttpHeaders.AUTHORIZATION, tokenDirector)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(dto)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.email").value("laura.sanchez@passtrack.edu"))
            .andExpect(jsonPath("$.rol").value("DOCENTE"))
            .andExpect(jsonPath("$.activo").value(true))
            .andExpect(content().string(not(containsString("passwordHash"))));
    }

    @Test
    void crearUsuario_SinRolDirector_Retorna403() throws Exception {
        UsuarioCreateRecord dto = new UsuarioCreateRecord(
            "Intento Externo",
            "externo@passtrack.edu",
            "ClaveSegura1!",
            Rol.ALUMNO
        );
        String body = json.writeValueAsString(dto);

        mvc.perform(post("/api/usuarios")
                .header(HttpHeaders.AUTHORIZATION, tokenDocente)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isForbidden());

        mvc.perform(post("/api/usuarios")
                .header(HttpHeaders.AUTHORIZATION, tokenAlumno)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isForbidden());
    }

    @Test
    void listarUsuarios_ComoDirector_Retorna200() throws Exception {
        mvc.perform(get("/api/usuarios")
                .header(HttpHeaders.AUTHORIZATION, tokenDirector))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").isArray())
            .andExpect(jsonPath("$.totalElements").value(3));
    }

    @Test
    void desactivarUsuario_ComoDirector_Retorna204YMarcaInactivo() throws Exception {
        mvc.perform(delete("/api/usuarios/{id}", alumnoExistente.getId())
                .header(HttpHeaders.AUTHORIZATION, tokenDirector))
            .andExpect(status().isNoContent());

        Usuario actualizado = repo.findById(alumnoExistente.getId()).orElseThrow();
        assertThat(actualizado.getActivo()).isFalse();
    }
}
