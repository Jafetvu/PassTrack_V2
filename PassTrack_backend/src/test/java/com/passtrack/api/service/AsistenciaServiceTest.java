package com.passtrack.api.service;

import com.passtrack.api.dto.asistencia.AsistenciaResponseRecord;
import com.passtrack.api.dto.asistencia.PaseListaItemRecord;
import com.passtrack.api.dto.asistencia.RegistroAsistenciaBulkRecord;
import com.passtrack.api.exception.BadRequestException;
import com.passtrack.api.exception.DuplicateResourceException;
import com.passtrack.api.model.Asistencia;
import com.passtrack.api.model.EstadoAsistencia;
import com.passtrack.api.model.Grupo;
import com.passtrack.api.model.Inscripcion;
import com.passtrack.api.model.Rol;
import com.passtrack.api.model.Usuario;
import com.passtrack.api.repository.AsistenciaRepository;
import com.passtrack.api.repository.GrupoRepository;
import com.passtrack.api.repository.InscripcionRepository;
import com.passtrack.api.repository.JustificanteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AsistenciaServiceTest {

    @Mock
    private AsistenciaRepository asistenciaRepository;

    @Mock
    private GrupoRepository grupoRepository;

    @Mock
    private InscripcionRepository inscripcionRepository;

    @Mock
    private JustificanteRepository justificanteRepository;

    @InjectMocks
    private AsistenciaService asistenciaService;

    private Grupo grupo;
    private Usuario alumno;

    @BeforeEach
    void preparar() {
        Usuario docente = Usuario.builder().id(10L).nombre("Prof. Titular").rol(Rol.DOCENTE).activo(true).build();
        alumno = Usuario.builder().id(20L).nombre("Alumno Uno").rol(Rol.ALUMNO).activo(true).build();
        grupo = Grupo.builder().id(100L).clave("MAT-1").nombre("Matemáticas").docente(docente).activo(true).build();
    }

    @Test
    void registrarPaseLista_DocenteNoTitular_LanzaAccessDenied() {
        when(grupoRepository.findById(100L)).thenReturn(Optional.of(grupo));
        RegistroAsistenciaBulkRecord payload = new RegistroAsistenciaBulkRecord(
            LocalDate.now(),
            List.of(new PaseListaItemRecord(20L, EstadoAsistencia.PRESENTE))
        );

        assertThatThrownBy(() -> asistenciaService.registrarPaseLista(100L, 999L, payload))
            .isInstanceOf(AccessDeniedException.class);

        verify(asistenciaRepository, never()).saveAll(anyList());
    }

    @Test
    void registrarPaseLista_FechaDistintaAHoy_LanzaBadRequest() {
        when(grupoRepository.findById(100L)).thenReturn(Optional.of(grupo));
        RegistroAsistenciaBulkRecord payload = new RegistroAsistenciaBulkRecord(
            LocalDate.now().minusDays(1),
            List.of(new PaseListaItemRecord(20L, EstadoAsistencia.PRESENTE))
        );

        assertThatThrownBy(() -> asistenciaService.registrarPaseLista(100L, 10L, payload))
            .isInstanceOf(BadRequestException.class);
    }

    @Test
    void registrarPaseLista_DuplicadoMismoDia_LanzaConflict() {
        LocalDate hoy = LocalDate.now();
        when(grupoRepository.findById(100L)).thenReturn(Optional.of(grupo));
        when(asistenciaRepository.existsByGrupoIdAndFecha(100L, hoy)).thenReturn(true);

        RegistroAsistenciaBulkRecord payload = new RegistroAsistenciaBulkRecord(
            hoy,
            List.of(new PaseListaItemRecord(20L, EstadoAsistencia.PRESENTE))
        );

        assertThatThrownBy(() -> asistenciaService.registrarPaseLista(100L, 10L, payload))
            .isInstanceOf(DuplicateResourceException.class);
    }

    @Test
    void registrarPaseLista_Valido_GuardaRegistros() {
        LocalDate hoy = LocalDate.now();
        when(grupoRepository.findById(100L)).thenReturn(Optional.of(grupo));
        when(asistenciaRepository.existsByGrupoIdAndFecha(100L, hoy)).thenReturn(false);
        when(inscripcionRepository.findByGrupoId(100L)).thenReturn(
            List.of(Inscripcion.builder().id(1L).alumno(alumno).grupo(grupo).build())
        );
        when(asistenciaRepository.saveAll(anyList())).thenAnswer(inv -> {
            List<Asistencia> lista = inv.getArgument(0);
            lista.get(0).setId(501L);
            return lista;
        });

        RegistroAsistenciaBulkRecord payload = new RegistroAsistenciaBulkRecord(
            hoy,
            List.of(new PaseListaItemRecord(20L, EstadoAsistencia.PRESENTE))
        );

        List<AsistenciaResponseRecord> resultado = asistenciaService.registrarPaseLista(100L, 10L, payload);

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).id()).isEqualTo(501L);
        assertThat(resultado.get(0).estado()).isEqualTo(EstadoAsistencia.PRESENTE);
    }
}
