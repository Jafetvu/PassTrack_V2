package com.passtrack.api.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.persistence.PersistenceException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
class EntityPersistenceTest {

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void persistirModeloCompleto_RelacionesYDefaults_FuncionanCorrectamente() {
        Usuario docente = entityManager.persistAndFlush(Usuario.builder()
            .nombre("Mtra. Laura Morales")
            .email("docente@passtrack.edu")
            .passwordHash("$2a$10$hashedpassword")
            .rol(Rol.DOCENTE)
            .build());

        Usuario alumno = entityManager.persistAndFlush(Usuario.builder()
            .nombre("Carlos Mendoza")
            .email("alumno@passtrack.edu")
            .passwordHash("$2a$10$hashedpassword")
            .rol(Rol.ALUMNO)
            .build());

        Grupo grupo = entityManager.persistAndFlush(Grupo.builder()
            .clave("MAT-101")
            .nombre("Matemáticas Avanzadas")
            .docente(docente)
            .build());

        Inscripcion inscripcion = entityManager.persistAndFlush(Inscripcion.builder()
            .alumno(alumno)
            .grupo(grupo)
            .build());

        Asistencia asistencia = entityManager.persistAndFlush(Asistencia.builder()
            .fecha(LocalDate.of(2026, 11, 26))
            .estado(EstadoAsistencia.FALTA)
            .alumno(alumno)
            .grupo(grupo)
            .build());

        Justificante justificante = entityManager.persistAndFlush(Justificante.builder()
            .motivo("Cita médica")
            .evidenciaUrl("/app/uploads/receta.pdf")
            .asistencia(asistencia)
            .build());

        assertThat(docente.getActivo()).isTrue();
        assertThat(docente.getCreatedAt()).isNotNull();
        assertThat(grupo.getActivo()).isTrue();
        assertThat(inscripcion.getId()).isNotNull();
        assertThat(asistencia.getId()).isNotNull();
        assertThat(justificante.getEstado()).isEqualTo(EstadoJustificante.PENDIENTE);
    }

    @Test
    void asistenciaDuplicadaMismaFecha_LanzaPersistenceException() {
        Usuario docente = entityManager.persistAndFlush(Usuario.builder()
            .nombre("Docente Test")
            .email("docente.dup@passtrack.edu")
            .passwordHash("hash")
            .rol(Rol.DOCENTE)
            .build());

        Usuario alumno = entityManager.persistAndFlush(Usuario.builder()
            .nombre("Alumno Test")
            .email("alumno.dup@passtrack.edu")
            .passwordHash("hash")
            .rol(Rol.ALUMNO)
            .build());

        Grupo grupo = entityManager.persistAndFlush(Grupo.builder()
            .clave("PROG-201")
            .nombre("Estructuras de Datos")
            .docente(docente)
            .build());

        LocalDate fecha = LocalDate.of(2026, 11, 26);
        entityManager.persistAndFlush(Asistencia.builder()
            .fecha(fecha)
            .estado(EstadoAsistencia.PRESENTE)
            .alumno(alumno)
            .grupo(grupo)
            .build());

        Asistencia duplicada = Asistencia.builder()
            .fecha(fecha)
            .estado(EstadoAsistencia.FALTA)
            .alumno(alumno)
            .grupo(grupo)
            .build();

        assertThatThrownBy(() -> entityManager.persistAndFlush(duplicada))
            .isInstanceOf(PersistenceException.class);
    }

    @Test
    void serializacionUsuario_NoExponePasswordHash() throws Exception {
        Usuario usuario = Usuario.builder()
            .id(1L)
            .nombre("Dr. Roberto Gómez")
            .email("director@passtrack.edu")
            .passwordHash("$2a$10$secretHashValue")
            .rol(Rol.DIRECTOR)
            .activo(true)
            .build();

        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        String json = mapper.writeValueAsString(usuario);

        assertThat(json).contains("director@passtrack.edu");
        assertThat(json).doesNotContain("passwordHash");
        assertThat(json).doesNotContain("$2a$10$secretHashValue");
    }
}
