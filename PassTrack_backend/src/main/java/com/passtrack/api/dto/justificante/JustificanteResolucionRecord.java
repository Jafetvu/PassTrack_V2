package com.passtrack.api.dto.justificante;

import com.passtrack.api.model.EstadoJustificante;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record JustificanteResolucionRecord(
    @NotNull(message = "El estado de resolución es obligatorio")
    EstadoJustificante estado,

    @NotBlank(message = "El comentario de resolución es obligatorio")
    @Size(max = 500, message = "El comentario no puede exceder 500 caracteres")
    String comentarioResolucion
) {
}
