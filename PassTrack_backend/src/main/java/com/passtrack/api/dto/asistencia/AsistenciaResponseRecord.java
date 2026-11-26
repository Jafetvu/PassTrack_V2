package com.passtrack.api.dto.asistencia;

import com.passtrack.api.model.Asistencia;
import com.passtrack.api.model.EstadoAsistencia;
import com.passtrack.api.model.EstadoJustificante;

import java.time.LocalDate;

public record AsistenciaResponseRecord(
    Long id,
    LocalDate fecha,
    EstadoAsistencia estado,
    Long alumnoId,
    String alumnoNombre,
    Long grupoId,
    String grupoClave,
    String grupoNombre,
    boolean tieneJustificante,
    EstadoJustificante estadoJustificante
) {
    public static AsistenciaResponseRecord fromEntity(
            Asistencia asistencia,
            boolean tieneJustificante,
            EstadoJustificante estadoJustificante) {
        return new AsistenciaResponseRecord(
            asistencia.getId(),
            asistencia.getFecha(),
            asistencia.getEstado(),
            asistencia.getAlumno().getId(),
            asistencia.getAlumno().getNombre(),
            asistencia.getGrupo().getId(),
            asistencia.getGrupo().getClave(),
            asistencia.getGrupo().getNombre(),
            tieneJustificante,
            estadoJustificante
        );
    }
}
