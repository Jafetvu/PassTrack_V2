package com.passtrack.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.passtrack.api.dto.justificante.JustificanteResolucionRecord;
import com.passtrack.api.model.Asistencia;
import com.passtrack.api.model.EstadoAsistencia;
import com.passtrack.api.model.EstadoJustificante;
import com.passtrack.api.model.Grupo;
import com.passtrack.api.model.Justificante;
import com.passtrack.api.model.Rol;
import com.passtrack.api.model.Usuario;
import com.passtrack.api.repository.AsistenciaRepository;
import com.passtrack.api.repository.GrupoRepository;
import com.passtrack.api.repository.JustificanteRepository;
import com.passtrack.api.repository.UsuarioRepository;
import com.passtrack.api.security.CustomUserDetails;
import com.passtrack.api.security.JwtService;
import com.passtrack.api.service.FileStorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class JustificanteControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper json;

    @Autowired
    private UsuarioRepository usuarioRepo;

    @Autowired
    private GrupoRepository grupoRepo;

    @Autowired
    private AsistenciaRepository asistenciaRepo;

    @Autowired
    private JustificanteRepository justificanteRepo;

    @Autowired
    private PasswordEncoder encoder;

    @Autowired
    private JwtService jwt;

    @MockBean
    private FileStorageService fileStorageService;

    private String tokenDirector;
    private String tokenDocente;
    private String tokenAlumno;
    private Asistencia asistenciaFalta;

    @BeforeEach
    void prepararDatos() {
        Usuario director = usuarioRepo.save(Usuario.builder()
            .nombre("Dir. Justificantes")
            .email("dir.just@passtrack.edu")
            .passwordHash(encoder.encode("Pass1234!"))
            .rol(Rol.DIRECTOR)
            .activo(true)
            .build());

        Usuario docente = usuarioRepo.save(Usuario.builder()
            .nombre("Doc. Justificantes")
            .email("doc.just@passtrack.edu")
            .passwordHash(encoder.encode("Pass1234!"))
            .rol(Rol.DOCENTE)
            .activo(true)
            .build());

        Usuario alumno = usuarioRepo.save(Usuario.builder()
            .nombre("Alu. Justificantes")
            .email("alu.just@passtrack.edu")
            .passwordHash(encoder.encode("Pass1234!"))
            .rol(Rol.ALUMNO)
            .activo(true)
            .build());

        Grupo grupo = grupoRepo.save(Grupo.builder()
            .clave("BIO-101")
            .nombre("Biología I")
            .docente(docente)
            .activo(true)
            .build());

        asistenciaFalta = asistenciaRepo.save(Asistencia.builder()
            .fecha(LocalDate.now())
            .estado(EstadoAsistencia.FALTA)
            .alumno(alumno)
            .grupo(grupo)
            .build());

        tokenDirector = "Bearer " + jwt.generateToken(new CustomUserDetails(director));
        tokenDocente = "Bearer " + jwt.generateToken(new CustomUserDetails(docente));
        tokenAlumno = "Bearer " + jwt.generateToken(new CustomUserDetails(alumno));
    }

    @Test
    void solicitarJustificante_ComoAlumnoPropietario_Retorna201() throws Exception {
        when(fileStorageService.storeFile(any())).thenReturn("mock-uuid.pdf");
        MockMultipartFile archivo = new MockMultipartFile(
            "archivo", "constancia.pdf", "application/pdf", "%PDF-1.4".getBytes()
        );

        mvc.perform(multipart("/api/justificantes")
                .file(archivo)
                .param("asistenciaId", asistenciaFalta.getId().toString())
                .param("motivo", "Cita médica oficial")
                .header(HttpHeaders.AUTHORIZATION, tokenAlumno))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.estado").value("PENDIENTE"))
            .andExpect(jsonPath("$.evidenciaUrl").value("mock-uuid.pdf"));
    }

    @Test
    void resolverJustificante_ComoDirector_AprobadoRetorna200YCambiaAsistencia() throws Exception {
        Justificante pendiente = justificanteRepo.save(Justificante.builder()
            .motivo("Incapacidad")
            .evidenciaUrl("uuid-1.pdf")
            .estado(EstadoJustificante.PENDIENTE)
            .asistencia(asistenciaFalta)
            .build());

        mvc.perform(get("/api/justificantes/pendientes")
                .header(HttpHeaders.AUTHORIZATION, tokenDirector))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1));

        JustificanteResolucionRecord dictamen = new JustificanteResolucionRecord(
            EstadoJustificante.APROBADO,
            "Validado por Dirección"
        );

        mvc.perform(patch("/api/justificantes/{id}/estado", pendiente.getId())
                .header(HttpHeaders.AUTHORIZATION, tokenDirector)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(dictamen)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.estado").value("APROBADO"))
            .andExpect(jsonPath("$.estadoAsistencia").value("JUSTIFICADO"));

        Asistencia actualizada = asistenciaRepo.findById(asistenciaFalta.getId()).orElseThrow();
        assertThat(actualizada.getEstado()).isEqualTo(EstadoAsistencia.JUSTIFICADO);
    }

    @Test
    void resolverJustificante_ComoDocenteOAlumno_Retorna403() throws Exception {
        Justificante pendiente = justificanteRepo.save(Justificante.builder()
            .motivo("Incapacidad")
            .evidenciaUrl("uuid-2.pdf")
            .estado(EstadoJustificante.PENDIENTE)
            .asistencia(asistenciaFalta)
            .build());

        String body = json.writeValueAsString(
            new JustificanteResolucionRecord(EstadoJustificante.APROBADO, "Intento no autorizado")
        );

        mvc.perform(patch("/api/justificantes/{id}/estado", pendiente.getId())
                .header(HttpHeaders.AUTHORIZATION, tokenDocente)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isForbidden());

        mvc.perform(patch("/api/justificantes/{id}/estado", pendiente.getId())
                .header(HttpHeaders.AUTHORIZATION, tokenAlumno)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isForbidden());
    }
}
