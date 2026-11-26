package com.passtrack.api.dto.grupo;

import com.passtrack.api.model.Inscripcion;

import java.time.Instant;

public record InscripcionResponseRecord(
    Long id,
    Long alumnoId,
    String alumnoNombre,
    String alumnoEmail,
    Long grupoId,
    String grupoClave,
    Instant createdAt
) {
    public static InscripcionResponseRecord fromEntity(Inscripcion inscripcion) {
        return new InscripcionResponseRecord(
            inscripcion.getId(),
            inscripcion.getAlumno().getId(),
            inscripcion.getAlumno().getNombre(),
            inscripcion.getAlumno().getEmail(),
            inscripcion.getGrupo().getId(),
            inscripcion.getGrupo().getClave(),
            inscripcion.getCreatedAt()
        );
    }
}
