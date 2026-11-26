package com.passtrack.api.dto.grupo;

import jakarta.validation.constraints.NotNull;

public record InscripcionCreateRecord(
    @NotNull(message = "El identificador del alumno es obligatorio")
    Long alumnoId
) {
}
