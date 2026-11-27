package com.passtrack.api.dto.justificante;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record JustificanteCreateRecord(
    @NotNull(message = "El identificador de la asistencia es obligatorio")
    Long asistenciaId,

    @NotBlank(message = "El motivo del justificante es obligatorio")
    @Size(max = 500, message = "El motivo no puede exceder 500 caracteres")
    String motivo
) {
}
