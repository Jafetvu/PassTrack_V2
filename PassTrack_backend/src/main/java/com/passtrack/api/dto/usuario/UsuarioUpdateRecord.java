package com.passtrack.api.dto.usuario;

import com.passtrack.api.model.Rol;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UsuarioUpdateRecord(
    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 150, message = "El nombre no puede superar 150 caracteres")
    String nombre,

    @NotNull(message = "El rol es obligatorio")
    Rol rol
) {
}
