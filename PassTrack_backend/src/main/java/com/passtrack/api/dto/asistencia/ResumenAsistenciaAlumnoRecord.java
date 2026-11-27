package com.passtrack.api.dto.asistencia;

import java.util.List;

public record ResumenAsistenciaAlumnoRecord(
    Long grupoId,
    String grupoClave,
    String grupoNombre,
    long totalSesiones,
    long presentes,
    long faltas,
    long retardos,
    long justificados,
    double porcentajeAsistencia,
    List<AsistenciaResponseRecord> historial
) {
    public ResumenAsistenciaAlumnoRecord {
        historial = historial == null ? List.of() : List.copyOf(historial);
    }
}
