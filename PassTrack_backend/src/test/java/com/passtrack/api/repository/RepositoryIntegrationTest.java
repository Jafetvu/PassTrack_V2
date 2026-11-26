package com.passtrack.api.repository;

import com.passtrack.api.model.Asistencia;
import com.passtrack.api.model.EstadoAsistencia;
import com.passtrack.api.model.EstadoJustificante;
import com.passtrack.api.model.Grupo;
import com.passtrack.api.model.Inscripcion;
import com.passtrack.api.model.Justificante;
import com.passtrack.api.model.Rol;
import com.passtrack.api.model.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class RepositoryIntegrationTest {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private GrupoRepository grupoRepository;

    @Autowired
    private InscripcionRepository inscripcionRepository;

    @Autowired
    private AsistenciaRepository asistenciaRepository;

    @Autowired
    private JustificanteRepository justificanteRepository;

    private Usuario docenteActivo;
    private Usuario docenteInactivo;
    private Usuario alumno;
    private Grupo grupoActivo;
    private Grupo grupoInactivo;

    @BeforeEach
    void setUp() {
        docenteActivo = usuarioRepository.save(Usuario.builder()
            .nombre("Mtra. Laura Morales")
            .email("laura.morales@passtrack.edu")
            .passwordHash("$2a$10$hash1")
            .rol(Rol.DOCENTE)
            .activo(true)
            .build());

        docenteInactivo = usuarioRepository.save(Usuario.builder()
            .nombre("Prof. Baja Lógica")
            .email("baja.docente@passtrack.edu")
            .passwordHash("$2a$10$hash2")
            .rol(Rol.DOCENTE)
            .activo(false)
            .build());

        alumno = usuarioRepository.save(Usuario.builder()
            .nombre("Carlos Mendoza")
            .email("carlos.mendoza@passtrack.edu")
            .passwordHash("$2a$10$hash3")
            .rol(Rol.ALUMNO)
            .activo(true)
            .build());

        grupoActivo = grupoRepository.save(Grupo.builder()
            .clave("MAT-101")
            .nombre("Matemáticas Avanzadas")
            .docente(docenteActivo)
            .activo(true)
            .build());

        grupoInactivo = grupoRepository.save(Grupo.builder()
            .clave("OLD-999")
            .nombre("Materia Archivada")
            .docente(docenteActivo)
            .activo(false)
            .build());
    }

    @Test
    void usuarioRepository_FiltraBorradoLogicoYBuscaPorEmail() {
        assertThat(usuarioRepository.findByEmail("laura.morales@passtrack.edu")).isPresent();
        assertThat(usuarioRepository.findByEmailAndActivoTrue("baja.docente@passtrack.edu")).isEmpty();
        assertThat(usuarioRepository.findByActivoTrue())
            .extracting(Usuario::getEmail)
            .containsExactlyInAnyOrder("laura.morales@passtrack.edu", "carlos.mendoza@passtrack.edu");
        assertThat(usuarioRepository.findByRolAndActivoTrue(Rol.DOCENTE))
            .extracting(Usuario::getId)
            .containsExactly(docenteActivo.getId());
    }

    @Test
    void grupoRepository_RetornaSoloGruposActivosDelDocente() {
        List<Grupo> grupos = grupoRepository.findByDocenteIdAndActivoTrue(docenteActivo.getId());

        assertThat(grupos)
            .extracting(Grupo::getClave)
            .containsExactly("MAT-101");
        assertThat(grupoRepository.existsByIdAndDocenteId(grupoActivo.getId(), docenteActivo.getId())).isTrue();
        assertThat(grupoRepository.existsByIdAndDocenteId(grupoActivo.getId(), docenteInactivo.getId())).isFalse();
        assertThat(grupoRepository.findByClave("MAT-101")).isPresent();
    }

    @Test
    void inscripcionRepository_VerificaMatriculaUnicaPorGrupo() {
        inscripcionRepository.save(Inscripcion.builder()
            .alumno(alumno)
            .grupo(grupoActivo)
            .build());

        assertThat(inscripcionRepository.existsByAlumnoIdAndGrupoId(alumno.getId(), grupoActivo.getId())).isTrue();
        assertThat(inscripcionRepository.existsByAlumnoIdAndGrupoId(alumno.getId(), grupoInactivo.getId())).isFalse();
        assertThat(inscripcionRepository.findByGrupoId(grupoActivo.getId())).hasSize(1);
    }

    @Test
    void asistenciaRepository_ConsultaPorGrupoFechaYClaveCompuestaUnica() {
        LocalDate hoy = LocalDate.of(2026, 11, 26);
        asistenciaRepository.save(Asistencia.builder()
            .fecha(hoy)
            .estado(EstadoAsistencia.FALTA)
            .alumno(alumno)
            .grupo(grupoActivo)
            .build());

        assertThat(asistenciaRepository.existsByGrupoIdAndFecha(grupoActivo.getId(), hoy)).isTrue();
        assertThat(asistenciaRepository.findByGrupoIdAndFecha(grupoActivo.getId(), hoy)).hasSize(1);

        Optional<Asistencia> encontrada = asistenciaRepository
            .findByAlumnoIdAndGrupoIdAndFecha(alumno.getId(), grupoActivo.getId(), hoy);
        assertThat(encontrada).isPresent();
        assertThat(encontrada.get().getEstado()).isEqualTo(EstadoAsistencia.FALTA);
    }

    @Test
    void justificanteRepository_ConsultaPorAsistenciaEstadoYAlumno() {
        Asistencia falta = asistenciaRepository.save(Asistencia.builder()
            .fecha(LocalDate.of(2026, 11, 26))
            .estado(EstadoAsistencia.FALTA)
            .alumno(alumno)
            .grupo(grupoActivo)
            .build());

        justificanteRepository.save(Justificante.builder()
            .motivo("Consulta médica")
            .evidenciaUrl("/app/uploads/justificante-1.pdf")
            .estado(EstadoJustificante.PENDIENTE)
            .asistencia(falta)
            .build());

        assertThat(justificanteRepository.existsByAsistenciaId(falta.getId())).isTrue();
        assertThat(justificanteRepository.findByAsistenciaId(falta.getId())).isPresent();
        assertThat(justificanteRepository.findByEstado(EstadoJustificante.PENDIENTE)).hasSize(1);
        assertThat(justificanteRepository.findByAsistenciaAlumnoId(alumno.getId())).hasSize(1);
    }
}
