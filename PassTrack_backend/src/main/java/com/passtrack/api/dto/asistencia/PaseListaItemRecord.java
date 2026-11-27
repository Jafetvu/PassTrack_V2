package com.passtrack.api.dto.asistencia;

import com.passtrack.api.model.EstadoAsistencia;
import jakarta.validation.constraints.NotNull;

public record PaseListaItemRecord(
    @NotNull(message = "El identificador del alumno es obligatorio")
    Long alumnoId,

    @NotNull(message = "El estado de asistencia es obligatorio")
    EstadoAsistencia estado
) {
}
