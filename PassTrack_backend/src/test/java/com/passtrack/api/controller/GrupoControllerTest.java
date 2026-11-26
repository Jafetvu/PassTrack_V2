package com.passtrack.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.passtrack.api.dto.grupo.GrupoCreateRecord;
import com.passtrack.api.dto.grupo.InscripcionCreateRecord;
import com.passtrack.api.model.Grupo;
import com.passtrack.api.model.Rol;
import com.passtrack.api.model.Usuario;
import com.passtrack.api.repository.GrupoRepository;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class GrupoControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper json;

    @Autowired
    private UsuarioRepository usuarioRepo;

    @Autowired
    private GrupoRepository grupoRepo;

    @Autowired
    private PasswordEncoder encoder;

    @Autowired
    private JwtService jwt;

    private String tokenDirector;
    private String tokenDocenteActivo;
    private String tokenOtroDocente;
    private Usuario docenteActivo;
    private Usuario docenteInactivo;
    private Usuario alumnoActivo;

    @BeforeEach
    void prepararDatos() {
        Usuario director = usuarioRepo.save(Usuario.builder()
            .nombre("Dir. Grupos")
            .email("dir.grupos@passtrack.edu")
            .passwordHash(encoder.encode("DirPass123!"))
            .rol(Rol.DIRECTOR)
            .activo(true)
            .build());

        docenteActivo = usuarioRepo.save(Usuario.builder()
            .nombre("Prof. Mateo Silva")
            .email("mateo.silva@passtrack.edu")
            .passwordHash(encoder.encode("DocPass123!"))
            .rol(Rol.DOCENTE)
            .activo(true)
            .build());

        Usuario otroDocente = usuarioRepo.save(Usuario.builder()
            .nombre("Profa. Sofía Lara")
            .email("sofia.lara@passtrack.edu")
            .passwordHash(encoder.encode("DocPass123!"))
            .rol(Rol.DOCENTE)
            .activo(true)
            .build());

        docenteInactivo = usuarioRepo.save(Usuario.builder()
            .nombre("Prof. Retirado")
            .email("retirado@passtrack.edu")
            .passwordHash(encoder.encode("DocPass123!"))
            .rol(Rol.DOCENTE)
            .activo(false)
            .build());

        alumnoActivo = usuarioRepo.save(Usuario.builder()
            .nombre("Diego Torres")
            .email("diego.torres@passtrack.edu")
            .passwordHash(encoder.encode("AluPass123!"))
            .rol(Rol.ALUMNO)
            .activo(true)
            .build());

        tokenDirector = "Bearer " + jwt.generateToken(new CustomUserDetails(director));
        tokenDocenteActivo = "Bearer " + jwt.generateToken(new CustomUserDetails(docenteActivo));
        tokenOtroDocente = "Bearer " + jwt.generateToken(new CustomUserDetails(otroDocente));
    }

    @Test
    void crearGrupo_ComoDirectorConDocenteActivo_Retorna201() throws Exception {
        GrupoCreateRecord dto = new GrupoCreateRecord("MAT-101", "Matemáticas I", docenteActivo.getId());

        mvc.perform(post("/api/grupos")
                .header(HttpHeaders.AUTHORIZATION, tokenDirector)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(dto)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.clave").value("MAT-101"))
            .andExpect(jsonPath("$.docenteId").value(docenteActivo.getId()))
            .andExpect(jsonPath("$.totalAlumnos").value(0));
    }

    @Test
    void crearGrupo_ConDocenteInactivoOInexistente_Retorna400() throws Exception {
        GrupoCreateRecord conInactivo = new GrupoCreateRecord("FIS-101", "Física I", docenteInactivo.getId());
        mvc.perform(post("/api/grupos")
                .header(HttpHeaders.AUTHORIZATION, tokenDirector)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(conInactivo)))
            .andExpect(status().isBadRequest());

        GrupoCreateRecord conInexistente = new GrupoCreateRecord("FIS-102", "Física II", 999999L);
        mvc.perform(post("/api/grupos")
                .header(HttpHeaders.AUTHORIZATION, tokenDirector)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(conInexistente)))
            .andExpect(status().isBadRequest());
    }

    @Test
    void crearGrupo_SinRolDirector_Retorna403() throws Exception {
        GrupoCreateRecord dto = new GrupoCreateRecord("QUI-101", "Química I", docenteActivo.getId());

        mvc.perform(post("/api/grupos")
                .header(HttpHeaders.AUTHORIZATION, tokenDocenteActivo)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(dto)))
            .andExpect(status().isForbidden());
    }

    @Test
    void inscribirAlumno_YConsultarGruposDocente_RespetaOwnership() throws Exception {
        Grupo grupo = grupoRepo.save(Grupo.builder()
            .clave("PROG-201")
            .nombre("Programación II")
            .docente(docenteActivo)
            .activo(true)
            .build());

        InscripcionCreateRecord inscripcionDto = new InscripcionCreateRecord(alumnoActivo.getId());
        mvc.perform(post("/api/grupos/{grupoId}/inscripciones", grupo.getId())
                .header(HttpHeaders.AUTHORIZATION, tokenDirector)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(inscripcionDto)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.alumnoId").value(alumnoActivo.getId()))
            .andExpect(jsonPath("$.grupoClave").value("PROG-201"));

        mvc.perform(get("/api/grupos/docente")
                .header(HttpHeaders.AUTHORIZATION, tokenDocenteActivo))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].clave").value("PROG-201"))
            .andExpect(jsonPath("$[0].totalAlumnos").value(1));

        mvc.perform(get("/api/grupos/{grupoId}/inscripciones", grupo.getId())
                .header(HttpHeaders.AUTHORIZATION, tokenOtroDocente))
            .andExpect(status().isForbidden());
    }
}
