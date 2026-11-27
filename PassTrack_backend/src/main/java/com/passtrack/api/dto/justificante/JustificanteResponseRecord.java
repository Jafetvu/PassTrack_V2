package com.passtrack.api.dto.justificante;

import com.passtrack.api.model.Asistencia;
import com.passtrack.api.model.EstadoAsistencia;
import com.passtrack.api.model.EstadoJustificante;
import com.passtrack.api.model.Justificante;

import java.time.Instant;
import java.time.LocalDate;

public record JustificanteResponseRecord(
    Long id,
    Long asistenciaId,
    LocalDate fechaAsistencia,
    EstadoAsistencia estadoAsistencia,
    Long alumnoId,
    String alumnoNombre,
    String grupoClave,
    String grupoNombre,
    String motivo,
    String evidenciaUrl,
    EstadoJustificante estado,
    String comentarioResolucion,
    Instant createdAt,
    Instant resolvedAt
) {
    public static JustificanteResponseRecord fromEntity(Justificante justificante) {
        Asistencia a = justificante.getAsistencia();
        return new JustificanteResponseRecord(
            justificante.getId(),
            a.getId(),
            a.getFecha(),
            a.getEstado(),
            a.getAlumno().getId(),
            a.getAlumno().getNombre(),
            a.getGrupo().getClave(),
            a.getGrupo().getNombre(),
            justificante.getMotivo(),
            justificante.getEvidenciaUrl(),
            justificante.getEstado(),
            justificante.getComentarioResolucion(),
            justificante.getCreatedAt(),
            justificante.getResolvedAt()
        );
    }
}
