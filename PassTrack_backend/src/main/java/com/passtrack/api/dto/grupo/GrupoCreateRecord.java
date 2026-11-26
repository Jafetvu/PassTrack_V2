package com.passtrack.api.dto.grupo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record GrupoCreateRecord(
    @NotBlank(message = "La clave del grupo es obligatoria")
    @Size(max = 30, message = "La clave no puede exceder 30 caracteres")
    String clave,

    @NotBlank(message = "El nombre del grupo es obligatorio")
    @Size(max = 150, message = "El nombre no puede exceder 150 caracteres")
    String nombre,

    @NotNull(message = "Debe especificar el docente titular")
    Long docenteId
) {
}
