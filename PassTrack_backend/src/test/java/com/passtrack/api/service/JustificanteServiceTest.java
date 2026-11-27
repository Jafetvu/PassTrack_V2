package com.passtrack.api.service;

import com.passtrack.api.dto.justificante.JustificanteCreateRecord;
import com.passtrack.api.dto.justificante.JustificanteResolucionRecord;
import com.passtrack.api.dto.justificante.JustificanteResponseRecord;
import com.passtrack.api.exception.DuplicateResourceException;
import com.passtrack.api.model.Asistencia;
import com.passtrack.api.model.EstadoAsistencia;
import com.passtrack.api.model.EstadoJustificante;
import com.passtrack.api.model.Grupo;
import com.passtrack.api.model.Justificante;
import com.passtrack.api.model.Rol;
import com.passtrack.api.model.Usuario;
import com.passtrack.api.repository.AsistenciaRepository;
import com.passtrack.api.repository.JustificanteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.access.AccessDeniedException;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JustificanteServiceTest {

    @Mock
    private JustificanteRepository justificanteRepository;

    @Mock
    private AsistenciaRepository asistenciaRepository;

    @Mock
    private FileStorageService fileStorageService;

    @InjectMocks
    private JustificanteService justificanteService;

    private Asistencia asistenciaFalta;
    private Justificante justificantePendiente;

    @BeforeEach
    void preparar() {
        Usuario docente = Usuario.builder().id(2L).nombre("Docente").rol(Rol.DOCENTE).activo(true).build();
        Usuario alumno = Usuario.builder().id(30L).nombre("Alumno Propietario").rol(Rol.ALUMNO).activo(true).build();
        Grupo grupo = Grupo.builder().id(5L).clave("HIST-1").nombre("Historia").docente(docente).activo(true).build();

        asistenciaFalta = Asistencia.builder()
            .id(77L)
            .fecha(LocalDate.now())
            .estado(EstadoAsistencia.FALTA)
            .alumno(alumno)
            .grupo(grupo)
            .build();

        justificantePendiente = Justificante.builder()
            .id(90L)
            .motivo("Consulta médica")
            .evidenciaUrl("uuid-receta.pdf")
            .estado(EstadoJustificante.PENDIENTE)
            .asistencia(asistenciaFalta)
            .build();
    }

    @Test
    void solicitarJustificante_AsistenciaAjena_LanzaAccessDenied() {
        when(asistenciaRepository.findById(77L)).thenReturn(Optional.of(asistenciaFalta));
        JustificanteCreateRecord dto = new JustificanteCreateRecord(77L, "Motivo");
        MockMultipartFile file = new MockMultipartFile("archivo", "doc.pdf", "application/pdf", "x".getBytes());

        assertThatThrownBy(() -> justificanteService.solicitarJustificante(999L, dto, file))
            .isInstanceOf(AccessDeniedException.class);
        verify(fileStorageService, never()).storeFile(any());
    }

    @Test
    void solicitarJustificante_Duplicado_LanzaConflict() {
        when(asistenciaRepository.findById(77L)).thenReturn(Optional.of(asistenciaFalta));
        when(justificanteRepository.existsByAsistenciaId(77L)).thenReturn(true);
        JustificanteCreateRecord dto = new JustificanteCreateRecord(77L, "Motivo");
        MockMultipartFile file = new MockMultipartFile("archivo", "doc.pdf", "application/pdf", "x".getBytes());

        assertThatThrownBy(() -> justificanteService.solicitarJustificante(30L, dto, file))
            .isInstanceOf(DuplicateResourceException.class);
    }

    @Test
    void resolverJustificante_Aprobado_CambiaAsistenciaAJustificado() {
        when(justificanteRepository.findById(90L)).thenReturn(Optional.of(justificantePendiente));
        when(asistenciaRepository.save(any(Asistencia.class))).thenAnswer(inv -> inv.getArgument(0));
        when(justificanteRepository.save(any(Justificante.class))).thenAnswer(inv -> inv.getArgument(0));

        JustificanteResolucionRecord resolucion = new JustificanteResolucionRecord(
            EstadoJustificante.APROBADO,
            "Comprobante médico válido"
        );

        JustificanteResponseRecord resultado = justificanteService.resolverJustificante(90L, resolucion);

        assertThat(resultado.estado()).isEqualTo(EstadoJustificante.APROBADO);
        assertThat(asistenciaFalta.getEstado()).isEqualTo(EstadoAsistencia.JUSTIFICADO);
        assertThat(resultado.estadoAsistencia()).isEqualTo(EstadoAsistencia.JUSTIFICADO);
        assertThat(resultado.resolvedAt()).isNotNull();
        verify(asistenciaRepository).save(asistenciaFalta);
    }

    @Test
    void resolverJustificante_Rechazado_NoModificaEstadoAsistencia() {
        when(justificanteRepository.findById(90L)).thenReturn(Optional.of(justificantePendiente));
        when(justificanteRepository.save(any(Justificante.class))).thenAnswer(inv -> inv.getArgument(0));

        JustificanteResolucionRecord resolucion = new JustificanteResolucionRecord(
            EstadoJustificante.RECHAZADO,
            "Documento ilegible"
        );

        JustificanteResponseRecord resultado = justificanteService.resolverJustificante(90L, resolucion);

        assertThat(resultado.estado()).isEqualTo(EstadoJustificante.RECHAZADO);
        assertThat(asistenciaFalta.getEstado()).isEqualTo(EstadoAsistencia.FALTA);
        verify(asistenciaRepository, never()).save(any());
    }
}
