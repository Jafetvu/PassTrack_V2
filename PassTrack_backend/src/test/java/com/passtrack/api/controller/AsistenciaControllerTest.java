package com.passtrack.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.passtrack.api.dto.asistencia.PaseListaItemRecord;
import com.passtrack.api.dto.asistencia.RegistroAsistenciaBulkRecord;
import com.passtrack.api.model.Asistencia;
import com.passtrack.api.model.EstadoAsistencia;
import com.passtrack.api.model.Grupo;
import com.passtrack.api.model.Inscripcion;
import com.passtrack.api.model.Rol;
import com.passtrack.api.model.Usuario;
import com.passtrack.api.repository.AsistenciaRepository;
import com.passtrack.api.repository.GrupoRepository;
import com.passtrack.api.repository.InscripcionRepository;
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

import java.time.LocalDate;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AsistenciaControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper json;

    @Autowired
    private UsuarioRepository usuarioRepo;

    @Autowired
    private GrupoRepository grupoRepo;

    @Autowired
    private InscripcionRepository inscripcionRepo;

    @Autowired
    private AsistenciaRepository asistenciaRepo;

    @Autowired
    private PasswordEncoder encoder;

    @Autowired
    private JwtService jwt;

    private String tokenDocenteTitular;
    private String tokenDocenteAjeno;
    private String tokenAlumnoA;
    private Grupo grupo;
    private Usuario alumnoA;
    private Usuario alumnoB;

    @BeforeEach
    void prepararDatos() {
        Usuario docenteTitular = usuarioRepo.save(Usuario.builder()
            .nombre("Prof. Titular")
            .email("titular.asist@passtrack.edu")
            .passwordHash(encoder.encode("Pass1234!"))
            .rol(Rol.DOCENTE)
            .activo(true)
            .build());

        Usuario docenteAjeno = usuarioRepo.save(Usuario.builder()
            .nombre("Prof. Ajeno")
            .email("ajeno.asist@passtrack.edu")
            .passwordHash(encoder.encode("Pass1234!"))
            .rol(Rol.DOCENTE)
            .activo(true)
            .build());

        alumnoA = usuarioRepo.save(Usuario.builder()
            .nombre("Alumno A")
            .email("alumnoa.asist@passtrack.edu")
            .passwordHash(encoder.encode("Pass1234!"))
            .rol(Rol.ALUMNO)
            .activo(true)
            .build());

        alumnoB = usuarioRepo.save(Usuario.builder()
            .nombre("Alumno B")
            .email("alumnob.asist@passtrack.edu")
            .passwordHash(encoder.encode("Pass1234!"))
            .rol(Rol.ALUMNO)
            .activo(true)
            .build());

        grupo = grupoRepo.save(Grupo.builder()
            .clave("CAL-101")
            .nombre("Cálculo I")
            .docente(docenteTitular)
            .activo(true)
            .build());

        inscripcionRepo.save(Inscripcion.builder().alumno(alumnoA).grupo(grupo).build());
        inscripcionRepo.save(Inscripcion.builder().alumno(alumnoB).grupo(grupo).build());

        tokenDocenteTitular = "Bearer " + jwt.generateToken(new CustomUserDetails(docenteTitular));
        tokenDocenteAjeno = "Bearer " + jwt.generateToken(new CustomUserDetails(docenteAjeno));
        tokenAlumnoA = "Bearer " + jwt.generateToken(new CustomUserDetails(alumnoA));
    }

    @Test
    void registrarAsistencia_DocenteTitular_Retorna201YConsultaHoy() throws Exception {
        RegistroAsistenciaBulkRecord payload = new RegistroAsistenciaBulkRecord(
            LocalDate.now(),
            List.of(
                new PaseListaItemRecord(alumnoA.getId(), EstadoAsistencia.PRESENTE),
                new PaseListaItemRecord(alumnoB.getId(), EstadoAsistencia.FALTA)
            )
        );

        mvc.perform(post("/api/asistencias/grupo/{id}", grupo.getId())
                .header(HttpHeaders.AUTHORIZATION, tokenDocenteTitular)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(payload)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.length()").value(2));

        mvc.perform(get("/api/asistencias/grupo/{id}/hoy", grupo.getId())
                .header(HttpHeaders.AUTHORIZATION, tokenDocenteTitular))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void registrarAsistencia_DocenteNoTitular_Retorna403() throws Exception {
        RegistroAsistenciaBulkRecord payload = new RegistroAsistenciaBulkRecord(
            LocalDate.now(),
            List.of(new PaseListaItemRecord(alumnoA.getId(), EstadoAsistencia.PRESENTE))
        );

        mvc.perform(post("/api/asistencias/grupo/{id}", grupo.getId())
                .header(HttpHeaders.AUTHORIZATION, tokenDocenteAjeno)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(payload)))
            .andExpect(status().isForbidden());
    }

    @Test
    void registrarAsistencia_FechaInvalidaODuplicada_Retorna400Y409() throws Exception {
        RegistroAsistenciaBulkRecord fechaPasada = new RegistroAsistenciaBulkRecord(
            LocalDate.now().minusDays(2),
            List.of(new PaseListaItemRecord(alumnoA.getId(), EstadoAsistencia.PRESENTE))
        );

        mvc.perform(post("/api/asistencias/grupo/{id}", grupo.getId())
                .header(HttpHeaders.AUTHORIZATION, tokenDocenteTitular)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(fechaPasada)))
            .andExpect(status().isBadRequest());

        asistenciaRepo.save(Asistencia.builder()
            .fecha(LocalDate.now())
            .estado(EstadoAsistencia.PRESENTE)
            .alumno(alumnoA)
            .grupo(grupo)
            .build());

        RegistroAsistenciaBulkRecord duplicado = new RegistroAsistenciaBulkRecord(
            LocalDate.now(),
            List.of(new PaseListaItemRecord(alumnoA.getId(), EstadoAsistencia.FALTA))
        );

        mvc.perform(post("/api/asistencias/grupo/{id}", grupo.getId())
                .header(HttpHeaders.AUTHORIZATION, tokenDocenteTitular)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(duplicado)))
            .andExpect(status().isConflict());
    }

    @Test
    void misAsistencias_ComoAlumno_RetornaSoloSusRegistros() throws Exception {
        asistenciaRepo.save(Asistencia.builder()
            .fecha(LocalDate.now())
            .estado(EstadoAsistencia.FALTA)
            .alumno(alumnoA)
            .grupo(grupo)
            .build());
        asistenciaRepo.save(Asistencia.builder()
            .fecha(LocalDate.now())
            .estado(EstadoAsistencia.PRESENTE)
            .alumno(alumnoB)
            .grupo(grupo)
            .build());

        mvc.perform(get("/api/asistencias/mis-asistencias")
                .header(HttpHeaders.AUTHORIZATION, tokenAlumnoA))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].grupoClave").value("CAL-101"))
            .andExpect(jsonPath("$[0].faltas").value(1))
            .andExpect(jsonPath("$[0].historial.length()").value(1))
            .andExpect(jsonPath("$[0].historial[0].alumnoId").value(alumnoA.getId()));

        mvc.perform(get("/api/asistencias/mis-asistencias")
                .header(HttpHeaders.AUTHORIZATION, tokenDocenteTitular))
            .andExpect(status().isForbidden());
    }
}
